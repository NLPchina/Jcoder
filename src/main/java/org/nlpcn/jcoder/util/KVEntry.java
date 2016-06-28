package org.nlpcn.jcoder.util;

/**
 * 简单的二值封装的接口体，源于java的恶心
 * 
 * @author ansj
 * @param <K>
 * 
 */
public class KVEntry<K, V extends Comparable<V>> implements Comparable<V> {
	private K key;
	private V value;

	public KVEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	public V setValue(V value) {
		// TODO Auto-generated method stub
		return this.value = value;
	}

	@Override
	public int compareTo(V o) {
		return this.value.compareTo(o);
	}

	@Override
	public String toString() {
		return this.key+":"+this.value ;
	}
	
	

}
