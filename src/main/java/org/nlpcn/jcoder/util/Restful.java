package org.nlpcn.jcoder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nutz.http.Response;

public class Restful {

	public static Restful ok() {
		return new Restful(true);
	}

	public static Restful fail() {
		return new Restful(false);
	}

	private boolean ok = true;

	private String message;

	private Object obj;

	/**
	 * server ipPort
	 */
	private String server;

	/**
	 * use time
	 */
	private Long took;

	/**
	 * 执行的版本
	 */
	private String version;

	private int code = ApiException.OK;

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public <T> T getObj() {
		return (T) obj;
	}

	public JSONObject obj2JsonObject() {
		if (obj == null || obj instanceof JSONObject) return (JSONObject) obj;
		if (obj instanceof String) {
			return JSONObject.parseObject((String) obj);
		} else {
			return (JSONObject) JSONObject.toJSON(obj);
		}
	}

	public JSONArray obj2JsonArray() {
		if (obj == null || obj instanceof JSONArray) return (JSONArray) obj;
		if (obj instanceof String) {
			return JSONObject.parseArray((String) obj);
		} else {
			return (JSONArray) JSONObject.toJSON(obj);
		}
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public int code() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Restful() {
	}

	public Restful(boolean ok) {
		this.ok = ok;
	}

	public Restful(boolean ok, String message, Object obj, int code) {
		this.ok = ok;
		this.message = message;
		this.obj = obj;
		this.code = code;
	}

	public Restful(boolean ok, String message, Object obj) {
		this.ok = ok;
		this.message = message;
		this.obj = obj;
	}

	public Restful(boolean ok, String message) {
		this.ok = ok;
		this.message = message;
	}

	public Restful(String message, Object obj) {
		this.message = message;
		this.obj = obj;
	}

	public Restful(Object obj) {
		this.obj = obj;
	}

	public static Restful instance() {
		return new Restful();
	}

	public static Restful instance(boolean ok, String message, Object obj, int code) {
		return new Restful(ok, message, obj, code);
	}

	public static Restful instance(boolean ok, String message, Object obj) {
		return new Restful(ok, message, obj);
	}

	public static Restful instance(String message, Object obj) {
		return new Restful(message, obj);
	}

	public static Restful instance(boolean ok, String message) {
		return new Restful(ok, message);
	}

	/**
	 * @param obj
	 * @return ok true message null obj
	 */
	public static Restful instance(Object obj) {
		return new Restful(obj);
	}

	public Restful msg(String message) {
		this.message = message;
		return this;
	}

	public Restful obj(Object obj) {
		this.obj = obj;
		return this;
	}

	public Restful code(int code) {
		this.code = code;
		return this;
	}

	public Restful ok(boolean ok) {
		this.ok = ok;
		return this;
	}

	/**
	 * debug 方式打印更多的信息
	 *
	 * @return
	 */
	public Restful debug() {

		if (StringUtil.isBlank(this.server)) {
			this.server = StaticValue.getHostPort();
		}

		if (StringUtil.isNotBlank(version)) {
			this.version = StaticValue.VERSION;
		}

		this.took = System.currentTimeMillis() - Rpcs.ctx().getTook();

		return this;
	}

	public String toJsonString() {
		return JSON.toJSONString(this);
	}

	/**
	 * nutz response 转换为 restful对象
	 *
	 * @return
	 */
	public static Restful instance(Response response) {
		Restful restful = JSONObject.parseObject(response.getContent(), Restful.class);
		restful.setCode(response.getStatus());
		return restful;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Long getTook() {
		return took;
	}

	public void setTook(Long took) {
		this.took = took;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
