package org.nlpcn.jcoder.server.rpc.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.server.rpc.RpcRequest;
import org.nlpcn.jcoder.server.rpc.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * rpc client for remote
 * 
 * @author ansj
 *
 */
public class RpcClient {

	private static final Logger LOG = Logger.getLogger(RpcClient.class);

	private final static int PARALLEL = Runtime.getRuntime().availableProcessors() * 2; //cpu number * 2

	/**
	 * result back
	 */
	protected static final ConcurrentHashMap<String, ResultCallBack> CALL_BACK_MAP = new ConcurrentHashMap<String, ResultCallBack>();

	private InetSocketAddress serverAddress = InetSocketAddress.createUnresolved("localhost", 9999);

	private static final EventLoopGroup group = new NioEventLoopGroup(PARALLEL);

	private Bootstrap bootstrap = null;

	public RpcClient() throws InterruptedException {
		connect(false);
	}

	protected volatile int errCount = 0;

	private synchronized void connect(boolean errConn) throws InterruptedException {

		if (errConn && errCount == 0) {
			return;
		}

		LOG.info("conn server " + serverAddress);

		bootstrap = new Bootstrap();

		bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);

		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				pipeline.addLast(new LengthFieldPrepender(4));
				pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
				pipeline.addLast(new ObjectEncoder());
				pipeline.addLast(new ClientHandler());
			}
		});
		channel = bootstrap.connect(serverAddress.getHostString(), serverAddress.getPort()).sync().channel();

		errCount = 0;
	}

	Channel channel = null;

	protected Object proxy(RpcRequest rpcRequest) throws Throwable {

		String messageId = rpcRequest.getMessageId();

		try {
			ResultCallBack callBack = new ResultCallBack(rpcRequest);
			CALL_BACK_MAP.put(messageId, callBack);
			channel.writeAndFlush(rpcRequest);
			Object result = callBack.getResult();
			errCount = 0;
			return result;
		} catch (Exception e) {
			LOG.error(e);
			errCount++;
			System.out.println(errCount + "\t" + this);
			if (errCount > 5) {
				LOG.info(e.getMessage() + " now reconnect server!");
				connect(true);
				LOG.info("reconnect server ok !");
			}
			throw e;
		} finally {
			CALL_BACK_MAP.remove(messageId);
		}

	}

	class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rep) throws Exception {
			ResultCallBack resultCallBack = CALL_BACK_MAP.get(rep.getMessageId());
			resultCallBack.over(rep);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}

	class ResultCallBack {

		private Lock lock = new ReentrantLock();

		private Condition finish = lock.newCondition();

		private RpcResponse response = null;

		private RpcRequest request = null;

		public ResultCallBack(RpcRequest request) {
			this.request = request;
		}

		/**
		 * return value
		 * 
		 * @param rep
		 */
		public void over(RpcResponse rep) {
			try {
				lock.lock();
				this.response = rep;
				finish.signal();
			} finally {
				lock.unlock();
			}
		}

		/**
		 * syn get result if <= 0 not outtime
		 * 
		 * @param time
		 * @return
		 * @throws InterruptedException
		 * @throws TimeoutException
		 */
		public Object getResult() throws InterruptedException, TimeoutException {
			try {
				lock.lock();
				if (response == null) {
					if (request.getTimeout() <= 0) {
						finish.await();
					} else {
						finish.await(request.getTimeout(), TimeUnit.MILLISECONDS);
					}
				}
				if (this.response != null) {
					return this.response.getResult();
				} else {
					throw new TimeoutException(request.getMessageId() + ":" + request.getClassName() + "/" + request.getMethodName() + " time out of : " + request.getTimeout());
				}
			} finally {
				lock.unlock();
			}
		}

	}

}
