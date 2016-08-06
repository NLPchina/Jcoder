package org.nlpcn.jcoder.server.rpc;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;
import org.nlpcn.jcoder.run.java.Testioc;
import org.nlpcn.jcoder.server.rpc.client.RpcClient;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
import org.nlpcn.jcoder.server.rpc.server.RpcServer;
import org.nutz.http.Http;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.ByteInputStream;

public class NettyServerTest {

	@Test
	public void startServerTest() throws Exception {
//		RpcServer.startServer(8081);
//		Thread.sleep(100000000L);
	}
	
	static AtomicInteger ai = new AtomicInteger(1000000) ;
	
	static AtomicInteger err = new AtomicInteger() ;


	@Test
	public void clientTest() throws Throwable {

//		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
//
//		RpcClient.connect("localhost", 8081);
//
//		Method method = Mirror.me(Testioc.class).findMethod("searchData", new Class[] { String.class });
//		
//
//		Runnable r = () -> {
//			try {
//				RpcRequest req = new RpcRequest(UUID.randomUUID().toString(), Testioc.class, method, true, false, 0, new Object[] { "ansj" });
//				
//				try {
//					RpcClient.getInstance().proxy(req);
////					Http.get("http://localhost:8080/api/Testioc/searchData?title=ansj").getContent() ;;
//				} catch (Exception e) {
//					err.incrementAndGet() ;
//					System.err.println(e);
//				}finally {
//					ai.decrementAndGet() ;
//				}
//				
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//		};
//
//		int m = ai.get();
//
//		long start = System.currentTimeMillis();
//
//		for (int i = 0; i < m; i++) {
//			scheduledThreadPool.execute(r);
//		}
//
//		while (ai.get() > 0) {
//			System.out.println(ai.get());
//			Thread.sleep(100);
//		}
//		
//		System.out.println("err : "+err.get());
//
//		System.out.println(System.currentTimeMillis() - start);
//
//		RpcClient.shutdown();

	}

}
