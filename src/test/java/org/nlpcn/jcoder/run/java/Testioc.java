package org.nlpcn.jcoder.run.java;

import java.io.InputStream;

public class Testioc {
	private static final java.util.Map<String, java.lang.reflect.Method> METHOD_MAP = new java.util.HashMap<String, java.lang.reflect.Method>();

	private void __JCODER__init() {
		Class<?> clz = this.getClass();
		java.lang.reflect.Method[] methods = clz.getMethods();
		for (java.lang.reflect.Method method : methods) {
			String name = method.getName();
			if (name.startsWith("__JCODER__")) {
				continue;
			}
			METHOD_MAP.put(name, method);
		}
	}

	public Testioc() {
		__JCODER__init();
	}

	public Testioc(boolean syn, long timeout) {
		__JCODER__init();
		this.__JCODER__syn = syn;
		this.__JCODER__timeout = timeout;
	}

	private boolean __JCODER__syn = true;

	private long __JCODER__timeout = 10000L;

	public void set__JCODER__syn(boolean syn) {
		this.__JCODER__syn = syn;
	}

	public void set__JCODER__timeout(long timeout) {
		this.__JCODER__timeout = timeout;
	}
	public Object searchData(String name) throws Throwable {
		return (Object) org.nlpcn.jcoder.server.rpc.client.RpcClient.getInstance().proxy(new org.nlpcn.jcoder.server.rpc.client.RpcRequest(java.util.UUID.randomUUID().toString(), this.getClass(),
				METHOD_MAP.get("searchData"), __JCODER__syn, false, __JCODER__timeout, new Object[] { name }));
	}
	
	public String searchData__jsonStr(String title) throws Throwable {
		return (String) org.nlpcn.jcoder.server.rpc.client.RpcClient.getInstance().proxy(new org.nlpcn.jcoder.server.rpc.client.RpcRequest(java.util.UUID.randomUUID().toString(), this.getClass(),
				METHOD_MAP.get("searchData"), __JCODER__syn, false, __JCODER__timeout, new Object[] { title }));
	}

}