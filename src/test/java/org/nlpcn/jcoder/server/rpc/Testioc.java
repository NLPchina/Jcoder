//package org.nlpcn.jcoder.server.rpc;
//
//import org.apache.log4j.Logger;
//import org.nlpcn.jcoder.domain.User;
//import org.nlpcn.jcoder.run.annotation.Execute;
//import org.nutz.ioc.loader.annotation.Inject;
//
//public class Testioc {
//
//	static {
//		System.out.println("----------compilaaae----------------");
//	}
//
//
//	@Inject 
//	private Logger log;
//
//	@Execute
//	public User searchData(String title, int aa) {
//		User user = new User() ; ;
//		user.setName(title);
//		return user ;
//	}
//
//}
