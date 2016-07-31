package org.nlpcn.jcoder.server.rpc;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * rpc server
 * 
 * @author ansj
 *
 */
public class RpcServer {

	private static ServerBootstrap bootstrap = null;

	private static EventLoopGroup boss = null;

	private static EventLoopGroup worker = null;

	private final static int PARALLEL = Runtime.getRuntime().availableProcessors() * 2; //cpu number * 2

	private static final int MESSAGE_LENGTH = 4;

	/**
	 * 
	 * @throws Exception
	 */
	public static void startServer(int port) throws Exception {
		ThreadFactory threadRpcFactory = new NamedThreadFactory("NettyRPC ThreadFactory");

		boss = new NioEventLoopGroup(PARALLEL);
		worker = new NioEventLoopGroup(PARALLEL, threadRpcFactory, SelectorProvider.provider());

		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					ChannelPipeline pipeline = socketChannel.pipeline();
					pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MESSAGE_LENGTH, 0, MESSAGE_LENGTH));
					pipeline.addLast(new LengthFieldPrepender(MESSAGE_LENGTH));
					pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
					pipeline.addLast(new ObjectEncoder());
					pipeline.addLast(new ExecuteHandler());
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture future = bootstrap.bind(port).sync();

			future.channel().closeFuture().sync();

		} finally {
			stopServer();
		}
	}

	public static void stopServer() {
		if (!worker.isShutdown()) {
			worker.shutdownGracefully();
		}

		if (!boss.isShutdown()) {
			boss.shutdownGracefully();
		}
	}

}
