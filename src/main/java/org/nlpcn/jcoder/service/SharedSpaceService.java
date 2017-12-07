package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.eclipse.jetty.util.StringUtil;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean(create = "init")
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
	 * 任务路径
	 */
	public static final String TASK_PATH = StaticValue.ZK_ROOT + "/task";


	/**
	 * Token
	 */
	public static final String TOKEN_PATH = StaticValue.ZK_ROOT + "/token";

	/**
	 * Host
	 */
	public static final String HOST_PATH = StaticValue.ZK_ROOT + "/host";

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
		zkDao.getZk().setData().forPath(TASK_PATH + "/" + id, job.getBytes("utf-8"));
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
	 * @param urlPath
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

			String str = new String(currentData.getData());
			String[] split = str.split("_");
			String head = split[0];
			Integer weight = Integer.parseInt(split[1]);
			if (weight <= 0) {
				LOG.info(HOST_PATH + "/" + hostPort + " weight less than zero , so skip");
				continue;
			}
			sum += weight;
			hosts.add(KeyValue.with(head + "://" + hostPort, weight));
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


	public void init() throws Exception {
		this.zkDao = new ZookeeperDao(StaticValue.ZK);

		if (zkDao.getZk().checkExists().forPath(HOST_PATH) == null) {
			zkDao.getZk().createContainers(HOST_PATH);
		}

		//将本机注册到集群
		String info = (StaticValue.IS_SSL ? "https" : "http") + "_" + 100;
		setData2ZKByEphemeral(HOST_PATH+StaticValue.getHostPort(), info.getBytes("utf-8"));

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
	}

	/**
	 * 主机关闭的时候调用,平时不调用
	 */
	public void release() throws Exception {
		//将本机权重设置为0
		String info = (StaticValue.IS_SSL ? "https" : "http") + "_" + 0;
		setData2ZKByEphemeral(HOST_PATH, info.getBytes("utf-8"));
		relaseMapping(StaticValue.getHostPort());
		zkDao.getZk().delete().forPath(HOST_PATH+StaticValue.getHostPort()) ;

		mappingCache.close();
		tokenCache.close();
		hostCache.close();
		zkDao.close();
	}

	/**
	 * 释放删除本机的mpping信息
	 * @param hostPort
	 */
	private void relaseMapping(String hostPort){
		Map<String, ChildData> currentChildren = mappingCache.getCurrentChildren(MAPPING_PATH);
	}
}
