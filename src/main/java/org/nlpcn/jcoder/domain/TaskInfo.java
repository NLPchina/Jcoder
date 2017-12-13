package org.nlpcn.jcoder.domain;

public class TaskInfo {

	private Long id;

	private String name;
	
	private String taskName ;

	private String description;

	private long success;

	private long error;

	private Long groupId;

	private long startTime;

	private Integer status;

	/**
	 * 标记状态 调度 活动 正在停止
	 */
	private String runStatus;

	public TaskInfo() {
	};

	public TaskInfo(String name ,Task task, long startTime) {
		this.id = task.getId();
		this.name = name;
		this.taskName = task.getName() ;
		this.description = task.getDescription();
		this.success = task.success();
		this.error = task.error();
		this.groupId = task.getGroupId();
		this.status = task.getStatus();
		this.runStatus = task.getRunStatus();
		this.startTime = startTime;
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

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
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
	
}
