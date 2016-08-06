package org.nlpcn.jcoder.server.rpc.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nlpcn.jcoder.domain.User;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

public class SerializationUtilTest {

	@Test
	public void test() {

		long start = System.currentTimeMillis();

		RpcResponse rpcResponse = new RpcResponse("idid") ;
		ArrayList<? extends Object> newArrayList = Lists.newArrayList("ansj",123) ;
		rpcResponse.setResult(rpcResponse);

		for (int i = 0; i < 10000000; i++) {
			
			byte[] jsonBytes = JSONObject.toJSONBytes(rpcResponse) ;
			
			rpcResponse = JSONObject.parseObject(jsonBytes, RpcResponse.class) ;
			
		}

		System.out.println(System.currentTimeMillis()-start);

	}

}
