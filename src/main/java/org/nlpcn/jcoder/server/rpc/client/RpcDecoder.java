package org.nlpcn.jcoder.server.rpc.client;

import java.nio.charset.Charset;
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

		int type = byteBuf.readByte();

		byte[] data = new byte[dataLength - 1];
		byteBuf.readBytes(data);

		Object obj = null;

		if (type == RpcContext.OBJ) {
			obj = SerializationUtil.deserialize(data, genericClass);
			Rpcs.getContext().setType(RpcContext.OBJ);
		} else if (type == RpcContext.JSON) {
			obj = JSONObject.toJavaObject((JSON) JSONObject.parse(data, 4, data.length - 4, CHARSET.newDecoder()), genericClass);
			Rpcs.getContext().setType(RpcContext.JSON);
		} else if (type == RpcContext.OBJ) {
			RpcRequest rpcRequest = new RpcRequest();
			rpcRequest.setClassName(VFile.VFILE_LOCAL);
			rpcRequest.setMethodName(VFile.VFILE_LOCAL);
			rpcRequest.setArguments(new Object[] { data });
			obj = rpcRequest;
		}

		list.add(obj);
	}
}