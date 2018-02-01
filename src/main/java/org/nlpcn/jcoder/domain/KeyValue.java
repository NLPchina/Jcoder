package org.nlpcn.jcoder.domain;

public class KeyValue<K, V> {

	private K key;
	private V value;

	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public static <K, V> KeyValue with(K key, V value) {
		return new KeyValue<>(key, value);
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		if (key == null) {
			return String.valueOf(value);
		} else {
			return key + "=" + value;
		}
	}
}
