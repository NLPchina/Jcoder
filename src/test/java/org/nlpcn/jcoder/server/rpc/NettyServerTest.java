package org.nlpcn.jcoder.server.rpc;

import java.util.stream.IntStream;

import org.junit.Test;

public class NettyServerTest {

	@Test
	public void startServerTest() throws Exception {
		RpcServer.startServer(9999);
	}

	@Test
	public void clientTest() throws Throwable {

		TestService ts = new TestService();

		long start = System.currentTimeMillis() ;
		
		
		IntStream.range(0, 1000000).parallel().forEach(i ->{
			try {
				if(i%100==0){
					System.out.println(i);
				}
				ts.method("aaa", 20) ;
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");		
		System.out.println(System.currentTimeMillis()-start);
		
		
	}

}
