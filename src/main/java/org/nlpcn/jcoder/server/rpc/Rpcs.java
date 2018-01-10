package org.nlpcn.jcoder.server.rpc;

import org.nlpcn.jcoder.server.rpc.domain.RpcContext;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.server.rpc.domain.RpcResponse;

/**
 * 线程池维护 请求的上下文
 * 
 * @author ansj
 *
 */
public class Rpcs {

	protected static final ThreadLocal<RpcContext> RPC_CONTEXT = new ThreadLocal<>();

	public Object getContext(Object key) {
		return getContext().get(key);
	}

	public void put(Object key, Object value) {
		getContext().put(key, value);
	}


	public static RpcRequest getReq() {
		return getContext().getReq();
	}

	public static RpcResponse getRep() {
		return getContext().getRep();
	}

	public static RpcContext getContext() {
		RpcContext context = RPC_CONTEXT.get();
		if (context == null) {
			synchronized (Thread.currentThread()) {
				context = RPC_CONTEXT.get();
				if (context != null) {
					return context;
				}
				context = new RpcContext(null);
				RPC_CONTEXT.set(context);
			}
		}
		return context;
	}

}
