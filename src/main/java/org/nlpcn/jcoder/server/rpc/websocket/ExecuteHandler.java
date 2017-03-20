package org.nlpcn.jcoder.server.rpc.websocket;

import java.util.Date;

import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.server.rpc.ChannelManager;
import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

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
		try {
			Rpcs.getRep().write(Restful.instance(false, ExceptionUtil.printStackTrace(cause)));
		} catch (Exception e) {
			LOG.error("send to client errMessage err :" + e.getMessage());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		LOG.warn("close");
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
					Rpcs.getRep().write(Restful.instance(false, e.getMessage()));
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
	 * 具体的执行一个task
	 * 
	 * @param ctx
	 * @param request
	 * @param threadName
	 */
	private void executeTask(ChannelHandlerContext ctx, RpcRequest request, String threadName) {

		Restful restful = new Restful();

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
				if (result instanceof Restful) {
					restful = (Restful) result;
				} else {
					restful.setObj(result);
				}
			} else {
				restful.setOk(false);
				restful.setMessage("server err : request " + request.getClassName() + "/" + request.getMethodName() + " not a rpc api");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			restful.setOk(false);
			restful.setMessage("server err :" + e.getMessage());
		}

		Rpcs.getRep().write(restful);
	}

}
