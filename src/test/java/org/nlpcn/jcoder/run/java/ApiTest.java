package org.nlpcn.jcoder.run.java;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.Mvcs;

public class ApiTest {

	@Inject
	private Logger log;

	@DefaultExecute
	public String defaultTest() throws InterruptedException {

		log.info(Mvcs.getReq().getServerName().matches("127.0.*"));
		
		return StaticValue.OK;
	}

}
