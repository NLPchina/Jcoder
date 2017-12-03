package cn.com.infcn.api.test;

import org.nlpcn.jcoder.run.annotation.Execute;

import com.alibaba.fastjson.JSONObject;

public class TestEtlApi {
	
	@Execute
	public void test(JSONObject doc){
		doc.put("aaa", "ccc") ;
		doc.remove("bbb") ;
		
	}

}
