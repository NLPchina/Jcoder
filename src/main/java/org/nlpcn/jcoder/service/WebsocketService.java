package org.nlpcn.jcoder.service;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.nlpcn.jcoder.util.websocket.WebSocketConfigurator;
import org.nlpcn.jcoder.util.websocket.WebSocketStringHandler;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.random.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/console", configurator = WebSocketConfigurator.class)
@IocBean(create = "init", depose = "depose")
public class WebsocketService extends Endpoint {

	protected static final Logger LOG = LoggerFactory.getLogger(WebsocketService.class);

	protected ConcurrentHashMap<String, WebSocketStringHandler> _sessions = new ConcurrentHashMap<>();

	protected ConcurrentHashMap<String, String> sessionIds = new ConcurrentHashMap<String, String>();

	public void init() {
		LOG.info("websocket service starting");
	}

	/**
	 * send all message for online client
	 * 
	 * @param message
	 */
	public void sendMessage(final String message) {
		_sessions.entrySet().parallelStream().forEach(e -> {
			WebSocketStringHandler handler = e.getValue();
			if (handler.getSession().isOpen()) {
				handler.getSession().getAsyncRemote().sendText(message);
			}
		});
	}

	public void onMessage(String channel, String message) {
		LOG.info(channel + ":" + message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		String uu32 = sessionIds.remove(session.getId());
		if (uu32 == null)
			return;
		WebSocketStringHandler handler = _sessions.remove(uu32);
		if (handler != null)
			handler.depose();
	}

	@OnError
	public void onError(Session session, java.lang.Throwable throwable) {
		onClose(session, null);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		String uu32 = R.UU32();
		WebSocketStringHandler handler = new WebSocketStringHandler(uu32, session);
		session.addMessageHandler(handler);
		sessionIds.put(session.getId(), uu32);
		_sessions.put(uu32, handler);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		WebSocketStringHandler handler = getHandler(session.getId());
		if (handler != null)
			handler.onMessage(message);
	}

	protected WebSocketStringHandler getHandler(String sessionId) {
		String uu32 = sessionIds.get(sessionId);
		if (uu32 == null)
			return null;
		return _sessions.get(uu32);
	}

	public void depose() {

	}

	public int count() {
		return _sessions.size();
	}
}