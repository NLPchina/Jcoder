package org.nlpcn.jcoder.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ApiDoc {

	protected List<ApiDoc> sub;
	protected Map<String, String> attr;
	private String name;
	private String content;

	public ApiDoc(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<ApiDoc> getSub() {
		return sub;
	}

	public void setSub(List<ApiDoc> sub) {
		this.sub = sub;
	}

	public Map<String, String> getAttr() {
		return attr;
	}

	public void setAttr(Map<String, String> attr) {
		this.attr = attr;
	}

	public void addAttr(String key, String value) {
		if (this.attr == null) {
			this.attr = new HashMap<>();
		}
		attr.put(key, value);
	}

	public abstract ApiDoc createSubDoc(String name);

}
