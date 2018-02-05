package org.nlpcn.jcoder.run.java;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterNameUtils {
	public static void main(String[] args) {
		Method[] methods = ParameterNameUtils.class.getMethods();

		for (Method method : methods) {
			if (!method.getName().equals("fun")) {
				continue;
			}

			for (Parameter pa : method.getParameters()) {
				System.out.println(pa.getName());
			}

		}
	}

	public void fun(String str, Integer abc) {

	}

}