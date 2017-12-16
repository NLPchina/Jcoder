package org.nlpcn.jcoder.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决冲突的操作
 */
public class Different implements Serializable {

	private String groupName;
	private String path;
	private Integer type;//0 task , 1.file
	private String message;

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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}


	public String getMessage() {
		return message;
	}

	public void addMessage(String message) {
		if (this.message == null) {
			this.message = message;
		} else {
			this.message = this.message + "\t" + message;
		}
	}

	@Override
	public String toString() {
		return "Different{" +
				"groupName='" + groupName + '\'' +
				", path='" + path + '\'' +
				", type=" + type +
				", message='" + message + '\'' +
				'}';
	}
}
