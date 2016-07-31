package org.nlpcn.jcoder.server.rpc;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.chainsaw.Main;
import org.nlpcn.jcoder.server.rpc.client.RpcClient;
import org.nutz.lang.Mirror;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class TestService extends RpcClient {

	public TestService() throws InterruptedException {
		super();
	}

	public String method(String name, int age) throws Throwable {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		Class<?> clz = this.getClass();

		Object[] arguments = new Object[] { name, age };

		Method method = Mirror.me(clz).findMethod(methodName, arguments);

		String messageId = UUID.randomUUID().toString();

		RpcRequest req = new RpcRequest(messageId, clz, method, arguments);
		
		return (String) proxy(req);
	}

	static Lock lock = new ReentrantLock();

	public static void main(String[] args) {
		
		Condition newCondition = lock.newCondition() ;
		
		new Thread( ()->{
			while(true){
				lock.lock();
				System.out.println("in r1");
				try {
					newCondition.await(10000,TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("in out r1");
				lock.unlock(); 
			}
		} ).start();
		
		new Thread( ()->{
			while(true){
				lock.lock();
				System.out.println("in r2");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("in out r2");
				lock.unlock(); 
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} ).start(); 
	}
}
