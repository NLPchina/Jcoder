package org.nlpcn.jcoder.server.rpc.client;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {

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

		Object obj = SerializationUtil.deserialize(data, genericClass);
		list.add(obj);
	}
}