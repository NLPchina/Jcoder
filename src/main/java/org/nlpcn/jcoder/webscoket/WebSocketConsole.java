package org.nlpcn.jcoder.webscoket;
/*
 * Copyright 2016 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.atmosphere.config.service.WebSocketHandlerService;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.util.SimpleBroadcaster;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.websocket.WebSocketStreamingHandlerAdapter;

/**
 * WebSocket on conosle
 * 
 * @author Ansj
 */
@WebSocketHandlerService(path = "/console", broadcaster = SimpleBroadcaster.class, atmosphereConfig = {
		"org.atmosphere.websocket.WebSocketProtocol=org.atmosphere.websocket.protocol.StreamingHttpProtocol" })
public class WebSocketConsole extends WebSocketStreamingHandlerAdapter {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private static WebSocket webSocket = null;

	@Override
	public void onOpen(WebSocket webSocket) throws IOException {

		WebSocketConsole.webSocket = webSocket;

		webSocket.resource().addEventListener(new WebSocketEventListenerAdapter() {

			@Override
			public void onDisconnect(AtmosphereResourceEvent event) {
				super.onDisconnect(event);
				COUNTER.decrementAndGet();
			}

			@Override
			public void onConnect(WebSocketEvent event) {
				super.onConnect(event);
				COUNTER.incrementAndGet();
			}
		});
	}

	public static void sendMessage(String message) {
		if(webSocket==null){
			return ;
		}
		webSocket.broadcast(message);
	}

	public static int count() {
		return COUNTER.get();
	}

}
