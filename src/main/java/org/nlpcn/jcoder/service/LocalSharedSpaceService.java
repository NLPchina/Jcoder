package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 单机版开放空间
 * Created by Ansj on 08/12/2017.
 */
public class LocalSharedSpaceService extends SharedSpaceService {

	private LinkedBlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();


	private Cache<String, Token> tokenCache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();

	/**
	 * {groupName , className , path:存放路径 , type :0 普通任务 ， 1 while 任务  2.all任务}
	 * 增加一个任务到任务队列
	 */
	public void add2TaskQueue(String groupName, String className, String scheduleStr) throws Exception {
		taskQueue.add(className) ;
	}

	@Override
	public Token getToken(String key) {
		return tokenCache.getIfPresent(key);
	}

	@Override
	public void regToken(Token token) {
		tokenCache.put(token.getToken(), token);
	}

	@Override
	public Token removeToken(String key) {
		Token token = tokenCache.getIfPresent(key);
		if (token != null) {
			tokenCache.invalidate(key);
		}
		return token;
	}

	@Override
	public void removeMapping(String path) throws Exception {

	}

	@Override
	public void addMapping(String path) throws Exception {

	}

	@Override
	public String host(String groupName, String path) {
		throw new RuntimeException("not need this method in local model") ;
	}

	@Override
	public String host(String groupName, String className, String mehtodName) {
		throw new RuntimeException("not need this method in local model") ;
	}


	/**
	 * 增加一个task到集群中，如果冲突返回false
	 *
	 * @param task
	 * @return 是否冲突
	 */
	@Override
	public void addTask(Task task) throws Exception {
	}

	/**
	 * 增加一个task到集群中，如果冲突返回false
	 *
	 * @param groupName
	 * @Param file
	 *
	 * @return 是否冲突
	 */
	@Override
	public void addFile(String groupName, File file) throws Exception {
	}


	@Override
	public SharedSpaceService init() throws Exception {
		return this ;
	}

	@Override
	public void release() throws Exception {
	}
}
