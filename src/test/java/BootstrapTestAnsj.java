public class BootstrapTestAnsj {
	public static void main(String[] args) throws Exception {
		Bootstrap.main(new String[]{
				"--zk=192.168.31.227:2181",
				"--host=192.168.31.227",
				"--home=jcoder_home",
				"--port=9095"
		});
	}
}
