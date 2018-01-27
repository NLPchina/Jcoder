package org.nlpcn.jcoder.domain;

public class GroupGit {

	private String groupName;

	private String uri;

	private String userName;

	private String password;

	/**
	 * 多少毫秒pull一次，0不自动
	 */
	private int autoPullMillis;


	private String token;

	/**
	 * 最后一次pull的时间
	 */
	private long lastPullTime;


}
