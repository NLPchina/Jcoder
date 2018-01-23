package org.nlpcn.jcoder.run.java;

import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;

public class Test {
	public static void main(String[] args) {
		RpcRequest req = new RpcRequest() ;

		req.setGroupName("InfcnNlp");
		req.setClassName("ApiTest2");
		req.setMethodName("test");
		req.setDebug(true);
		req.setMessageId("123");

		System.out.println(JSONObject.toJSONString(req));
	}
}
