package org.nlpcn.jcoder.domain;

import org.nlpcn.jcoder.util.StaticValue;

public class TaskInfo {

	private Long id;

	private String name;

	private String taskName;

	private String description;

	private long success;

	private long error;

	private String groupName;

	private long startTime;

	private Integer status;

	private String hostPort;

	/**
	 * 标记状态 调度 活动 正在停止
	 */
	private String runStatus;

	public TaskInfo() {
	}

	;

	public TaskInfo(String name, Task task, long startTime) {
		this.id = task.getId();
		this.name = name;
		this.taskName = task.getName();
		this.description = task.getDescription();
		this.success = task.success();
		this.error = task.error();
		this.groupName = task.getGroupName();
		this.status = task.getStatus();
		this.runStatus = task.getRunStatus();
		this.startTime = startTime;
		this.hostPort = StaticValue.getHostPort();
	}

	public TaskInfo(String name, String taskName, String groupName) {
		this.id = 0L;
		this.name = name;
		this.taskName = taskName;
		this.success = 0;
		this.error = 0;
		this.groupName = groupName;
		this.status = 1;
		this.hostPort = StaticValue.getHostPort();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getSuccess() {
		return success;
	}

	public void setSuccess(long success) {
		this.success = success;
	}

	public long getError() {
		return error;
	}

	public void setError(long error) {
		this.error = error;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
}
