package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean(create = "init")
public class SharedSpaceService {

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
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<Long, AtomicLong> taskSuccess = new HashMap<>();

	/**
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<Long, AtomicLong> taskErr = new HashMap<>();

	protected ZookeeperDao zookeeperDao;

	// /jcoder/mapping/className/methodName/addr,groupName
	private TreeCache mappingCache;

	private TreeCache tokenCache;


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
		zookeeperDao.getZk().setData().forPath(TASK_PATH + "/" + id, job.getBytes("utf-8"));
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
			zookeeperDao.getZk().setData().forPath(TOKEN_PATH + "/" + token.getToken(), JSONObject.toJSONBytes(token));
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
		zookeeperDao.getZk().create().creatingParentsIfNeeded().forPath(TOKEN_PATH + "/" + token.getToken(), JSONObject.toJSONBytes(token));
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
			zookeeperDao.getZk().delete().forPath(TOKEN_PATH + "/" + key);
		}
		return token;
	}


	/**
	 * 删除一个地址映射
	 *
	 * @param path
	 */
	public void removeMapping(String path) throws Exception {
		zookeeperDao.getZk().delete().forPath(makeMappingPath(path));
	}

	/**
	 * 增加一个mapping到
	 *
	 * @param path
	 */
	public void addMapping(String path) throws Exception {
		String className = path.split("/")[2];
		Task task = TaskService.findTaskByCache(className);

		byte[] data = task.getGroupName().getBytes();
		data = Arrays.copyOf(data, data.length + 1);
		if (StaticValue.IS_SSL) {
			data[data.length - 1] = 1;
		}

		path = makeMappingPath(path);

		if(zookeeperDao.getZk().checkExists().forPath(path)==null){
			zookeeperDao.getZk().create().creatingParentsIfNeeded().forPath(path, data);
		}else {
			zookeeperDao.getZk().setData().forPath(path, data) ;
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
		sb.append(path);
		sb.append("/");
		sb.append(StaticValue.getHost());
		sb.append(":");
		sb.append(StaticValue.PORT);
		return sb.toString();
	}


	public void init() throws Exception {
		this.zookeeperDao = new ZookeeperDao(StaticValue.ZK);

		/**
		 * 监听路由缓存
		 */
		mappingCache = new TreeCache(zookeeperDao.getZk(), MAPPING_PATH).start();

		//token
		tokenCache = new TreeCache(zookeeperDao.getZk(), TOKEN_PATH).start();


		//监听task运行
		NodeCache nodeCache = new NodeCache(zookeeperDao.getZk(), MESSAGE_PATH);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				byte[] data = nodeCache.getCurrentData().getData();
				//TODO: 拿到任务后去执行吧。。心累
			}
		});
		nodeCache.start();
	}
}
