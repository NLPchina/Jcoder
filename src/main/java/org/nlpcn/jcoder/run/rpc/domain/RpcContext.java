package org.nlpcn.jcoder.run.rpc.domain;

import javax.websocket.Session;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RpcContext {

	public static final int OBJ = 0, JSON = 1, FILE = 2;

	private Map<Object, Object> map = null;

	private String groupName;

	private Session session;

	private RpcRequest req;

	private RpcResponse rep;

	/**
	 * 單位是納秒
	 */
	private long took;


	public void put(Object key, Object value) {
		if (map == null) {
			map = new HashMap<>();
		}
		map.put(key, value);
	}

	public Object get(Object key) {
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public long getTook() {
		return took;
	}

	public void setTook(long took) {
		this.took = took;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public RpcRequest getReq() {
		return req;
	}

	public void setReq(RpcRequest req) {
		this.req = req;
	}

	public RpcResponse getRep() {
		return rep;
	}

	public void setRep(RpcResponse rep) {
		this.rep = rep;
	}

	public String localAddress() {
		return ((InetSocketAddress) session.getUserProperties().get("javax.websocket.endpoint.localAddress")).getHostName();
	}

	public String remoteAddress() {
		return ((InetSocketAddress) session.getUserProperties().get("javax.websocket.endpoint.remoteAddress")).getHostName();
	}
}
