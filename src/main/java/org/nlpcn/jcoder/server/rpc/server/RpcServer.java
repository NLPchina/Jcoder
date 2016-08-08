package org.nlpcn.jcoder.server.rpc.server;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.server.rpc.client.RpcDecoder;
import org.nlpcn.jcoder.server.rpc.client.RpcEncoder;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
import org.nlpcn.jcoder.server.rpc.client.RpcResponse;

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

/**
 * rpc server
 * 
 * @author ansj
 *
 */
public class RpcServer {
	
	private static final Logger LOG = Logger.getLogger(RpcServer.class) ;

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
		
		LOG.info("to start rpc server ");
		
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
					pipeline.addLast(new RpcDecoder(RpcRequest.class));
					pipeline.addLast(new RpcEncoder(RpcResponse.class));
					pipeline.addLast(new ContextHandler());
					pipeline.addLast(new ExecuteHandler());
				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture future = bootstrap.bind(port).sync();

			future.channel().closeFuture();
			
			LOG.info("start rpc server ok");

		} finally {
		
		}
	}

	public static void stopServer() {
		LOG.info("to stop rpc server");
		if (!worker.isShutdown()) {
			worker.shutdownGracefully();
		}

		if (!boss.isShutdown()) {
			boss.shutdownGracefully();
		}
		LOG.info("stop rpc server ok");
	}

}
