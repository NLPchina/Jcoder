package org.nlpcn.jcoder.server.rpc.client;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {

	private static final Charset CHARSET = Charset.forName("utf-8");;

	private Class<?> genericClass;

	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		if (byteBuf.readableBytes() < 4) {
			return;
		}
		byteBuf.markReaderIndex();
		int dataLength = byteBuf.readInt();
		if (dataLength < 0) {
			channelHandlerContext.close();
		}
		if (byteBuf.readableBytes() < dataLength) {
			byteBuf.resetReaderIndex();
		}
		byte[] data = new byte[dataLength];
		byteBuf.readBytes(data);

		Object obj = null;

		if (data[0] == 'J' && data[1] == 's' && data[2] == 'o' && data[3] == 'n') {
			obj = JSONObject.toJavaObject((JSON) JSONObject.parse(data, 4, data.length-4, CHARSET.newDecoder()), genericClass);
		} else if (data[0] == 'F' && data[1] == 'i' && data[2] == 'l' && data[3] == 'e') {
			RpcRequest rpcRequest = new RpcRequest();
			rpcRequest.setClassName(VFile.VFILE_LOCAL);
			rpcRequest.setMethodName(VFile.VFILE_LOCAL);
			rpcRequest.setArguments(new Object[] { data });
			obj = rpcRequest;
		} else {
			obj = SerializationUtil.deserialize(data, genericClass);
		}

		list.add(obj);
	}
}