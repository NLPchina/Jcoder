package org.nlpcn.jcoder.server.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * execute request
 * 
 * @author ansj
 *
 */
public class ExecuteHandler extends SimpleChannelInboundHandler<RpcRequest> {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("close");
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

		RpcResponse response = new RpcResponse(request.getMessageId());

		response.setResult("ok");

		ctx.channel().writeAndFlush(response);

	}
}
