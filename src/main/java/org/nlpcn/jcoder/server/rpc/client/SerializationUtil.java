package org.nlpcn.jcoder.server.rpc.client;

import java.util.Collection;

import org.objenesis.ObjenesisStd;

import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class SerializationUtil {

	private static final ObjenesisStd OBJ = new ObjenesisStd(true);

	public static <T> byte[] serialize(T obj) {
		if (obj instanceof Collection) {
			return serializeList((Collection<?>) obj);
		} else {
			return serializeObj(obj);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] paramArrayOfByte, Class<T> targetClass) {
		if (Collection.class.isAssignableFrom(targetClass)) {
			return (T) deserializeList(paramArrayOfByte);
		} else {
			return deserializeObj(paramArrayOfByte, targetClass);
		}
	}

	public static <T> byte[] serializeObj(T obj) {
		if (obj == null) {
			throw new RuntimeException("serializeObj(" + obj + ") err!");
		}
		@SuppressWarnings("unchecked")
		Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(obj.getClass());
		LinkedBuffer buffer = LinkedBuffer.allocate(256);
		byte[] protostuff = null;
		try {
			protostuff = GraphIOUtil.toByteArray(obj, schema, buffer);
		} catch (Exception e) {
			throw new RuntimeException("serialize(" + obj.getClass() + ") obj (" + obj + ") error", e);
		} finally {
			buffer.clear();
		}
		return protostuff;
	}

	public static <T> T deserializeObj(byte[] paramArrayOfByte, Class<T> targetClass) {
		if (paramArrayOfByte == null || paramArrayOfByte.length == 0) {
			throw new RuntimeException("deserialize bytearray is empty!");
		}
		T instance = OBJ.newInstance(targetClass);
		Schema<T> schema = RuntimeSchema.getSchema(targetClass);
		GraphIOUtil.mergeFrom(paramArrayOfByte, instance, schema);
		return instance;
	}

	public static <T> byte[] serializeList(Collection<T> objList) {
		if (objList == null) {
			throw new RuntimeException("serializeList(" + objList + ") is null or empty!");
		}
		return serializeObj(new Collection2Obj<T>(objList));

	}

	public static Collection<?> deserializeList(byte[] paramArrayOfByte) {
		if (paramArrayOfByte == null || paramArrayOfByte.length == 0) {
			throw new RuntimeException("deserializeList bytearray is empty!");
		}

		return deserializeObj(paramArrayOfByte, Collection2Obj.class).getList();

	}

}

class Collection2Obj<T> {

	private Collection<T> list = null;

	public Collection2Obj() {

	}

	public Collection2Obj(Collection<T> collection) {
		this.list = collection;
	}

	public Collection<T> getList() {
		return list;
	}

	public void setList(Collection<T> list) {
		this.list = list;
	}

}