package org.nlpcn.jcoder.domain;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;

@Table("user_group")
public class UserGroup {

	@Id
	private Long id;
	@Column("user_id")
	private Long userId;
	@Column("group_id")
	private Long groupId;
	@Column("create_time")
	private Date createTime;
	@Column("auth")
	private Integer auth; //1 查看 2 编辑

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public Integer getAuth() {
		return auth;
	}

	public void setAuth(Integer auth) {
		this.auth = auth;
	}

	@Override
	public String toString() {
		return "UserGroup [id=" + id + ", userId=" + userId + ", groupId=" + groupId + ", createTime=" + createTime + "]";
	}
}
