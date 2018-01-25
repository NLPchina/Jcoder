package org.nlpcn.jcoder.util;

import java.util.HashMap;
import java.util.TreeMap;


/**
 * 重新封装了个map，并没有类型校验，使用的时候小心一点点
 */
public class Maps {
	public static <K, V> HashMap<K, V> hash(Object... objects) throws IllegalArgumentException {
		if (objects == null || objects.length % 2 == 1) {
			throw new IllegalArgumentException("The parameter must be even");
		}

		HashMap<K, V> map = new HashMap<>();

		for (int i = 0; i < objects.length; i += 2) {
			map.put((K) objects[i], (V) objects[i + 1]);
		}

		return map;
	}

	public static <K, V> TreeMap<K, V> tree(Object... objects) throws IllegalArgumentException {
		if (objects == null || objects.length % 2 == 1) {
			throw new IllegalArgumentException("The parameter must be even");
		}

		TreeMap<K, V> map = new TreeMap<>();

		for (int i = 0; i < objects.length; i += 2) {
			map.put((K) objects[i], (V) objects[i + 1]);
		}

		return map;
	}

}
