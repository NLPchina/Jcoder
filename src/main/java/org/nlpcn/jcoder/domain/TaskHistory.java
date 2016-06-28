package org.nlpcn.jcoder.domain;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

@Table("task_history")
public class TaskHistory {

	@Id
	private Long id;

	@Column("task_id")
	private Long taskId;

	@Column
	private String name;

	@Column
	private String description;

	@Column("schedule_str")
	private String scheduleStr;

	@Column
	private String code;

	@Column("code_type")
	private String codeType;

	@Column("group_id")
	private Long groupId;

	@Column("create_user")
	private String createUser;

	@Column("update_user")
	private String updateUser;

	@Column("create_time")
	private Date createTime;

	@Column("update_time")
	private Date updateTime;


	@Column("version")
	private String version;

	@Column("type")
	private Integer type;

	@Column("status")
	private Integer status;

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

	public String getScheduleStr() {
		return scheduleStr;
	}

	public void setScheduleStr(String scheduleStr) {
		this.scheduleStr = scheduleStr;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	
	public TaskHistory() {
	}

	public TaskHistory(Task t) {
		this.taskId = t.getId();
		this.code = t.getCode();
		this.codeType = t.getCodeType();
		this.createTime = t.getCreateTime();
		this.createUser = t.getCreateUser();
		this.description = t.getDescription();
		this.groupId = t.getGroupId();
		this.name = t.getName();
		this.scheduleStr = t.getScheduleStr();
		this.status = t.getStatus();
		this.type = t.getType();
		this.updateTime = t.getUpdateTime();
		this.updateUser = t.getUpdateUser();
		this.version = t.getVersion();
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	
}
