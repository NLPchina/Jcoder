package org.nlpcn.jcoder.run.rpc.domain;

import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nlpcn.jcoder.util.Restful;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class RpcResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageId;

	public RpcResponse(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void write(Restful restful) {
		write(JSONObject.toJSONString(restful));
	}

	public void write(String str) {
		Rpcs.ctx().getSession().getAsyncRemote().sendText(str);
	}

	public void write(byte[] bytes) throws ExecutionException, InterruptedException {
		Rpcs.ctx().getSession().getAsyncRemote().sendBinary(ByteBuffer.wrap(bytes));
	}


}
