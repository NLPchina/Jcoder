package org.nlpcn.jcoder.run.mvc.cache;

import java.util.Arrays;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.json.Json;

import com.alibaba.fastjson.JSON;

public class Args {

	private Object[] args;

	public Args(Object[] args) {
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public static Args create(Object[] args) {
		return new Args(args);
	}

	@Override
	public int hashCode() {
		return Objects.hash(args);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Args)) {
			return false;
		}

		Object[] temp = ((Args) obj).getArgs();

		if (temp.length != args.length) {
			return false;
		}

		for (int i = 0; i < temp.length; i++) {

			if (Objects.deepEquals(args[i], temp[i])) {
				continue;
			}
			if (jsonEqual(args[i], temp[i])) {
				continue;
			}

			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return Arrays.toString(args);
	}

	private boolean jsonEqual(Object o1, Object o2) {

		// in deepequals , must not be null at the same time
		if (o1 == null || o2 == null) {
			return false;
		}

		if (!o1.getClass().equals(o2.getClass())) {
			return false;
		}

		if (o1 instanceof ServletRequest) {
			return true;
		}

		if (o1 instanceof ServletResponse) {
			return true;
		}

		if (o1 instanceof HttpSession) {
			return true;
		}

		if (o1 instanceof ServletContext) {
			return true;
		}

		return Json.toJson(o1).equals(JSON.toJSON(o2));
	}

	public static void main(String[] args) {
		Object temp = new Object[0];

		System.out.println(temp instanceof Object[]);
	}
}
