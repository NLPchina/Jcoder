import org.junit.Test;

public class BootstrapTestAnsj {

	//	private static String host = "192.168.31.107";
//	private static String host = "192.168.31.227";
//	private static String zk = "192.168.31.227:2181";
//	private static String zk = "192.168.3.137:2181|jcoder:jcoder";

//	private static String zk = "192.168.10.9:2181|jcoder:jcoder";
//	private static String zk = "192.168.3.66:2181";

	private static String zk = "192.168.10.3:2181|jcoder:jcoder";
	private static String host = "192.168.3.66";

//	private static String zk = "127.0.0.1:2181";
//	private static String host = "127.0.0.1";

	@Test
	public void test1() throws Exception {
		int port = 9095;
		Bootstrap.main(new String[]{
				//"--zk=" + zk,
				//"--host=" + host,
				"--home=jcoder_home_" + port,
				"--port=" + port,
				"--token=www.infcn.com.cn",
				"--testing=true"
		});
	}

//	@Test
//	public void test2() throws Exception {
//		int port = 9098;
//		Bootstrap.main(new String[]{
//				"--zk=" + zk,
//				"--host=" + host,
//				"--home=jcoder_home_" + port,
//				"--port=" + port
//		});
//	}
//
//	@Test
//	public void test3() throws Exception {
//		int port = 9101;
//		Bootstrap.main(new String[]{
//				"--zk=" + zk,
//				"--host=" + host,
//				"--home=jcoder_home_" + port,
//				"--port=" + port
//		});
//	}
}
