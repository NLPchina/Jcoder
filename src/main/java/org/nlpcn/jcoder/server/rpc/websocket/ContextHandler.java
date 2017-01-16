package org.nlpcn.jcoder.server.rpc.websocket;

import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.server.rpc.domain.RpcContext;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.server.rpc.domain.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * 增加 context 到 线程的上下文中
 * 
 * @author ansj
 *
 */
public class ContextHandler extends SimpleChannelInboundHandler<WebSocketFrame>{

	private static final Logger LOG = LoggerFactory.getLogger(ContextHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

		RpcContext rpcContext = Rpcs.getContext();

		RpcRequest request = JSONObject.toJavaObject((JSON) JSONObject.parse(((TextWebSocketFrame) frame).text()), RpcRequest.class);

		rpcContext.setReq(request);

		rpcContext.setChContext(ctx);
		
		rpcContext.setRep(new RpcResponse(request.getMessageId()));

		super.channelRead(ctx, request);
	}
	
	//{"jsonStr":false,"messageId":"12312","methodName":"execute","syn":true,"arguments":["ansj"],"className":"TestApi","timeout":10000}

	public static void main(String[] args) {
		RpcRequest req = new RpcRequest() ;
		req.setClassName("TestApi");
		req.setMethodName("execute");
		req.setMessageId("12312");
		req.setArguments(new Object[]{"ansj"});
		
		System.out.println(JSONObject.toJSON(req));
	}


}
