package org.nlpcn.jcoder.server.rpc.domain;

import java.io.Serializable;

import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.util.Restful;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

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
		Rpcs.getContext().getChContext().channel().writeAndFlush(new TextWebSocketFrame(str)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	public void write(byte[] bytes) {
		Rpcs.getContext().getChContext().channel().writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)))
				.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	public static void write(Channel channel, Restful restful) {
		write(channel, JSONObject.toJSONString(restful));
	}

	public static void write(Channel channel, String str) {
		channel.writeAndFlush(new TextWebSocketFrame(str)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	public static void write(Channel channel, byte[] bytes) {
		channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes))).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

}
