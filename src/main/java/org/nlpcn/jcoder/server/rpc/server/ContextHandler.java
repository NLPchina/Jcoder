package org.nlpcn.jcoder.server.rpc.server;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
import org.nlpcn.jcoder.server.rpc.client.RpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 增加 context 到 线程的上下文中
 * 
 * @author ansj
 *
 */
public class ContextHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = Logger.getLogger(ContextHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (!(msg instanceof RpcRequest)) {
			return;
		}

		RpcRequest request = (RpcRequest) msg;

		RpcContext rpcContext = new RpcContext(ctx);

		rpcContext.setReq(request);

		RpcResponse rep = new RpcResponse(request.getMessageId());

		rpcContext.setRep(rep);

		Rpcs.setContext(rpcContext);

		super.channelRead(ctx, msg);
	}

}
