import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BootstrapTestAnsj {

	private static String host = "192.168.31.227";
	private static String zk = "192.168.31.227:2181";

	@Test
	public void test1() throws Exception {
		int port = 9095 ;
		Bootstrap.main(new String[]{
				"--zk=" + zk,
				"--host=" + host,
				"--home=jcoder_home_" + port,
				"--port="+port
		});
	}

	@Test
	public void test2() throws Exception {
		int port = 9097 ;
		Bootstrap.main(new String[]{
				"--zk=" + zk,
				"--host=" + host,
				"--home=jcoder_home_" + port,
				"--port="+port
		});
	}

	@Test
	public void test3() throws Exception {
		int port = 9099 ;
		Bootstrap.main(new String[]{
				"--zk=" + zk,
				"--host=" + host,
				"--home=jcoder_home_" + port,
				"--port="+port
		});
	}
}
