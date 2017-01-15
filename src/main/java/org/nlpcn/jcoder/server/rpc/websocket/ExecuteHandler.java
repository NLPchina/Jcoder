package org.nlpcn.jcoder.server.rpc.websocket;

import java.util.Date;

import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.server.rpc.ChannelManager;
import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.server.rpc.domain.RpcContext;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.server.rpc.domain.RpcResponse;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * execute request
 * 
 * @author ansj
 *
 */
public class ExecuteHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(ExecuteHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		LOG.error(cause.getMessage());
		ctx.close();
		ChannelManager.remove(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		LOG.error("close");
		ChannelManager.remove(ctx.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

		String threadName = request.getClassName() + "@" + request.getMethodName() + "@RPC" + request.getMessageId() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");

		try {
			try {
				executeTask(ctx, request, threadName);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				try {
					writeError(ctx, request, e.getMessage());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				throw e;
			}
		} finally {
			ThreadManager.removeActionIfOver(threadName);
		}
	}

	/**
	 * 写错误流到服务器端
	 * 
	 * @param ctx
	 * @param request
	 * @param message
	 */
	private void writeError(ChannelHandlerContext ctx, RpcRequest request, String message) {
		RpcResponse response = Rpcs.getRep();
		if (response == null) {
			response = new RpcResponse(request.getMessageId());
		}
		response.setOk(false);
		response.setMessage("server has err : " + message);
		ctx.channel().writeAndFlush(encoder(response)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * 具体的执行一个task
	 * 
	 * @param ctx
	 * @param request
	 * @param threadName
	 */
	private void executeTask(ChannelHandlerContext ctx, RpcRequest request, String threadName) {

		RpcResponse response = Rpcs.getRep();

		try {

			ApiActionInvoker invoker = StaticValue.MAPPING.getOrCreateByUrl(request.getClassName(), request.getMethodName());

			if (invoker == null) {
				throw new ApiException(404, "not find api in mapping");
			}

			Task task = TaskService.findTaskByCache(request.getClassName());

			if (task == null) {
				throw new ApiException(404, "not find api by name " + request.getClassName() + " in mapping");
			}
			
			ExecuteMethod method = task.codeInfo().getExecuteMethod(request.getMethodName());

			if (method == null) {
				throw new ApiException(404, "not find api " + request.getClassName() + " by method name +" + request.getMethodName() + "+ in mapping");
			}

			ApiMethodInvokeProcessor invokeProcessor = invoker.getChain().getInvokeProcessor();

			if (method.isRpc()) {
				Object result = invokeProcessor.executeByCache(task, method.getMethod(), request.getArguments());
				response.setObj(result);
			} else {
				response.setOk(false);
				response.setMessage("server err : request " + request.getClassName() + "/" + request.getMethodName() + " not a rpc api");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			response.setOk(false);
			response.setMessage("server err :" + e.getMessage());
		}
		ctx.channel().writeAndFlush(encoder(response)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	private TextWebSocketFrame encoder(RpcResponse rep) {
		return new TextWebSocketFrame(JSONObject.toJSONString(rep));
	}
}
