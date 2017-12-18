package org.nlpcn.jcoder.domain;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Table("groups")
public class Group {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column("create_time")
    private Date createTime;

    private int fileNum ;

    private int taskNum ;

    private long fileLength ;

    private List<HostGroup> hosts ;

    private List<Map<String,Object>> users;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public List<HostGroup> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostGroup> hosts) {
        this.hosts = hosts;
    }

    public List<Map<String, Object>> getUsers() {
		return users;
	}

	public void setUsers(List<Map<String, Object>> users) {
		this.users = users;
	}

    public int getFileNum() {
        return fileNum;
    }

    public void setFileNum(int fileNum) {
        this.fileNum = fileNum;
    }


    @Override
    public String toString() {
	return "Group [id=" + id + ", name=" + name + ", description="
		+ description + ", createTime=" + createTime + "]";
    }
}
