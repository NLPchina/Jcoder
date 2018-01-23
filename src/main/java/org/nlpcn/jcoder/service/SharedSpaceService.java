package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.nlpcn.jcoder.domain.*;
import org.nlpcn.jcoder.job.CheckDiffJob;
import org.nlpcn.jcoder.job.MasterRunTaskJob;
import org.nlpcn.jcoder.job.MasterTaskCheckJob;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.util.GroupFileListener;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.ZKMap;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by Ansj on 05/12/2017.
 */
public class SharedSpaceService {

	private static final Logger LOG = LoggerFactory.getLogger(SharedSpaceService.class);
	/**
	 * 路由表
	 */
	public static final String MAPPING_PATH = StaticValue.ZK_ROOT + "/mapping";

	/**
	 * Token
	 */
	public static final String TOKEN_PATH = StaticValue.ZK_ROOT + "/token";


	/**
	 * Master
	 */
	private static final String MASTER_PATH = StaticValue.ZK_ROOT + "/master";

	/**
	 * Host
	 * /jcoder/host_group/[ipPort_groupName],[hostGroupInfo]
	 * /jcoder/host_group/[ipPort]
	 */
	public static final String HOST_GROUP_PATH = StaticValue.ZK_ROOT + "/host_group";


	/**
	 * 在线主机
	 */
	private static final String HOST_PATH = StaticValue.ZK_ROOT + "/host";

	/**
	 * group /jcoder/task/group/className.task
	 * |-resource (filePath,md5)
	 * |-lib libMap(libName,md5)
	 */
	public static final String GROUP_PATH = StaticValue.ZK_ROOT + "/group";

	/**
	 * group /jcoder/lock
	 */
	private static final String LOCK_PATH = StaticValue.ZK_ROOT + "/lock";


	private ZookeeperDao zkDao;

	/**
	 * 选举
	 */
	private LeaderLatch leader;

	/**
	 * 监听路由缓存
	 *
	 * @Example /jcoder/mapping/[groupName]/[className]/[methodName]/[hostPort]
	 */
	private TreeCache mappingCache;

	private ZKMap<Token> tokenCache;

	//缓存在线主机 key:127.0.0.1:2181_groupName HostGroup.java
	private ZKMap<HostGroup> hostGroupCache;

	/**
	 * 在线groupcache
	 */
	private TreeCache groupCache;


	/**
	 * 递归查询所有子文件
	 */
	public void walkGroupCache(Set<String> set, String path) throws Exception {
		try {
			Set<String> children = groupCache.getCurrentChildren(path).keySet();
			for (String child : children) {
				String cPath = path + "/" + child;
				set.add(cPath);
				walkGroupCache(set, cPath);
			}
		} catch (Exception e) {
			LOG.error("walk file err: " + path);
		}
	}


