package org.nlpcn.jcoder.domain;


import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;

/**
 * 监听目录的操作
 * Created by Ansj on 03/01/2018.
 */
public class Handler {

	private String groupName;

	private String taskName;

	private String path;

	private Type action;

	public Handler(String path, String groupName, String taskName, Type action) {
		this.path = path;
		this.groupName = groupName;
		this.taskName = taskName;
		this.action = action;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Type getAction() {
		return action;
	}

	public void setAction(Type action) {
		this.action = action;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public String toString() {
		return "Handler{" +
				"path='" + path + '\'' +
				", groupName='" + groupName + '\'' +
				", taskName='" + taskName + '\'' +
				", action='" + action + '\'' +
				'}';
	}
}
