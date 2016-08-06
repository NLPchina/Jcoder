package cn.com.infcn.pdfconvert;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;

public class Testioc {

	static {
		System.out.println("----------compilaaae----------------");
	}

	@Inject
	private User user;

	@Inject 
	private Logger log;

	@Execute
	public Object searchData(String title) {
		user.setName(title);
		return user;
	}

}
