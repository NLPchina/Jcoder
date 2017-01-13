package org.nlpcn.jcoder.server.rpc.client;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {

	private Class<?> genericClass;

	public RpcEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if (genericClass.isInstance(msg)) {
			byte[] data = null;
			if (RpcContext.Json.equals(Rpcs.getContext().getReturnType())) {
				data = JSONObject.toJSONBytes(msg);
			} else {
				data = SerializationUtil.serialize(msg);
			}
			out.writeInt(data.length);
			out.writeBytes(data);
		} else if (msg instanceof String) {
			System.out.println(msg);
			byte[] data = ((String) msg).getBytes("utf-8");
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}
}