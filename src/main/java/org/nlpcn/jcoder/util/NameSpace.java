package org.nlpcn.jcoder.util;

import java.util.HashMap;
import java.util.Map;

public class NameSpace {

	public static Map<String,Map<Object,Object>> SPACE = new HashMap<>() ;

	/**
	 * 增加在自己的命名空間增加
	 * @param key
	 * @param value
	 * @return 
	 */
	public static void put(Object key,Object value){
		String name = Thread.currentThread().getName() ;
		SPACE.get(name).put(key, value) ;
	}
	
	/**
	 * 從自己的命名空間取得變量
	 * @param key
	 * @return
	 */
	public static Object get(Object key){
		String name = Thread.currentThread().getName() ;
		return SPACE.get(name).get(key) ;
	}
}
