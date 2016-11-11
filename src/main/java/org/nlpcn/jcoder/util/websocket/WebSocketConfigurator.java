package org.nlpcn.jcoder.util.websocket;

import javax.websocket.server.ServerEndpointConfig;

import org.nlpcn.jcoder.util.StaticValue;

public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return StaticValue.getSystemIoc().get(endpointClass);
    }
}