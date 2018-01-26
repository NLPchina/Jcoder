package org.nlpcn.jcoder.run.rpc.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.CodeInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.run.rpc.RpcFilter;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nlpcn.jcoder.run.rpc.domain.RpcContext;
import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.run.rpc.domain.RpcResponse;
import org.nlpcn.jcoder.run.rpc.domain.RpcUser;
import org.nlpcn.jcoder.run.rpc.service.SessionService;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api", configurator = JcoderConfigurator.class)
@IocBean
public class ApiWebsocket extends Endpoint {

	private static final Logger LOG = LoggerFactory.getLogger(ApiWebsocket.class);

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		LOG.info("{} onClose , reson:{} ", session.getId(), closeReason);
		SessionService.remove(session.getId());

	}

	@OnError
	public void onError(Session session, java.lang.Throwable throwable) {
		LOG.info(session.getId() + " error ", throwable);
		onClose(session, null);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		LOG.info("{} onOpen ", session.getId());
		SessionService.add(new RpcUser(null, session));
	}


	/**
	 * api执行接口，rpc websocket协议
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws InstantiationException, IllegalAccessException {
		RpcRequest request = context(message, session); //构建请求

		if (filter(request)) {
			String threadName = request.getGroupName() + "@" + request.getClassName() + "@" + request.getMethodName() + "@RPC" + request.getMessageId() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
			try {
				try {
					executeTask(request, threadName);
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
	}


	/**
	 * 具体的执行一个task
	 */
	private void executeTask(RpcRequest request, String threadName) {

		Restful restful = new Restful();

		try {

			ApiActionInvoker invoker = StaticValue.MAPPING.getOrCreateByUrl(request.getGroupName(), request.getClassName(), request.getMethodName());

			if (invoker == null) {
				throw new ApiException(404, "not find api in mapping");
			}

			Task task = TaskService.findTaskByCache(request.getGroupName(), request.getClassName());

			if (task == null) {
				throw new ApiException(404, "not find api by name " + request.getClassName() + " in mapping");
			}

			CodeInfo.ExecuteMethod method = task.codeInfo().getExecuteMethod(request.getMethodName());

			if (method == null) {
				throw new ApiException(404, "not find api " + request.getClassName() + " by method name +" + request.getMethodName() + "+ in mapping");
			}

			ApiMethodInvokeProcessor invokeProcessor = invoker.getChain().getInvokeProcessor();

			if (method.isRpc()) {
				JSON arguments = request.getArguments();

				Object[] params;
				if (arguments instanceof JSONObject) {
					params = TaskService.map2Args((Map<String, Object>) arguments, method.getMethod());
				} else if (arguments instanceof JSONArray) {
					params = ((JSONArray) arguments).toArray();
				} else {
					params = new Object[0];
				}

				Object result = invokeProcessor.executeByCache(task, method.getMethod(), params);
				if (result instanceof Restful) {
					restful = (Restful) result;
				} else {
					restful.setObj(result);
				}
			} else {
				restful.setOk(false);
				restful.setMessage("server err : request " + request.getClassName() + "/" + request.getMethodName() + " not a rpc api");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			restful.setOk(false);
			restful.setMessage("server err :" + e.getMessage());
		}


		if (Rpcs.getReq().isDebug()) {
			restful.debug();
		}

		Rpcs.getRep().write(restful);
	}

	/**
	 * 收集一些必要的信息
	 */
	private RpcRequest context(String message, Session session) {
		RpcContext rpcContext = Rpcs.ctx();
		RpcRequest request = JSONObject.parseObject(message, RpcRequest.class);
		rpcContext.setSession(session);
		rpcContext.setTook(System.currentTimeMillis()); //设置请求时间

		rpcContext.setGroupName(request.getGroupName());
		rpcContext.setReq(request);
		rpcContext.setRep(new RpcResponse(request.getMessageId()));

		return request;

	}

	/**
	 * 进行一些必要的过滤
	 */
	public boolean filter(RpcRequest req) throws IllegalAccessException, InstantiationException {

		Task task = TaskService.findTaskByCache(req.getGroupName(), req.getClassName());
		if (task == null) {
			Rpcs.getRep().write(Restful.instance(false, "not find task by " + req.getClassName(), null, 404));
			return false;
		}

		if (task.codeInfo().getClassz() == null) { //class not compile
			new JavaRunner(task).compile();
		}

		Filters classFilters = task.codeInfo().getClassz().getAnnotation(Filters.class);

		CodeInfo.ExecuteMethod method = task.codeInfo().getExecuteMethod(req.getMethodName());

		if (method == null) {
			Rpcs.getRep().write(Restful.instance(false, "not find method " + req.getClassName() + " in task " + req.getClassName(), null, 404));
			return false;
		}

		Filters methodFilters = method.getMethod().getAnnotation(Filters.class);

		if (classFilters != null || methodFilters != null) {

			if (classFilters != null) {
				for (By by : classFilters.value()) {
					if (by.type().isAssignableFrom(RpcFilter.class)) {
						Restful match = ((RpcFilter) by.type().newInstance()).match(req);
						if (match != null) {
							Rpcs.getRep().write(match);
							return false;
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
							return false;
						}
					}
				}
			}
		}

		return true;
	}

}
