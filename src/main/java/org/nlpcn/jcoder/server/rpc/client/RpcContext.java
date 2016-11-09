package org.nlpcn.jcoder.server.rpc.client;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class RpcContext {

	private ChannelHandlerContext chContext;

	private RpcRequest req;

	private RpcResponse rep;

	private Map<Object, Object> map = null;

	public RpcContext(ChannelHandlerContext ctx) {
		this.chContext = ctx;
	}

	public ChannelHandlerContext getChContext() {
		return chContext;
	}

	public void setChContext(ChannelHandlerContext chContext) {
		this.chContext = chContext;
	}

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

}
