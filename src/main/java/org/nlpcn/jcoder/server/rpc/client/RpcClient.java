package org.nlpcn.jcoder.server.rpc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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

/**
 * rpc client for remote,目前采用单例唯一,未来可能做连接池或者其他方式
 * 
 * @author ansj
 *
 */
public class RpcClient {

	private final int parallel = Runtime.getRuntime().availableProcessors() * 2; // cpu number * 2

	protected final ConcurrentHashMap<String, ResultCallBack> callBackMap = new ConcurrentHashMap<String, ResultCallBack>();

	private InetSocketAddress serverAddress = null;

	private EventLoopGroup group = new NioEventLoopGroup(parallel);

	private Bootstrap bootstrap = null;

	private Channel channel = null;

	private Channel fileChannel = null;

	/**
	 * 单例模式
	 */
	private static RpcClient instance = new RpcClient();

	/**
	 * 连续错误5次以上进行重连
	 */
	protected volatile int errCount = 0;

	/**
	 * 连接到服务器
	 * 
	 * @param serverAddress
	 * @return
	 * @throws InterruptedException
	 */
	public static RpcClient connect(InetSocketAddress serverAddress) throws InterruptedException {
		instance.serverAddress = serverAddress;
		instance.disconnect();
		instance.connect(false);
		return instance;
	}

	public static RpcClient connect(String host, int port) throws InterruptedException {
		instance.serverAddress = InetSocketAddress.createUnresolved(host, port);
		instance.disconnect();
		instance.connect(false);
		return instance;
	}

	public static void shutdown() {
		RpcClient.instance._shutdown();
	}

	/**
	 * 获得对象,在此之前对象必须初始化,就是调用静态方法的connect() ;
	 * 
	 * @return
	 */
	public static RpcClient getInstance() {
		return instance;
	}

	private RpcClient() {
	}

	/**
	 * 一个假的构造方法
	 * 
	 * @throws InterruptedException
	 */
	public RpcClient(String host, int port) throws InterruptedException {
		connect(host, port);
	}

	/**
	 * 又一个假的构造方法
	 * 
	 * @throws InterruptedException
	 */
	public RpcClient(InetSocketAddress address) throws InterruptedException {
		connect(address);
	}

	public synchronized void disconnect() {
		if (channel != null) {
			channel.disconnect();
		}
	}

	private void _shutdown() {
		disconnect();
		group.shutdownGracefully();
	}

	private synchronized void connect(boolean errConn) throws InterruptedException {

		if (errConn && errCount == 0) {
			return;
		}

		try {
			if (group != null && group.isShuttingDown()) {
				group = new NioEventLoopGroup(parallel);
			}

			bootstrap = new Bootstrap();

			bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);

			bootstrap.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
					pipeline.addLast(new LengthFieldPrepender(4));
					pipeline.addLast(new RpcDecoder(RpcResponse.class));
					pipeline.addLast(new RpcDecoder(RpcResponse.class));
					pipeline.addLast(new RpcEncoder(RpcRequest.class));
					pipeline.addLast(new ClientHandler());
				}
			});
			channel = bootstrap.connect(serverAddress.getHostString(), serverAddress.getPort()).sync().channel();

			fileChannel = bootstrap.connect(serverAddress.getHostString(), serverAddress.getPort()).sync().channel();

			channel.closeFuture();

			errCount = 0;
		} catch (Exception e) {
			if (group != null) {
				group.shutdownGracefully();
			}
			throw e;
		}

	}

	public Object proxy(RpcRequest rpcRequest) throws Throwable {
		String messageId = rpcRequest.getMessageId();

		try {
			ResultCallBack callBack = new ResultCallBack(rpcRequest);
			callBackMap.put(messageId, callBack);
			channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE).sync();
			Object result = null;
			if (rpcRequest.isSyn()) {
				result = callBack.getResult();
			}
			errCount = 0;
			return result;
		} catch (Exception e) {
			errCount++;
			if (errCount > 5) {
				connect(true);
			}
			throw e;
		} finally {
			callBackMap.remove(messageId);
		}
	}

	class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rep) throws Exception {
			if (rep.isFile()) {
				write2Server(ctx, rep);
			} else {
				ResultCallBack resultCallBack = callBackMap.get(rep.getMessageId());
				resultCallBack.over(rep);
			}
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelActive(ctx);
		}

		/**
		 * write a stream to server
		 * 
		 * @param ctx
		 * @param rep
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void write2Server(ChannelHandlerContext ctx, RpcResponse rep) throws IOException, InterruptedException {
			VFile file = (VFile) rep.getResult();

			if (!file.check()) {
				RpcRequest rpcRequest = new RpcRequest(file.getId(), VFile.VFILE_SERVER, VFile.VFILE_SERVER, true, false, 0, new Object[] { "verification err" });
				fileChannel.writeAndFlush(rpcRequest).sync().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
				return;
			}

			if (file.isFile()) {
				writeFromFile(file);
			} else {
				writeFromStream(file);
			}

		}

		/**
		 * 从一个流中读取
		 * 
		 * @param file
		 * @throws InterruptedException
		 * @throws IOException
		 */
		private void writeFromStream(VFile file) throws InterruptedException, IOException {

			byte[] bytes = null;

			try {
				byte[] b = new byte[file.getLen()];

				InputStream inputStream = VFile.STREAM_MAP.get(file.getId());

				int len = inputStream.read(b);

				if (len <= 0) {
					bytes = null;
					InputStream remove = VFile.STREAM_MAP.remove(file.getId());
					if (remove != null) {
						remove.close();
					}
				} else if (len == b.length) {
					bytes = b;
				} else {
					bytes = Arrays.copyOfRange(b, 0, len);
				}

				if (bytes != null) {
					file.setOff(file.getOff() + bytes.length);
				}

				RpcRequest rpcRequest = new RpcRequest(file.getId(), VFile.VFILE_CLIENT, VFile.VFILE_CLIENT, true, false, 0, new Object[] { bytes });

				fileChannel.writeAndFlush(rpcRequest).sync().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

			} catch (Exception e) {
				try {
					RpcRequest rpcRequest = new RpcRequest(file.getId(), VFile.VFILE_CLIENT, VFile.VFILE_CLIENT, true, false, 0, new Object[] { e.getMessage() });
					fileChannel.writeAndFlush(rpcRequest).sync().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
				} finally {
					InputStream remove = VFile.STREAM_MAP.remove(file.getId());
					if (remove != null) {
						remove.close();
					}
				}

			}
		}

		/**
		 * 从文件中读取
		 * 
		 * @param file
		 * @throws InterruptedException
		 * @throws IOException
		 */
		private void writeFromFile(VFile file) throws InterruptedException, IOException {
			if (VFile.STREAM_MAP.contains(file.getId())) {
				writeFromStream(file);
			} else {
				FileInputStream fileInputStream = new FileInputStream(new File(file.getClientLocalPath()));
				fileInputStream.skip(file.getOff());
				VFile.STREAM_MAP.put(file.getId(), fileInputStream);
				writeFromStream(file);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
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
		 * @throws ServerException
		 */
		public Object getResult() throws InterruptedException, TimeoutException, ServerException {
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
					if (response.getError() != null) {
						throw new ServerException(response.getError());
					}
					return this.response.getResult();
				} else {
					throw new TimeoutException(request.getMessageId() + ":" + request.getClassName() + "/" + request.getMethodName() + " time out of : " + request.getTimeout());
				}
			} finally {
				lock.unlock();
			}
		}

	}

	public Channel getChannel() {
		return channel;
	}

}
