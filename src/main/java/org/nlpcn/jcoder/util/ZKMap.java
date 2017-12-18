package org.nlpcn.jcoder.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 对curator treemap 的序列化封装
 * <p>
 * Created by Ansj on 18/12/2017.
 */
public class ZKMap<V> {

	private TreeCache treeCache;

	private CuratorFramework client;

	private String path;

	private Class<?> vClass;

	private ZKMap() {
	}

	public ZKMap(CuratorFramework client, String path, Class<? extends V> c) throws Exception {
		treeCache = new TreeCache(client, path);
		this.path = path;
		this.client = client;
		this.vClass = c;
	}

	/**
	 * 值返回当前目录下一级节点的个数
	 *
	 * @return
	 */
	public int size() {
		return treeCache.getCurrentChildren(path).size();
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}

	public boolean containsKey(String key) {
		return get(key) == null;
	}

	public boolean containsValue(String value) {
		throw new RuntimeException("not support");
	}

	/**
	 * key是抽象路径。不要以/开始
	 *
	 * @param key
	 * @return
	 */
	public V get(Object key) {
		ChildData currentData = treeCache.getCurrentData(path + "/" + key);
		if (currentData == null) {
			return null;
		}
		return JSONObject.parseObject(currentData.getData(), vClass);
	}

	public V put(String key, V value) {
		try {
			V v = get(key);
			if (v != null) {
				client.setData().forPath(path+"/" + key, JSONObject.toJSONBytes(value));
			}else{
				client.create().forPath(path+"/" + key, JSONObject.toJSONBytes(value));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return value;
	}

	public V remove(String key) {
		V v = get(key);
		if (v != null) {
			try {
				client.delete().forPath(path + "/"+ key);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return v;
	}

	public void putAll(Map<String, ? extends V> m) {
		for (Map.Entry<String,? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}

	}

	public void clear() {
		Set<String> ks = keySet();
		for (String k : ks) {
			remove(k);
		}
	}

	public Set<String> keySet() {
		Set<String> collect = treeCache.getCurrentChildren(path).keySet().stream().collect(Collectors.toSet());
		return (Set<String>) collect;
	}

	public Collection<V> values() {
		Collection<ChildData> values = treeCache.getCurrentChildren(path).values();
		List<V> list = new ArrayList<>(values.size());

		for (ChildData data : values) {
			list.add(JSONObject.parseObject(data.getData(), vClass));
		}
		return list;

	}

	public Set<Map.Entry<String, V>> entrySet() {

		Map<String, ChildData> currentChildren = treeCache.getCurrentChildren(path);

		Map<String, V> result = new HashMap<>();

		for (Map.Entry<String, ChildData> entry : currentChildren.entrySet()) {
			result.put(entry.getKey(), JSONObject.parseObject(entry.getValue().getData(), vClass));
		}

		return result.entrySet();
	}

	public ZKMap<V> start() throws Exception {
		treeCache.start();
		return this;
	}

	public void close() {
		treeCache.close();
	}
}
