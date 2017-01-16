package org.nlpcn.jcoder.server.rpc.websocket;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import org.nlpcn.jcoder.server.rpc.domain.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public final class WebSocketServer {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);

	protected static final String WEBSOCKET_PATH = "/ws";

	private final static int PARALLEL = Runtime.getRuntime().availableProcessors() * 2;

	private static EventLoopGroup bossGroup = null;

	private static EventLoopGroup workerGroup = null;

	public static void startServer(int port) throws Exception {

		LOG.info("to start websocket server ");

		ThreadFactory threadRpcFactory = new NamedThreadFactory("WebSocketThread ThreadFactory");
		bossGroup = new NioEventLoopGroup(PARALLEL);
		workerGroup = new NioEventLoopGroup(PARALLEL, threadRpcFactory, SelectorProvider.provider());

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new HttpServerCodec());
				pipeline.addLast(new HttpObjectAggregator(65536));
				pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
				pipeline.addLast(new ContextHandler());
				pipeline.addLast(new FilterHandler());
				pipeline.addLast(new ExecuteHandler());
			}
			
		}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture future = bootstrap.bind(port).sync();

		future.channel().closeFuture();
		LOG.info("start websocket server ok");
	}

	public static void stopServer() {
		LOG.info("to websocket rpc server");
		if (!workerGroup.isShutdown()) {
			workerGroup.shutdownGracefully();
		}

		if (!bossGroup.isShutdown()) {
			bossGroup.shutdownGracefully();
		}
		LOG.info("stop websocket server ok");
	}

}
