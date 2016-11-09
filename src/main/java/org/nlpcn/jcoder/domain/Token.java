package org.nlpcn.jcoder.domain;

import java.util.Date;

/**
 * token entity
 * 
 * @author Ansj
 *
 */
public class Token {
	
	public static final Token NULL = new Token();

	private String token; 

	private User user;

	private Date expirationTime;

	private Date createTime;

	private long times;

	public long addTimes() {
		return ++times;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getTimes() {
		return times;
	}

}
