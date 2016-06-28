package org.nlpcn.jcoder.run.java;


import java.util.Date;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.execute.DefaultExecute;
import org.nlpcn.jcoder.util.DateUtils;
import org.nutz.ioc.loader.annotation.Inject;

public class Test2 {
	
	@Inject
	private Logger log ;

	@DefaultExecute
	public void execute() throws InterruptedException {
		log.info(DateUtils.formatDate(new Date(), DateUtils.SDF_FORMAT));
	}

}
