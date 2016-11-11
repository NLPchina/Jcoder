package org.nlpcn.jcoder.controller;

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
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/console", configurator = WebSocketConfigurator.class)
@IocBean(create = "init", depose = "depose")
public class ConsoleAction extends Endpoint {

	protected static final Logger LOG = LoggerFactory.getLogger(ConsoleAction.class);

	protected ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

	public void init() {
		LOG.info("websocket service starting");
	}

	/**
	 * send all message for online client
	 * 
	 * @param message
	 */
	public void sendMessage(final String message) {
		sessions.entrySet().parallelStream().forEach(e -> {
			Session session = e.getValue();
			if (session.isOpen()) {
				session.getAsyncRemote().sendText(message);
			}
		});
	}


	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session.getId());
	}

	@OnError
	public void onError(Session session, java.lang.Throwable throwable) {
		onClose(session, null);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		sessions.put(session.getId(), session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		LOG.info(session.getId() + ":" + message);
	}

	public void depose() {

	}
	
	public int count() {
		return sessions.size();
	}
}