package org.nlpcn.jcoder.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

	/**
	 * 权限数组。* 为全部
	 */
	private Set<String> authorizes = new HashSet<>();

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

	public void addAuthorize(String authorize) {
		if (!authorize.contains("/")) {
			authorize = authorize + "/*";
		}
		authorizes.add(authorize);
	}

	public void rmAuthorize(String authorize) {
		authorizes.remove(authorize);
	}

	public boolean authorize(String authorize) {
		if (authorizes.size() == 0) {
			return true;
		}
		if (!authorize.contains("/")) {
			authorize = authorize + "/*";
		}
		return authorizes.contains(authorize);
	}

}
