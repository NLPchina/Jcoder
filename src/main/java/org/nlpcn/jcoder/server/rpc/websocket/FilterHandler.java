package org.nlpcn.jcoder.server.rpc.websocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.server.rpc.RpcFilter;
import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FilterHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(ExecuteHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest req) throws Exception {

		Task task = TaskService.findTaskByCache(req.getClassName());
		if (task == null) {
			Rpcs.getRep().write(Restful.instance(false, "not find task by " + req.getClassName(), null, 404));
			return;
		}

		Filters classFilters = task.codeInfo().getClassz().getAnnotation(Filters.class);

		ExecuteMethod method = task.codeInfo().getExecuteMethod(req.getMethodName());

		if (method == null) {
			Rpcs.getRep().write(Restful.instance(false, "not find method " + req.getClassName() + " in task " + req.getClassName(), null, 404));
			return;
		}

		Filters methodFilters = method.getMethod().getAnnotation(Filters.class);

		if (classFilters != null || methodFilters != null) {

			if (classFilters != null) {
				for (By by : classFilters.value()) {
					if (by.type().isAssignableFrom(RpcFilter.class)) {
						Restful match = ((RpcFilter) by.type().newInstance()).match(req);
						if (match != null) {
							Rpcs.getRep().write(match);
							return;
						}
					}
				}
			}

			if (methodFilters != null) {
				for (By by : methodFilters.value()) {
					if (by.type().isAssignableFrom(RpcFilter.class)) {
						Restful match = ((RpcFilter) by.type().newInstance()).match(req);
						if (match != null) {
							Rpcs.getRep().write(match);
							return;
						}
					}
				}
			}
		}

		ctx.fireChannelRead(req);
	}

}
