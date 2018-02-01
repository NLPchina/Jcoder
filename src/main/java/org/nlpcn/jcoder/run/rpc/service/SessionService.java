package org.nlpcn.jcoder.run.rpc.service;

import org.nlpcn.jcoder.run.rpc.domain.RpcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ansj on 24/01/2018.
 */
public class SessionService {

	private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);

	private static final ConcurrentHashMap<String, RpcUser> SESSION_ID_MAP = new ConcurrentHashMap<>();


	/**
	 * get all channel
	 */
	public static Collection<RpcUser> getOnlineSession() {
		return SESSION_ID_MAP.values();
	}

	/**
	 * get channell by clientId
	 */
	public static RpcUser getRpcUser(String id) {
		return SESSION_ID_MAP.get(id);
	}

	/**
	 * remove a channel
	 */
	public static void remove(String sessionId) {
		SESSION_ID_MAP.remove(sessionId);
	}

	/**
	 * add a channel
	 */
	public static void add(RpcUser user) {
		SESSION_ID_MAP.put(user.getSession().getId(), user);
	}
}
