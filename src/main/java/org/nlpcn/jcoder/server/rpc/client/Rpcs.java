package org.nlpcn.jcoder.server.rpc.client;

/**
 * 线程池维护 请求的上下文
 * 
 * @author ansj
 *
 */
public class Rpcs {

	protected static final ThreadLocal<RpcContext> RPC_CONTEXT = new ThreadLocal<>();

	public static void setContext(RpcContext rpcCtx) {
		RPC_CONTEXT.set(rpcCtx);
	}

	public static RpcContext getContext() {
		return RPC_CONTEXT.get();
	}

	public Object getContext(Object key) {
		return getContextOutOfNull().get(key);
	}

	public void getContext(Object key, Object value) {
		getContext().put(key, value);
	}

	public static RpcRequest getReq() {
		return getContextOutOfNull().getReq();
	}

	public static RpcResponse getRep() {
		return getContextOutOfNull().getRep();
	}

	private static RpcContext getContextOutOfNull() {
		RpcContext context = getContext();
		if (context == null) {
			return new RpcContext(null);
		}
		return context;
	}

}
