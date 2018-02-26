package org.nlpcn.jcoder.run.rpc.websocket;

import com.alibaba.fastjson.JSONObject;

import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.run.rpc.domain.RpcUser;
import org.nlpcn.jcoder.run.rpc.service.SessionService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import static org.nlpcn.jcoder.constant.Constants.LOG_ROOM;

@ServerEndpoint(value = "/log", configurator = JcoderConfigurator.class)
@IocBean
public class LogWebsocket extends Endpoint {

	private static final Logger LOG = LoggerFactory.getLogger(LogWebsocket.class);

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		LOG.info("{} onClose , reson:{} ", session.getId(), closeReason);
		SessionService.remove(session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		LOG.info(session.getId() + " error ", throwable);
		onClose(session, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, String.valueOf(throwable.getMessage())));
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		LOG.info("{} onOpen ", session.getId());
		SessionService.add(new RpcUser(null, session));
	}


	/**
	 * @param message {groupName:"_jcoder_log",methodName:"join"}
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws InstantiationException, IllegalAccessException, IOException {

		HttpSession httpSession = (HttpSession) session.getUserProperties().get("HttpSession");

		if (httpSession == null || httpSession.getAttribute(UserConstants.USER) == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "not login"));
			return;
		}

		RpcRequest request = JSONObject.parseObject(message, RpcRequest.class); //构建请求


		String groupName = request.getGroupName();

		String methodName = request.getMethodName();

		if (!LOG_ROOM.equals(groupName)) {
			groupName = LOG_ROOM + "_" + groupName;
		}

		if ("join".equals(methodName)) {
			StaticValue.space().getRoomService().join(groupName, session.getId());
		} else if ("left".equals(methodName)) {
			StaticValue.space().getRoomService().left(groupName, session.getId());
		} else {
			LOG.error("not has method name : it only support `join` , `left` : " + methodName);
		}


	}


}
