package org.nlpcn.jcoder.run.rpc.domain;

import org.nlpcn.jcoder.domain.User;

import javax.websocket.Session;


/**
 * Created by Ansj on 24/01/2018.
 */
public class RpcUser {

	private User user;

	private Session session;

	public RpcUser(User user, Session session) {
		this.user = user;
		this.session = session;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
