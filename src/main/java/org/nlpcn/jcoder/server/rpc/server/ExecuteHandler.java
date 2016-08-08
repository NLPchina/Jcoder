package org.nlpcn.jcoder.server.rpc.server;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
import org.nlpcn.jcoder.server.rpc.client.RpcResponse;
import org.nlpcn.jcoder.server.rpc.client.VFile;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.StaticValue;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * execute request
 * 
 * @author ansj
 *
 */
public class ExecuteHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger LOG = Logger.getLogger(ExecuteHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		LOG.error(cause.getMessage());
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

		if (VFile.VFILE.equals(request.getClassName()) && VFile.VFILE.equals(request.getMethodName())) {
			VFile vfile = (VFile) VFile.BUFFERED_MAP.remove(request.getMessageId());
			if(vfile==null){
				throw new IOException("vfile form bufferd map is null") ;
			}
			try {
				Object result = request.getArguments()[0];

				if (result == null) {
					vfile.addBytes(VFile.END_BYTE);
				} else if (result instanceof byte[]) {
					vfile.addBytes((byte[]) result);
				} else {
					throw new IOException(result.toString());
				}
			} catch (Exception e) {
				LOG.error(e);
				vfile.addBytes(VFile.ERR_BYTE);
				throw e;
			}
		} else {
			String threadName = request.getClassName() + "@" + request.getMethodName() + "@RPC" + request.getMessageId() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
			try {
				executeTask(ctx, request, threadName);
			} catch (Exception e) {
				LOG.error(e);
				try {
					RpcResponse response = Rpcs.getRep();
					if (response == null) {
						response = new RpcResponse(request.getMessageId());
					}
					response.setError("server has err : " + e.getMessage());
					ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				throw e;
			} finally {
				ThreadManager.removeActionIfOver(threadName);
			}
		}
	}

	/**
	 * 具体的执行一个task
	 * 
	 * @param ctx
	 * @param request
	 * @param threadName
	 */
	private void executeTask(ChannelHandlerContext ctx, RpcRequest request, String threadName) {
		ThreadManager.add2ActionTask(threadName, Thread.currentThread());

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
				if (request.isJsonStr()) {
					response.setResult(JSON.toJSONString(result));
				} else {
					response.setResult(result);
				}

			} else {
				response.setError("server err : request " + request.getClassName() + "/" + request.getMethodName() + " not a rpc api");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e);
			response.setError("server err :" + e.getMessage());
		}
		ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}
}
