package org.nlpcn.jcoder.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * api 的信息类
 * 
 * @author ansj
 *
 */

public class ClassDoc extends ApiDoc {

	public ClassDoc(String name) {
		super(name);
	}

	private boolean single = true;

	private boolean status = true;

	private String description;

	public boolean isSingle() {
		return single;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public class MethodDoc extends ApiDoc {
		public MethodDoc(String name) {
			super(name);
		}

		private boolean defaultExecute;

		private String retrunContent;

		private Set<String> methods;

		public boolean isDefaultExecute() {
			return defaultExecute;
		}

		public void setDefaultExecute(boolean defaultExecute) {
			this.defaultExecute = defaultExecute;
		}

		public String getRetrunContent() {
			return retrunContent;
		}

		public void setRetrunContent(String retrunContent) {
			this.retrunContent = retrunContent;
		}

		public Set<String> getMethods() {
			if (methods == null) {
				Set<String> temp = new HashSet<>();
				temp.add("*");
				return temp;
			}
			return methods;
		}

		public void setMethods(Set<String> methods) {
			this.methods = methods;
		}

		public void addMethod(String method) {
			if (methods == null) {
				methods = new HashSet<>();
			}
			methods.add(method);
		}

		public class ParamDoc extends ApiDoc {

			private String fieldName;

			private String type;

			public ParamDoc(String name) {
				super(name);
			}

			public String getFieldName() {
				return fieldName;
			}

			public void setFieldName(String fieldName) {
				this.fieldName = fieldName;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			@Override
			public ApiDoc createSubDoc(String name) {
				return null;
			}
		}

		@Override
		public ApiDoc createSubDoc(String name) {
			ParamDoc paramDoc = new ParamDoc(name);
			if (sub == null) {
				sub = new ArrayList<>();
			}
			sub.add(paramDoc);
			return paramDoc;
		}
	}

	@Override
	public ApiDoc createSubDoc(String name) {
		MethodDoc methodDoc = new MethodDoc(name);
		if (sub == null) {
			sub = new ArrayList<>();
		}
		sub.add(methodDoc);
		return methodDoc;
	}

}

abstract class ApiDoc {

	private String name;
	private String content;
	protected List<ApiDoc> sub;
	protected Map<String, String> attr;

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
