package org.nlpcn.jcoder.run.rpc.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nlpcn.jcoder.run.rpc.domain.RpcUser;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ansj on 24/01/2018.
 */
public class ZookeeperRoomServiceTest {

	private RoomService roomService = null;

	private String roomName = "test";

	private String userName = "123";

	@Before
	public void setUp() throws Exception {
//		roomService = new MemoryRoomService() ;
		ZookeeperDao zookeeperDao = new ZookeeperDao("192.168.3.137:2181").start();
		roomService = new ZookeeperRoomService(zookeeperDao);
		Session session = new Session() {
			@Override
			public WebSocketContainer getContainer() {
				return null;
			}

			@Override
			public void addMessageHandler(MessageHandler handler) throws IllegalStateException {

			}

			@Override
			public Set<MessageHandler> getMessageHandlers() {
				return null;
			}

			@Override
			public void removeMessageHandler(MessageHandler handler) {

			}

			@Override
			public String getProtocolVersion() {
				return null;
			}

			@Override
			public String getNegotiatedSubprotocol() {
				return null;
			}

			@Override
			public List<Extension> getNegotiatedExtensions() {
				return null;
			}

			@Override
			public boolean isSecure() {
				return false;
			}

			@Override
			public boolean isOpen() {
				return false;
			}

			@Override
			public long getMaxIdleTimeout() {
				return 0;
			}

			@Override
			public void setMaxIdleTimeout(long milliseconds) {

			}

			@Override
			public int getMaxBinaryMessageBufferSize() {
				return 0;
			}

			@Override
			public void setMaxBinaryMessageBufferSize(int length) {

			}

			@Override
			public int getMaxTextMessageBufferSize() {
				return 0;
			}

			@Override
			public void setMaxTextMessageBufferSize(int length) {

			}

			@Override
			public RemoteEndpoint.Async getAsyncRemote() {
				return null;
			}

			@Override
			public RemoteEndpoint.Basic getBasicRemote() {
				return null;
			}

			@Override
			public String getId() {
				return userName;
			}

			@Override
			public void close() throws IOException {

			}

			@Override
			public void close(CloseReason closeReason) throws IOException {

			}

			@Override
			public URI getRequestURI() {
				return null;
			}

			@Override
			public Map<String, List<String>> getRequestParameterMap() {
				return null;
			}

			@Override
			public String getQueryString() {
				return null;
			}

			@Override
			public Map<String, String> getPathParameters() {
				return null;
			}

			@Override
			public Map<String, Object> getUserProperties() {
				return null;
			}

			@Override
			public Principal getUserPrincipal() {
				return null;
			}

			@Override
			public Set<Session> getOpenSessions() {
				return null;
			}
		};

		SessionService.add(new RpcUser(null, session));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		roomService.join(roomName, userName);
		Set<String> test = roomService.ids(roomName);
		System.out.println(test);
		roomService.sendMessage(roomName, null);
		roomService.left(roomName, userName);
		test = roomService.ids(roomName);
		System.out.println(test);
		roomService.dropRoom(roomName, true);
	}

}