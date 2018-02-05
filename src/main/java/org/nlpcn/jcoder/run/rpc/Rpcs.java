package org.nlpcn.jcoder.run.rpc;

import org.nlpcn.jcoder.run.rpc.domain.RpcContext;
import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.run.rpc.domain.RpcResponse;

/**
 * 线程池维护 请求的上下文
 *
 * @author ansj
 */
public class Rpcs {

	protected static final ThreadLocal<RpcContext> RPC_CONTEXT = new ThreadLocal<>();

	public static RpcResponse getRep() {
		return ctx().getRep();
	}

	public static RpcRequest getReq() {
		return ctx().getReq();
	}

	public static RpcContext ctx() {
		RpcContext context = RPC_CONTEXT.get();
		if (context == null) {
			synchronized (Thread.currentThread()) {
				context = RPC_CONTEXT.get();
				if (context != null) {
					return context;
				}
				context = new RpcContext();
				RPC_CONTEXT.set(context);
			}
		}
		return context;
	}

	public Object getContext(Object key) {
		return ctx().get(key);
	}

	public void put(Object key, Object value) {
		ctx().put(key, value);
	}


}