	/**
	 * 删除一个地址映射
	 */
	public void removeMapping(String groupName, String className, String methodName, String hostPort) {
		try {
			String path = MAPPING_PATH + "/" + groupName + "/" + className + "/" + methodName + "/" + hostPort;
			if (zkDao.getZk().checkExists().forPath(path) != null) {
				zkDao.getZk().delete().forPath(path);
				LOG.info("remove mapping {}/{}/{}/{} ok", hostPort, groupName, className, methodName);
			} else {
				LOG.warn("remove mapping {}/{}/{}/{} but it not exists", hostPort, groupName, className, methodName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("remove err {}/{}/{}/{} message: {}", hostPort, groupName, className, methodName, e.getMessage());
		}

	}

	/**
	 * 增加一个mapping到
	 */
	public void addMapping(String groupName, String className, String methodName) {

		StringBuilder sb = new StringBuilder(MAPPING_PATH);

		String path = null;
		try {
			sb.append("/").append(groupName).append("/").append(className).append("/").append(methodName).append("/");
			zkDao.getZk().createContainers(sb.toString());
			sb.append(StaticValue.getHostPort());
			path = sb.toString();
			setData2ZKByEphemeral(path, new byte[0], null);
			LOG.info("add mapping: {} ok", path);
		} catch (Exception e) {
			LOG.error("Add mapping " + path + " err", e);
			e.printStackTrace();
		}
	}

	/**
	 * 增加一个task到集群中，
	 */
	public void addTask(Task task) throws Exception {
		// /jcoder/task/group/className.task
		setData2ZK(GROUP_PATH + "/" + task.getGroupName() + "/" + task.getName(), JSONObject.toJSONBytes(task));
	}

	/**
	 * lock a path in /zookper/locak[/path]
	 */
	public InterProcessMutex lockGroup(String groupName) {
		InterProcessMutex lock = new InterProcessMutex(zkDao.getZk(), LOCK_PATH + "/" + groupName);
		return lock;
	}

	/**
	 * 解锁一个目录并尝试删除
	 */
	public void unLockAndDelete(InterProcessMutex lock) {
		if (lock != null && lock.isAcquiredInThisProcess()) {
			try {
				lock.release(); //释放锁
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将数据写入到zk中
	 */
	private void setData2ZK(String path, byte[] data) throws Exception {

		LOG.info("add data to: {}, data len: {} ", path, data.length);

		boolean flag = true;
		if (zkDao.getZk().checkExists().forPath(path) == null) {
			try {
				zkDao.getZk().create().creatingParentsIfNeeded().forPath(path, data);
				flag = false;
			} catch (KeeperException.NodeExistsException e) {
				flag = true;
			}
		}

		if (flag) {
			zkDao.getZk().setData().forPath(path, data);
		}

		//如果修改了子目录节点则将 跟目录md5设置为空
		if (path.startsWith(GROUP_PATH)) {
			int index = path.indexOf("/file/");
			if (index > -1) {
				String rootPath = path.substring(0, index + 5);
				FileInfo root = getData(rootPath, FileInfo.class);
				root.setMd5("EMPTY__");
				setData2ZK(rootPath, JSONObject.toJSONBytes(root));
			}
		}
	}

	public byte[] getData2ZK(String path) throws Exception {
		LOG.info("get data from: {} ", path);
		byte[] bytes = null;
		try {
			bytes = zkDao.getZk().getData().forPath(path);
		} catch (KeeperException.NoNodeException e) {
		}

		return bytes;
	}


	/**
	 * 将临时数据写入到zk中，临时节点保证只有添加不做更新
	 */
	public void setData2ZKByEphemeral(String path, byte[] data, Watcher watcher) throws Exception {

		if (zkDao.getZk().checkExists().forPath(path) != null) {
			try {
				zkDao.getZk().delete().forPath(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		zkDao.getZk().create().withMode(CreateMode.EPHEMERAL).forPath(path, data);

		if (watcher != null) {
			zkDao.getZk().getData().usingWatcher(watcher).forPath(path); //注册监听
		}
	}


	public SharedSpaceService init() throws Exception {

		long start = System.currentTimeMillis();
		LOG.info("shared space init");

		this.zkDao = new ZookeeperDao(StaticValue.ZK).start();

		//清空临时节点


		//注册监听事件
		zkDao.getZk().getConnectionStateListenable().addListener((client, connectionState) -> {
			LOG.info("=============================" + connectionState);
			if (connectionState == ConnectionState.LOST) {
				while (true) {
					try {
						StaticValue.space().release();
						StaticValue.space().init();
						break;
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (Exception e) {
						LOG.error("reconn zk server ", e);
					}
				}
			}
		});


		/**
		 * 选举leader
		 */
		leader = new LeaderLatch(zkDao.getZk(), MASTER_PATH, StaticValue.getHostPort());
		leader.addListener(new LeaderLatchListener() {
			@Override
			public void isLeader() {
				StaticValue.setMaster(true);
				LOG.info("I am master my host is " + StaticValue.getHostPort());
				MasterTaskCheckJob.startJob();
				MasterRunTaskJob.startJob();
			}

			@Override
			public void notLeader() {
				StaticValue.setMaster(false);
				LOG.info("I am lost master " + StaticValue.getHostPort());
				MasterTaskCheckJob.stopJob();
				MasterRunTaskJob.stopJob();
			}

		});
		leader.start();


		if (zkDao.getZk().checkExists().forPath(HOST_GROUP_PATH) == null) {
			zkDao.getZk().create().creatingParentsIfNeeded().forPath(HOST_GROUP_PATH);
		}


		if (zkDao.getZk().checkExists().forPath(GROUP_PATH) == null) {
			zkDao.getZk().create().creatingParentsIfNeeded().forPath(GROUP_PATH);
		}

		if (zkDao.getZk().checkExists().forPath(TOKEN_PATH) == null) {
			zkDao.getZk().create().creatingParentsIfNeeded().forPath(TOKEN_PATH);
		}

		if (zkDao.getZk().checkExists().forPath(HOST_PATH) == null) {
			zkDao.getZk().create().creatingParentsIfNeeded().forPath(HOST_PATH);
		}

		/**
		 * 监听group目录
		 */
		groupCache = new TreeCache(zkDao.getZk(), GROUP_PATH);
		groupCache.start();


		/**
		 * 缓存主机
		 */
		hostGroupCache = new ZKMap(zkDao.getZk(), HOST_GROUP_PATH, HostGroup.class).start();

		joinCluster();

		setData2ZKByEphemeral(HOST_PATH + "/" + StaticValue.getHostPort(), new byte[0], new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getType() == Watcher.Event.EventType.NodeDeleted) { //节点删除了
					try {
						LOG.info("I lost node so add it again " + event.getPath());
						setData2ZKByEphemeral(event.getPath(), new byte[0], this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		//映射信息
		mappingCache = new TreeCache(zkDao.getZk(), MAPPING_PATH).start();

		/**
		 * 监控token
		 */
		tokenCache = new ZKMap(zkDao.getZk(), TOKEN_PATH, Token.class).start();

		groupCache.getListenable().addListener((client, event) -> { //广播监听group目录
			LOG.info("found group change type:{} path:{}", event.getType(), event.getData().getPath());
			if (event.getData() != null) {
				switch (event.getType()) {
					case NODE_ADDED:
					case NODE_UPDATED:
					case NODE_REMOVED:
						String path = event.getData().getPath().substring(GROUP_PATH.length() + 1);
						String[] split = path.split("/");
						String groupName = split[0];
						if (split.length < 2) {
							return;
						}
						String taskName = split[1];

						if (StaticValue.isMaster() && !"file".equals(taskName)) {//如果本机是master,并且更新的是task
							MasterTaskCheckJob.addQueue(new Handler(event.getData().getPath(), groupName, taskName, event.getType()));
						}

						//如果本机没有group则忽略
						if (StaticValue.getSystemIoc().get(GroupService.class, "groupService").findGroupByName(groupName) == null) {
							return;
						}


						Set<String> taskNames = null;
						Set<String> relativePaths = null;

						if ("file".equals(taskName)) {
							path = path.substring(groupName.length() + 5);
							if (StringUtil.isNotBlank(path) && path != "/") {
								relativePaths = new HashSet<>();
								relativePaths.add(path);
							}
						} else {
							taskNames = new HashSet<>();
							taskNames.add(taskName);
						}

						if ((relativePaths != null && relativePaths.size() > 0) || (taskNames != null && taskNames.size() > 0)) {
							different(groupName, taskNames, relativePaths, false, false);
						}
						break;
				}
			}
		});


		LOG.info("shared space init ok use time {}", System.currentTimeMillis() - start);
		return this;

	}

	/**
	 * 主机关闭的时候调用,平时不调用
	 */
	public void release() throws Exception {
		LOG.info("release SharedSpace");
		Optional.of(groupCache).ifPresent(o -> closeWithoutException(o));
		Optional.of(leader).ifPresent((o) -> closeWithoutException(o));
		Optional.of(mappingCache).ifPresent((o) -> closeWithoutException(o));
		Optional.of(tokenCache).ifPresent((o) -> closeWithoutException(o));
		Optional.of(hostGroupCache).ifPresent((o) -> closeWithoutException(o));
		Optional.of(zkDao).ifPresent((o) -> closeWithoutException(o));
	}


	/**
	 * 关闭一个类且不抛出异常
	 */
	private void closeWithoutException(Closeable close) {
		try {
			close.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 加入集群,如果发生不同则记录到different中
	 */
	private Map<String, List<Different>> joinCluster() throws IOException {

		Map<String, List<Different>> result = new HashMap<>();

		List<Group> groups = GroupService.allLocalGroup();
		Collections.shuffle(groups); //因为要锁组，重新排序下防止顺序锁


		for (Group group : groups) {
			List<Different> diffs = joinCluster(group, true);
			result.put(group.getName(), diffs);

			if (StaticValue.TESTRING) {
				GroupFileListener.unRegediter(group.getName());
				GroupFileListener.regediter(group.getName());
			}

		}

		return result;
	}

	/**
	 * 加入刷新一个主机到集群中
	 */
	public List<Different> joinCluster(Group group, boolean upMapping) throws IOException {

		LOG.info("join cluster by groupName: {} upMapping: {}", group.getName(), upMapping);

		List<Different> diffs = new ArrayList<>();

		String groupName = group.getName();

		JarService.getOrCreate(groupName);//查找之前先初始化一下

		//查找出这个组所有的task
		List<Task> tasks = TaskService.findAllTasksByCache().stream().filter(t -> groupName.equals(t.getGroupName())).collect(Collectors.toList());

		//查找出这个组所有的文件
		List<FileInfo> fileInfos = FileInfoService.listFileInfosByGroup(groupName);

		//增加或查找不同
		InterProcessMutex lock = lockGroup(groupName);
		try {
			lock.acquire();
			//判断group是否存在。如果不存在。则进行安全添加
			if (zkDao.getZk().checkExists().forPath(GROUP_PATH + "/" + groupName) == null) {
				addGroup2Cluster(groupName, tasks, fileInfos);
				diffs = Collections.emptyList();
			} else {
				diffs = diffGroup(groupName, tasks, (ArrayList<FileInfo>) fileInfos);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			unLockAndDelete(lock);
		}

		if (upMapping) {
			tasks.forEach(task -> {
				try {
					new JavaRunner(task).compile();

					Collection<CodeInfo.ExecuteMethod> executeMethods = task.codeInfo().getExecuteMethods();

					executeMethods.forEach(e -> {
						addMapping(task.getGroupName(), task.getName(), e.getMethod().getName());
					});

				} catch (Exception e) {
					LOG.error("compile {}/{} err ", task.getGroupName(), task.getCode(), e);
				}
			});
		}
		return diffs;
	}

	/**
	 * 取得所有的在线主机
	 */
	public List<String> getAllHosts() throws Exception {
		return getZk().getChildren().forPath(HOST_PATH);
	}

	/**
	 * 查询本地group和集群currentGroup差异
	 *
	 * @param groupName 组名称
	 * @param list      组内的所有任务
	 */
	private List<Different> diffGroup(String groupName, List<Task> list, ArrayList<FileInfo> fileInfos) throws Exception {

		String path = GROUP_PATH + "/" + groupName;

		Set<String> paths = groupCache.getCurrentChildren(path).keySet();
		Set<String> clusterTaskNames = paths.stream().filter(p -> !p.equals("file")).collect(Collectors.toSet());

		Set<String> taskNames = new HashSet<>();
		taskNames.addAll(clusterTaskNames);
		list.forEach(t -> taskNames.add(t.getName()));


		Set<String> relativePaths = new HashSet<>();

		//先判断根结点
		FileInfo root = getData(GROUP_PATH + "/" + groupName + "/file", FileInfo.class);

		boolean fileChange = !root.getMd5().equals(fileInfos.get(fileInfos.size() - 1).getMd5());
		if (root != null && !fileChange) {
			LOG.info(groupName + " file md5 same so skip");
		} else {
			LOG.info(groupName + " file changed find differents");
			walkGroupCache(relativePaths, GROUP_PATH + "/" + groupName + "/file");
			for (int i = 0; i < fileInfos.size() - 1; i++) {
				relativePaths.add(fileInfos.get(i).getRelativePath());
			}
		}

		List<Different> diffs = different(groupName, taskNames, relativePaths, true, true);

		boolean fileDiff = false;

		for (Different diff : diffs) {
			if (diff.getType() == 1) {
				fileDiff = true;
			}
			LOG.info(diff.toString());
		}

		if (!fileDiff && fileChange) { //发现文件无不同。那么更新根目录md5
			setData2ZK(GROUP_PATH + "/" + groupName + "/file", JSONObject.toJSONBytes(fileInfos.get(fileInfos.size() - 1)));
		}

		return diffs;

	}


	/**
	 * 刷新一个，固定的task 或者是 file。不和集群中的其他文件进行对比
	 *
	 * @Param upHostGroup 只有全局刷新的时候才设置为true， 默认值发现不同。
	 */
	public List<Different> different(String groupName, Set<String> taskNames, Set<String> relativePaths, boolean upHostGroup, boolean useCache) throws Exception {

		LOG.info("to different group:{}", groupName);

		List<Different> diffs = new ArrayList<>();

		if (taskNames != null && !taskNames.isEmpty()) {
			TaskService taskService = StaticValue.getSystemIoc().get(TaskService.class);
			for (String taskName : taskNames) {
				Different different = new Different();
				different.setPath(taskName);
				different.setGroupName(groupName);
				different.setType(0);
				diffTask(taskService.findTask(groupName, taskName), different, groupName, taskName, useCache);
				if (different.getMessage() != null) {
					diffs.add(different);
				}
			}
		}

		if (relativePaths != null && relativePaths.size() > 0) {
			for (String relativePath : relativePaths) {
				Different different = new Different();
				different.setGroupName(groupName);
				different.setPath(relativePath);
				different.setType(1);
				diffFile(relativePath, different, groupName, useCache);
				if (different.getMessage() != null) {
					diffs.add(different);
				}
			}
		}

		for (Different diff : diffs) {
			LOG.info("useCache:{} {} ", useCache, diff);
		}

		HostGroup cHostGroup = hostGroupCache.get(StaticValue.getHostPort() + "_" + groupName);

		CheckDiffJob.addDiff(groupName, diffs);//注册到定时任务的监控

		if (upHostGroup || cHostGroup == null || (diffs.size() > 0 && cHostGroup.isCurrent())) {
			HostGroup hostGroup = new HostGroup();
			hostGroup.setSsl(StaticValue.IS_SSL);
			hostGroup.setCurrent(diffs.size() == 0);
			hostGroup.setWeight(diffs.size() > 0 ? 0 : 100);
			Watcher watcher = null;
			if (cHostGroup == null) {
				new HostGroupWatcher(hostGroup);
			}
			try {
				setData2ZKByEphemeral(HOST_GROUP_PATH + "/" + StaticValue.getHostPort() + "_" + groupName, JSONObject.toJSONBytes(hostGroup), watcher); //应该已经有一个监听不用再加了
			} catch (Exception e1) {
				e1.printStackTrace();
				LOG.error("add host group info err !!!!!", e1);
			}
		}

		return diffs;
	}

	/**
	 * 和主版本对比
	 *
	 * @param relativePath
	 * @param different
	 * @param groupName
	 * @throws Exception
	 */
	private void diffFile(String relativePath, Different different, String groupName, boolean useCache) throws Exception {

		FileInfo cInfo = null;
		if (useCache) {
			cInfo = getDataInGroupCache(GROUP_PATH + "/" + groupName + "/file" + relativePath, FileInfo.class);
		} else {
			cInfo = getData(GROUP_PATH + "/" + groupName + "/file" + relativePath, FileInfo.class);
		}


		File file = new File(StaticValue.GROUP_FILE, groupName + relativePath);

		if (cInfo == null) {
			if (file.exists()) {
				different.addMessage("文件在主版本中不存在");
				return;
			}
			return;
		}

		if (!file.exists()) {
			different.addMessage("文件在本地不存在");
			return;
		}

		FileInfo lInfo = new FileInfo(file);
		if (!cInfo.getMd5().equals(lInfo.getMd5())) {
			different.addMessage("文件内容不一致");
		}
	}

	/**
	 * 比较两个task是否一致
	 */
	private void diffTask(Task task, Different different, String groupName, String taskName, boolean useCache) {
		if (task != null) {
			groupName = task.getGroupName();
			taskName = task.getName();
		}

		try {
			Task cluster = null;
			if (useCache) {
				cluster = getDataInGroupCache(GROUP_PATH + "/" + groupName + "/" + taskName, Task.class);
			} else {
				getZk().sync().forPath(GROUP_PATH + "/" + groupName + "/" + taskName);
				cluster = getData(GROUP_PATH + "/" + groupName + "/" + taskName, Task.class);
			}


			if (cluster == null) {
				if (task == null) {
					return;
				}

				different.addMessage("集群中不存在此Task");
				return;
			} else {
				if (task == null) {
					different.addMessage("主机" + StaticValue.getHostPort() + "中不存在此Task");
					return;
				}
			}

			if (!Objects.equals(task.getCode(), cluster.getCode())) {
				different.addMessage("代码不一致");
			}
			if (!Objects.equals(task.getStatus(), cluster.getStatus())) {
				different.addMessage("状态不一致");
			}
			if (!Objects.equals(task.getType(), cluster.getType())) {
				different.addMessage("类型不一致");
			}
			if (task.getType() == 2 && !Objects.equals(task.getScheduleStr(), cluster.getScheduleStr())) {
				different.addMessage("定时计划不一致");
			}
			if (!Objects.equals(task.getDescription(), cluster.getDescription())) {
				different.addMessage("简介不一致");
			}
		} catch (Exception e) {
			e.printStackTrace();
			different.addMessage(e.getMessage());
		}

	}

	/**
	 * 如果这个group在集群中没有则，添加到集群
	 */
	private void addGroup2Cluster(String groupName, List<Task> list, List<FileInfo> fileInfos) throws IOException {

		list.stream().forEach(t -> {
			try {
				addTask(t);
			} catch (Exception e) {
				e.printStackTrace(); //这个异常很麻烦
				LOG.error("add task to cluster err ：" + t.getGroupName() + "/" + t.getName(), e);
			}
		});

		fileInfos.stream().forEach(fi -> {
			try {
				String relativePath = fi.getRelativePath();
				if ("/".equals(relativePath)) {
					setData2ZK(GROUP_PATH + "/" + groupName + "/file", JSONObject.toJSONBytes(fi));
				} else {
					setData2ZK(GROUP_PATH + "/" + groupName + "/file" + fi.getRelativePath(), JSONObject.toJSONBytes(fi));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		});


	}


	/**
	 * 将文件同步更新到集群中
	 */
	public void upCluster(String groupName, String relativePath) throws Exception {
		File file = new File(StaticValue.GROUP_FILE, groupName + relativePath);
		if (file.exists()) {
			setData2ZK(GROUP_PATH + "/" + groupName + "/file" + relativePath, JSONObject.toJSONBytes(new FileInfo(file)));
			LOG.info("up file: {} to {} -> {}", file.getAbsoluteFile(), groupName, relativePath);
		} else {
			zkDao.getZk().delete().deletingChildrenIfNeeded().forPath(GROUP_PATH + "/" + groupName + "/file" + relativePath);
			LOG.info("delete file to {} -> {}", groupName, relativePath);
		}
	}

	public <T> T getData(String path, Class<T> c) throws Exception {
		if (path.startsWith(TOKEN_PATH)) {
			path = "/jcoder/token/************";//token path 不敢直接打印到日志
		}
		byte[] bytes = getData2ZK(path);
		if (bytes == null) {
			return null;
		}
		return JSONObject.parseObject(bytes, c);
	}

	public <T> T getDataInGroupCache(String path, Class<T> c) throws Exception {
		byte[] data = groupCache.getCurrentData(path).getData();
		if (data == null) {
			return getData(path, c); //缓存没生效这里补漏
		}
		return JSONObject.parseObject(data, c);
	}

	/**
	 * 随机的获取一台和主版本同步着的主机
	 */
	public List<String> getCurrentHostPort(String groupName) {
		List<String> collect = hostGroupCache.entrySet().stream().filter(e -> e.getValue().isCurrent()).map(e -> e.getKey()).filter(k -> groupName.equals(k.split("_")[1])).map(k -> k.split("_")[0]).collect(Collectors.toList());
		return collect;
	}

	/**
	 * 从主机集群中获取随机一个同步版本的机器，如果机器不存在则返回null
	 *
	 * @param groupName 组名称
	 */
	public String getRandomCurrentHostPort(String groupName) {
		List<String> collect = getCurrentHostPort(groupName);
		if (collect.size() == 0) {
			return null;
		}
		return collect.get(new Random().nextInt(collect.size()));
	}

	public CuratorFramework getZk() {
		return zkDao.getZk();
	}

	public TreeCache getMappingCache() {
		return mappingCache;
	}

	public ZKMap<Token> getTokenCache() {
		return tokenCache;
	}

	public ZKMap<HostGroup> getHostGroupCache() {
		return hostGroupCache;
	}

	public TreeCache getGroupCache() {
		return groupCache;
	}


}
