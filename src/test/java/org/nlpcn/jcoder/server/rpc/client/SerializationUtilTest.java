package org.nlpcn.jcoder.server.rpc.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.junit.Test;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.User;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class SerializationUtilTest {

	@Test
	public void testResponse() {

		long start = System.currentTimeMillis();

		RpcResponse rpcResponse = new RpcResponse("idid");
		ArrayList<? extends Object> newArrayList = Lists.newArrayList("ansj", 123);
		rpcResponse.setResult(newArrayList);

		byte[] serializer = SerializationUtil.serialize(rpcResponse);

		RpcResponse deserializer = SerializationUtil.deserialize(serializer, RpcResponse.class);

		System.out.println(deserializer.getMessageId());

		System.out.println(deserializer.getResult());

		String job = JSONObject.toJSONString(new File("build.gradle"));

		System.out.println(job);

	}

	@Test
	public void testRequest() {

		long start = System.currentTimeMillis();

		RpcRequest req = new RpcRequest("123", "testIoc", "searchData", true, false, 10000, new Object[] { 123 });

		byte[] serializer = SerializationUtil.serialize(req);

		req = SerializationUtil.deserialize(serializer, RpcRequest.class);

		String job = JSONObject.toJSONString(req);

		System.out.println(job);

	}

	@Test
	public void testAll() {
		ArrayList<Object> newArrayList = Lists.newArrayList("ansj", 123);

		byte[] serializer = SerializationUtil.serialize(newArrayList);

		List<Object> list = (List<Object>) SerializationUtil.deserialize(serializer, ArrayList.class);

		System.out.println(list);

		ArrayList<String> stringList = Lists.newArrayList("ansj", "ansj");

		serializer = SerializationUtil.serializeList(stringList);

		ArrayList<String> stringlist = (ArrayList<String>) SerializationUtil.deserialize(serializer, List.class);

		System.out.println(stringlist);

		User user = new User();

		user.setName("ansj");

		serializer = SerializationUtil.serialize(user);

		user = SerializationUtil.deserialize(serializer, User.class);

		System.out.println(JSONArray.toJSON(user));

		List<User> lists = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			User user1 = new User();

			user1.setName("ansj" + i);

			lists.add(user1);
		}

		serializer = SerializationUtil.serialize(lists);

		lists = SerializationUtil.deserialize(serializer, List.class);

		for (User user2 : lists) {
			System.out.println(user2.getName());
		}

	}

	@Test
	public void testFile() {

		VFile vFile = new VFile(new File("src/main/java"));

		byte[] serializer = SerializationUtil.serialize(vFile);

		vFile = SerializationUtil.deserialize(serializer, VFile.class);

		printVFile(vFile);

	}

	private void printVFile(VFile vFile) {
		System.out.println(vFile.getName());

		if (vFile.getListVFiles() != null) {
			for (VFile v : vFile.getListVFiles()) {
				printVFile(v);
			}
		}
	}

	@Test
	public void schemaTest() {
		Schema<User> schema2 = (Schema<User>) RuntimeSchema.getSchema(User.class);
		long start = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++) {
			Schema<User> schema = (Schema<User>) RuntimeSchema.getSchema(User.class);
		}

		System.out.println(System.currentTimeMillis() - start);
	}

	@Test
	public void serializationTest() {

		Task task = new Task();

		task.setMessage("aaaaaaaaaaa");

		ArrayList<Integer> lists = Lists.newArrayList(1, 3, 4, 3, 24, 324, 24, 32);

		task.codeInfo().setJavaObject(lists);

		@SuppressWarnings("unchecked")
		Schema<Task> schema = (Schema<Task>) RuntimeSchema.getSchema(task.getClass());
		LinkedBuffer buffer = LinkedBuffer.allocate(256);
		byte[] protostuff = null;
		try {
			protostuff = ProtostuffIOUtil.toByteArray(task, schema, buffer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			buffer.clear();
		}

		System.out.println(protostuff.length);
		Task taskNew = new Task() ;
		GraphIOUtil.mergeFrom(protostuff, taskNew, schema);
		
		System.out.println(taskNew.codeInfo().getJavaObject());
	}

}
