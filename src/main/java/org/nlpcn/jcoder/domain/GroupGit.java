package org.nlpcn.jcoder.domain;

import java.util.Date;

public class GroupGit {

	private String groupName;

	private String uri;

	private String userName;

	private String password;

	private String branch;

	private String md5 ;

	/**
	 * 多少毫秒pull一次，0不自动
	 */
	private int autoPullMillis;


	private String token;

	/**
	 * 最后一次pull的时间
	 */
	private Date lastPullTime;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getAutoPullMillis() {
		return autoPullMillis;
	}

	public void setAutoPullMillis(int autoPullMillis) {
		this.autoPullMillis = autoPullMillis;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getLastPullTime() {
		return lastPullTime;
	}

	public void setLastPullTime(Date lastPullTime) {
		this.lastPullTime = lastPullTime;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
