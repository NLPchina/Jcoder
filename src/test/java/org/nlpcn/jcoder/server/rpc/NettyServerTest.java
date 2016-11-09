//package org.nlpcn.jcoder.server.rpc;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.util.UUID;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.junit.Test;
//import org.nlpcn.jcoder.server.rpc.client.RpcClient;
//import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
//import org.nlpcn.jcoder.server.rpc.client.VFile;
//import org.nlpcn.jcoder.server.rpc.server.RpcServer;
//
//import com.alibaba.fastjson.JSONObject;
//
//import io.netty.channel.ChannelFutureListener;
//
//public class NettyServerTest {
//
//	@Test
//	public void startServerTest() throws Exception {
//		RpcServer.startServer(8081);
//
//	}
//
//	static AtomicInteger ai = new AtomicInteger(1000000);
//
//	static AtomicInteger err = new AtomicInteger();
//
//	@Test
//	public void clientTest() throws Throwable {
//
//		RpcClient.connect("localhost", 8081);
//
//		RpcRequest req = new RpcRequest(UUID.randomUUID().toString(), "ApiTest", "defaultTest", true, false, 0, new Object[] {});
//		
//		System.out.println(JSONObject.toJSONString(req));
//
//		Object proxy = RpcClient.getInstance().proxyJson(req);
//
//		System.out.println(proxy);
//
//		RpcClient.shutdown();
//
//	}
//
//	@Test
//	public void clientThreadTest() throws Throwable {
//
//		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
//
//		RpcClient.connect("localhost", 8081);
//
//		Runnable r = () -> {
//			try {
//				RpcRequest req = new RpcRequest(UUID.randomUUID().toString(), "Testioc", "searchData", true, false, 0, new Object[] { "ansj" });
//
//				try {
//					RpcClient.getInstance().proxy(req);
//					//					Http.get("http://localhost:8080/api/Testioc/searchData?title=ansj").getContent() ;;
//				} catch (Exception e) {
//					err.incrementAndGet();
//					System.err.println(e);
//				} finally {
//					ai.decrementAndGet();
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
//		System.out.println("err : " + err.get());
//
//		System.out.println(System.currentTimeMillis() - start);
//
//		RpcClient.shutdown();
//
//	}
//
//	@Test
//	public void serverVFileTest() throws Throwable {
//
//		RpcClient.connect("localhost", 8081);
//
//		RpcRequest req = new RpcRequest(UUID.randomUUID().toString(), "Testioc", "searchData", true, false, 0,
//				new Object[] { new VFile(new FileInputStream(new File("README.md"))) });
//
//		long start = System.currentTimeMillis();
//
//		try {
//			for (int i = 0; i < 1; i++) {
//				Object proxy = RpcClient.getInstance().proxy(req);
//
//				System.out.println(proxy);
//			}
//
//		} catch (Exception e) {
//			err.incrementAndGet();
//			System.err.println(e);
//		} finally {
//			ai.decrementAndGet();
//		}
//
//		RpcClient.shutdown();
//
//	}
//
//	@Test
//	public void clientFileTest() throws InterruptedException {
//
//		RpcClient.connect("localhost", 8081);
//
//		RpcClient.getInstance().getChannel()
//				.writeAndFlush(new RpcRequest("aaaaaaaaaaaa", VFile.VFILE_CLIENT, VFile.VFILE_CLIENT, true, false, 10000, new Object[] { new byte[] { 1, 2, 3 } }))
//				.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//
//	}
//
//}
