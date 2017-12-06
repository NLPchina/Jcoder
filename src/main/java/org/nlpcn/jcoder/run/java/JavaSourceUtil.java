package org.nlpcn.jcoder.run.java;

import org.nlpcn.jcoder.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSourceUtil {

	private static final Pattern PAT = Pattern.compile("public\\s+?class\\s+?.*?");

	public static void main(String[] args) throws IOException {
		System.out.println(findClassName("public class Test implements Execute{"));
	}

	public static String findPackage(String sourceCode) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new StringReader(sourceCode));
			String temp = null;

			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				}
				if (temp.trim().startsWith("package ")) {
					temp = temp.split("[\\s+?;]")[1];
					return temp;
				} else {
					return null;
				}
			}

			return null;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public static String findFullName(String sourceCode) throws IOException {
		String pack = findPackage(sourceCode);
		String className = findClassName(sourceCode);
		if (className == null) {
			return null;
		}
		if (pack == null) {
			return className;
		}

		return pack + "." + className;
	}

	/**
	 * 根据一个类返回类名称
	 * 
	 * @param sourceCode
	 * @return 类名称
	 * @throws IOException
	 */
	public static String findClassName(String sourceCode) {

		try (BufferedReader br = new BufferedReader(new StringReader(sourceCode))) {
			String temp = null;

			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				}
				Matcher matcher = PAT.matcher(temp);

				if (matcher.find()) {
					temp = temp.split("[\\s+?;{]")[2];
					return temp;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
