package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.eclipse.jetty.util.StringUtil;
import org.nlpcn.jcoder.domain.*;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
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
	private static final String MAPPING_PATH = StaticValue.ZK_ROOT + "/mapping";

	/**
	 * 广播体操
	 */
	public static final String MESSAGE_PATH = StaticValue.ZK_ROOT + "/message";

	/**
	 * Token
	 */
	public static final String TOKEN_PATH = StaticValue.ZK_ROOT + "/token";

	/**
	 * Host
	 */
	public static final String HOST_PATH = StaticValue.ZK_ROOT + "/host";

	/**
	 * group /jcoder/task/group/className.task
	 * |-resource (filePath,md5)
	 * |-lib libMap(libName,md5)
	 */
	public static final String GROUP_PATH = StaticValue.ZK_ROOT + "/group";


	/**
	 * group /jcoder/lock
	 */
	public static final String LOCK_PATH = StaticValue.ZK_ROOT + "/lock";

	/**
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<Long, AtomicLong> taskSuccess = new HashMap<>();

	/**
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<Long, AtomicLong> taskErr = new HashMap<>();

	protected ZookeeperDao zkDao;

	/**
	 * 监听路由缓存
	 *
	 * @Example /jcoder/mapping/className/methodName/hostPort,groupName
	 */
	private TreeCache mappingCache;

	private TreeCache tokenCache;

	//缓存在线主机 key:127.0.0.1:2181 value:https？http_weight(int)
	private TreeCache hostCache;


	/**
	 * {groupName , className , path:存放路径 , type :0 普通任务 ， 1 while 任务  2.all任务}
	 * 增加一个任务到任务队列
	 */
	public void add2TaskQueue(String groupName, String className, String scheduleStr) throws Exception {
		String id = UUID.randomUUID().toString();
		JSONObject job = new JSONObject();
		job.put("groupName", groupName);
		job.put("className", className);
		int type = 0;
		if ("while".equalsIgnoreCase(scheduleStr)) {
			type = 1;
		} else if ("all".equalsIgnoreCase(scheduleStr)) {
			type = 2;
		}
		job.put("type", type);
		zkDao.getZk().setData().forPath(MESSAGE_PATH + "/" + id, job.getBytes("utf-8"));
	}


	/**
	 * 计数器，记录task成功失败个数
	 *
	 * @param id
	 * @param success
	 */
	public void counter(Long id, boolean success) {
		if (success) {
			taskSuccess.compute(id, (k, v) -> {
				if (v == null) {
					v = new AtomicLong();
				}
				v.incrementAndGet();
				return v;
			});
		} else {
			taskErr.compute(id, (k, v) -> {
				if (v == null) {
					v = new AtomicLong();
				}
				v.incrementAndGet();
				return v;
			});
		}
	}

	/**
	 * 获得一个task成功次数
	 *
	 * @param id
	 * @return
	 */
	public long getSuccess(Long id) {
		AtomicLong atomicLong = taskSuccess.get(id);
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
	}

	/**
	 * 获得一个task失败次数
	 *
	 * @param id
	 * @return
	 */
	public long getError(Long id) {
		AtomicLong atomicLong = taskErr.get(id);
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
	}

	/**
	 * 获得一个token
	 *
	 * @param key
	 * @return
	 */
	public Token getToken(String key) throws Exception {
		ChildData currentData = tokenCache.getCurrentData(TOKEN_PATH + "/" + key);
		if (currentData == null) {
			return null;
		}
		Token token = JSONObject.parseObject(currentData.getData(), Token.class);
		if ((token.getExpirationTime().getTime() - System.currentTimeMillis()) < 20 * 60000L) {
			token.setExpirationTime(new Date(System.currentTimeMillis() + 20 * 60000L));
			zkDao.getZk().setData().forPath(TOKEN_PATH + "/" + token.getToken(), JSONObject.toJSONBytes(token));
		}

		return token;
	}

	/**
	 * 注册一个token,token必须是刻一用路径描述的
	 *
	 * @param token
	 */
	public void regToken(Token token) throws Exception {
		if (token.getToken().contains("/")) {
			throw new RuntimeException("token can not has / in name ");
		}
		zkDao.getZk().create().creatingParentsIfNeeded().forPath(TOKEN_PATH + "/" + token.getToken(), JSONObject.toJSONBytes(token));
	}

	/**
	 * 移除一个token
	 *
	 * @param key
	 * @return
	 */
	public Token removeToken(String key) throws Exception {
		Token token = getToken(key);
		if (token != null) {
			zkDao.getZk().delete().forPath(TOKEN_PATH + "/" + key);
		}
		return token;
	}


	/**
	 * 删除一个地址映射
	 *
	 * @param path
	 */
	public void removeMapping(String path) throws Exception {
		zkDao.getZk().delete().forPath(makeMappingPath(path));
	}

	/**
	 * 增加一个mapping到
	 *
	 * @param path
	 */
	public void addMapping(String path) throws Exception {
		String className = path.split("/")[0];
		Task task = TaskService.findTaskByCache(className);
		byte[] data = task.getGroupName().getBytes();
		path = makeMappingPath(path);
		setData2ZK(path, data);
	}

	/**
	 * 增加一个task到集群中，
	 *
	 * @param task
	 */
	public void addTask(Task task) throws Exception {
		// /jcoder/task/group/className.task
		setData2ZK(GROUP_PATH + "/" + task.getGroupName() + "/" + task.getName(), JSONObject.toJSONBytes(task));
	}

	/**
	 * 增加一个task到集群中，如果冲突返回false
	 *
	 * @param groupName
	 * @return 是否冲突
	 * @Param file
	 */
	public void addFile(String groupName, File file) throws Exception {
		String md5ByFile = null;
		if (file.isDirectory()) {
			md5ByFile = "";
		} else {
			md5ByFile = MD5Util.getMd5ByFile(file);
		}

		File root = new File(StaticValue.GROUP_FILE, groupName);

		StringBuilder sb = new StringBuilder();

		while (!(file.equals(root))) {
			System.out.println(file.getAbsoluteFile());
			sb.insert(0, file.getName());
			sb.insert(0, "/");
			file = file.getParentFile();
		}
		;
		sb.insert(0, "/" + groupName + "/file");

		setData2ZK(GROUP_PATH + sb.toString(), md5ByFile.getBytes("utf-8"));
	}

	/**
	 * lock a path in /zookper/locak[/path]
	 *
	 * @param path
	 */
	private InterProcessMutex lock(String path) {
		return new InterProcessMutex(zkDao.getZk(), LOCK_PATH + path);
	}

	/**
	 * 传入路径，在路径中寻找合适运行此方法的主机
	 *
	 * @param groupName
	 * @param path
	 * @return 保护http。。。地址的
	 */
	public String host(String groupName, String path) {
		String[] split = path.split("/");
		if (split.length < 3) {
			return null;
		}

		String className = split[2];
		String methodName = null;
		if (split.length >= 4) {
			methodName = split[3];
		}

		if (StringUtil.isBlank(className)) {
			return null;
		}

		return host(groupName, className, methodName);

	}


	/**
	 * 传入一个地址，给出路由到的地址，如果返回空则为本机，未找到或其他情况也保留于本机
	 *
	 * @return
	 */
	public String host(String groupName, String className, String mehtodName) {
		Map<String, ChildData> currentChildren = null;
		if (StringUtil.isBlank(mehtodName)) {
			currentChildren = mappingCache.getCurrentChildren(MAPPING_PATH + "/" + className);
		} else {
			currentChildren = mappingCache.getCurrentChildren(MAPPING_PATH + "/" + className + "/" + mehtodName);
		}

		if (currentChildren == null || currentChildren.size() == 0) {
			return null;
		}

		List<KeyValue<String, Integer>> hosts = new ArrayList<>();

		int sum = 0;
		for (Map.Entry<String, ChildData> entry : currentChildren.entrySet()) {
			try {
				if (StringUtil.isNotBlank(groupName)) {
					String gName = new String(entry.getValue().getData(), "utf-8");
					if (!groupName.equals(gName)) {
						continue;
					}
				}
			} catch (Exception e) {
				LOG.error("get route err by " + entry.getKey(), e);
				e.printStackTrace();
				continue;
			}

			String hostPort = entry.getKey();

			ChildData currentData = hostCache.getCurrentData(HOST_PATH + "/" + hostPort);

			if (currentData == null) {
				LOG.warn(HOST_PATH + "/" + hostPort + " got null , so skip");
				continue;
			}

			HostInfo hostInfo = JSONObject.parseObject(currentData.getData(), HostInfo.class);
			Integer weight = hostInfo.getWeight();
			if (weight <= 0) {
				LOG.info(HOST_PATH + "/" + hostPort + " weight less than zero , so skip");
				continue;
			}
			sum += weight;
			hosts.add(KeyValue.with((hostInfo.isSsl() ? "https://" : "http://") + hostPort, weight));
		}


		if (hosts.size() == 0) {
			return null;
		}

		int random = new Random().nextInt(sum);

		for (KeyValue<String, Integer> host : hosts) {
			random -= host.getValue();
			if (random < 0) {
				LOG.info("{}/{}/{} proxy to {} ", groupName, className, mehtodName, host.getKey());
				return host.getKey();
			}
		}

		LOG.info("this log impossible print !");

		return null;

	}

	/**
	 * 将数据写入到zk中
	 *
	 * @param path
	 * @param data
	 * @throws Exception
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
	}

	private byte[] getData2ZK(String path) throws Exception {
		LOG.info("get data to: from data ", path);
		byte[] bytes = null;
		try {
			bytes = zkDao.getZk().getData().forPath(path);
		} catch (KeeperException.NoNodeException e) {
		}

		return bytes;
	}


	/**
	 * 将数据写入到zk中
	 *
	 * @param path
	 * @param data
	 * @throws Exception
	 */
	private void setData2ZKByEphemeral(String path, byte[] data) throws Exception {

		boolean flag = true;

		if (zkDao.getZk().checkExists().forPath(path) == null) {
			try {
				zkDao.getZk().create().withMode(CreateMode.EPHEMERAL).forPath(path, data);
				flag = false;
			} catch (KeeperException.NodeExistsException e) {
				flag = true;
			}
		}

		if (flag) {
			zkDao.getZk().setData().forPath(path, data);
		}
	}

	/**
	 * 构建路径到zk的路径
	 *
	 * @param path
	 * @return
	 */
	private String makeMappingPath(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(MAPPING_PATH);
		sb.append("/");
		sb.append(path);
		sb.append("/");
		sb.append(StaticValue.getHostPort());

		return sb.toString();
	}


	public SharedSpaceService init() throws Exception {
		this.zkDao = new ZookeeperDao(StaticValue.ZK);

		if (zkDao.getZk().checkExists().forPath(HOST_PATH) == null) {
			zkDao.getZk().create().creatingParentsIfNeeded().forPath(HOST_PATH);
		}

		List<Different> differents = joinCluster();

		//将本机注册到集群

		HostInfo hostInfo = new HostInfo();

		hostInfo.setHost(StaticValue.getHost());
		hostInfo.setPort(StaticValue.PORT);
		hostInfo.setSsl(StaticValue.IS_SSL);
		hostInfo.setWeight(differents.size() > 0 ? 0 : 100);
		hostInfo.setCurrent(differents.size() == 0);

		setData2ZKByEphemeral(HOST_PATH + "/" + StaticValue.getHostPort(), JSONObject.toJSONBytes(hostInfo));

		//映射信息
		mappingCache = new TreeCache(zkDao.getZk(), MAPPING_PATH).start();

		/**
		 * 监控token
		 */
		tokenCache = new TreeCache(zkDao.getZk(), TOKEN_PATH).start();

		/**
		 * 缓存主机
		 */
		hostCache = new TreeCache(zkDao.getZk(), HOST_PATH).start();


		//监听task运行
		NodeCache nodeCache = new NodeCache(zkDao.getZk(), MESSAGE_PATH);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				byte[] data = nodeCache.getCurrentData().getData();
				//TODO: 拿到任务后去执行吧。。心累
			}
		});
		nodeCache.start();
		return this;
	}

	/**
	 * 主机关闭的时候调用,平时不调用
	 */
	public void release() throws Exception {
		//将本机权重设置为0
		try {
			ChildData currentData = hostCache.getCurrentData(HOST_PATH + StaticValue.getHostPort());
			HostInfo hostInfo = JSONObject.parseObject(currentData.getData(), HostInfo.class);
			hostInfo.setWeight(0);
			;
			setData2ZKByEphemeral(HOST_PATH + StaticValue.getHostPort(), JSONObject.toJSONBytes(hostInfo));
		} catch (Exception e) {
			e.printStackTrace();
		}
		relaseMapping(StaticValue.getHostPort());
		zkDao.getZk().delete().forPath(HOST_PATH + StaticValue.getHostPort());

		mappingCache.close();
		tokenCache.close();
		hostCache.close();
		zkDao.close();
	}

	/**
	 * 释放删除本机的mpping信息
	 *
	 * @param hostPort
	 */
	private void relaseMapping(String hostPort) {
		Map<String, ChildData> currentChildren = mappingCache.getCurrentChildren(MAPPING_PATH);
		//TODO:
	}


	/**
	 * 加入集群,如果发生不同则记录到different中
	 */
	private List<Different> joinCluster() {

		List<Task> tasks = StaticValue.systemDao.search(Task.class, "id");
		if (tasks.size() == 0) {
			return Collections.emptyList(); //一个空的主机无害
		}

		List<Different> allDiff = new ArrayList<>();

		tasks.stream().collect(Collectors.groupingBy(t -> t.getGroupName())).forEach((groupName, list) -> {
			InterProcessMutex lock = lock(LOCK_PATH + "/" + groupName);
			try {
				//判断group是否存在。如果不存在。则进行安全添加
				if (zkDao.getZk().checkExists().forPath(GROUP_PATH + "/" + groupName) == null) {
					lock.acquire();
					addGroup2Cluster(groupName, list);
				} else {
					allDiff.addAll(diffGroup(groupName, list));
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (lock != null && lock.isAcquiredInThisProcess()) {
					try {
						lock.release(); //释放锁
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});

		return allDiff;
	}

	/**
	 * 查询本地group和集群currentGroup差异
	 *
	 * @param groupName 组名称
	 * @param list      组内的所有任务
	 * @return
	 * @throws IOException
	 */
	private List<Different> diffGroup(String groupName, List<Task> list) throws IOException {
		final List<Different> diffs = new ArrayList<>();
		for (Task task : list) {
			Different different = new Different();
			different.setPath(task.getName());
			different.setGroupName(groupName);
			different.setType(0);
			diffTask(task, different);
			if (different.getMessage() != null) {
				diffs.add(different);
			}
		}

		Path path = new File(StaticValue.GROUP_FILE, groupName).toPath();

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			// 在访问子目录前触发该方法
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (!dir.toFile().isHidden()) {
					Different different = diffFile(groupName, dir.toFile());
					if (different != null) {
						diffs.add(different);
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (!file.toFile().isHidden()) {
					Different different = diffFile(groupName, file.toFile());
					if (different != null) {
						diffs.add(different);
					}
				}
				return FileVisitResult.CONTINUE;
			}

			private Different diffFile(String groupName, File file) {

				String md5ByFile = null;
				if (file.isDirectory()) {
					md5ByFile = "";
				} else {
					MD5Util.getMd5ByFile(file);
				}

				File root = new File(StaticValue.GROUP_FILE, groupName);

				StringBuilder sb = new StringBuilder();
				while (!(file.equals(root))) {
					sb.insert(0, file.getName());
					sb.insert(0, "/");
					file = file.getParentFile();
				}
				;

				sb.insert(0, "/" + groupName + "/file");

				Different different = new Different();
				different.setGroupName(groupName);
				different.setPath(sb.toString());
				different.setType(1);

				try {
					byte[] bytes = getData2ZK(GROUP_PATH + sb.toString());

					if (!md5ByFile.equals(new String(bytes))) {
						different.addMessage("文件内容不一致");
					}

				} catch (Exception e) {
					different.addMessage("文件在集群中不存在");
				}


				if (different.getMessage() == null) {
					return null;
				} else {
					return different;
				}
			}
		});

		return diffs;

	}

	/**
	 * 比较两个task是否一致
	 *
	 * @param task
	 */
	private void diffTask(Task task, Different different) {
		try {

			byte[] bytes = getData2ZK(GROUP_PATH + GROUP_PATH + "/" + task.getGroupName() + "/" + task.getName());

			if (bytes == null) {
				different.addMessage("集群中不存在此Task");
				return;
			}

			Task cluster = JSONObject.parseObject(bytes, Task.class);

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
			if (Objects.equals(task.getDescription(), cluster.getDescription())) {
				different.addMessage("简介不一致");
			}
		} catch (Exception e) {
			e.printStackTrace();
			different.addMessage(e.getMessage());
		}

	}

	/**
	 * 如果这个group在集群中没有则，添加到集群
	 *
	 * @param groupName
	 * @param list
	 * @throws IOException
	 */
	private void addGroup2Cluster(String groupName, List<Task> list) throws IOException {
		list.stream().forEach(t -> {
			try {
				addTask(t);
			} catch (Exception e) {
				e.printStackTrace(); //这个异常很麻烦
				LOG.error("add task to cluster err ：" + t.getGroupName() + "/" + t.getName(), e);
			}
		});

		Path path = new File(StaticValue.GROUP_FILE, groupName).toPath();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			// 在访问子目录前触发该方法
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				File file = dir.toFile();
				if (!file.canRead() || file.isHidden()) {
					return FileVisitResult.CONTINUE;
				}
				try {
					addFile(groupName, file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				File file = path.toFile();
				if (!file.canRead() || file.isHidden()) {
					return FileVisitResult.CONTINUE;
				}
				try {
					addFile(groupName, file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
