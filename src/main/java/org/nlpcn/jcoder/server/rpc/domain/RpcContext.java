package org.nlpcn.jcoder.server.rpc.domain;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.nlpcn.jcoder.server.rpc.Rpcs;

import io.netty.channel.ChannelHandlerContext;

public class RpcContext {

	public static final int OBJ = 0, JSON = 1, FILE = 2;

	private ChannelHandlerContext chContext;

	private RpcRequest req;

	private RpcResponse rep;

	private Map<Object, Object> map = null;

	private String groupName ;

	private int type;

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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String remoteAddress() {
		return ((InetSocketAddress) getChContext().channel().remoteAddress()).getAddress().getHostAddress() ;
	}
	
	public String localAddress() {
		return ((InetSocketAddress) getChContext().channel().localAddress()).getAddress().getHostAddress() ;
	}

}
