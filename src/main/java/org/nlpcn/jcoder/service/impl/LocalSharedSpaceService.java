package org.nlpcn.jcoder.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public class LocalSharedSpaceService implements SharedSpaceService {

	private static final Logger LOG = LoggerFactory.getLogger(LocalSharedSpaceService.class);


	@Inject
	private TaskService taskService;

	private LinkedBlockingQueue<Long> taskQueue = new LinkedBlockingQueue<>();


	private static final Map<Long, AtomicLong> taskSuccess = new HashMap<>();

	private static final Map<Long, AtomicLong> taskErr = new HashMap<>();

	private Cache<String, Token> tokenCache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();


	@Override
	public void add2TaskQueue(String name) {
		String[] split = name.split("/");
		Task taskByCache = TaskService.findTaskByCache(split[1]);
		if (taskByCache != null) {
			taskQueue.add(taskByCache.getId());
		} else {
			LOG.warn(name + " not found in cache");
		}
	}

	@Override
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

	@Override
	public long getSuccess(Long id) {
		AtomicLong atomicLong = taskSuccess.get(id);
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
	}

	@Override
	public long getErr(Long id) {
		AtomicLong atomicLong = taskErr.get(id);
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
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
	public Long poll() throws InterruptedException {
		return taskQueue.poll(Integer.MAX_VALUE, TimeUnit.DAYS);
	}
}
