package org.nlpcn.jcoder.util.websocket;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Basic Echo Client Socket
 */
@WebSocket(maxTextMessageSize = 1024 * 1024)
public class SimpleEchoSocket {

	private final CountDownLatch closeLatch;

	public boolean stop = false;

	private Session session;

	public SimpleEchoSocket() {
		this.closeLatch = new CountDownLatch(1);
	}

	public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
		return this.closeLatch.await(duration, unit);
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
		stop = true;
	}

	public void close() {
		try {
			if (session != null)
				session.close(StatusCode.NORMAL, "I'm done");
			this.session = null;
			this.closeLatch.countDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.printf("Got connect: %s%n", session);
		this.session = session;
	}

	@OnWebSocketMessage
	public void onMessage(String msg) {
		System.out.println(msg);
		if (WebSocketUtil.STOP.equals(msg)) {
			close();
		}
	}

	/**
	 * 发送一条消息
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void sendMessage(String msg) throws IOException {
		session.getRemote().sendString(msg);
	}
}