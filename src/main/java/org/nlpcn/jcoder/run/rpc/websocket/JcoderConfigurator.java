package org.nlpcn.jcoder.run.rpc.websocket;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.Mvcs;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class JcoderConfigurator extends ServerEndpointConfig.Configurator {

	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		Ioc ioc = Mvcs.getIoc();
		if (ioc == null)
			ioc = Mvcs.ctx().getDefaultIoc();
		return ioc.get(endpointClass);
	}

	public void modifyHandshake(ServerEndpointConfig sec,
	                            HandshakeRequest request,
	                            HandshakeResponse response) {
		super.modifyHandshake(sec, request, response);
		javax.servlet.http.HttpSession session = (javax.servlet.http.HttpSession) request.getHttpSession();
		if (session != null)
			sec.getUserProperties().put("HttpSession", session);
	}
}
