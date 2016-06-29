package org.nlpcn.jcoder.run.java;


import java.util.Date;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.util.DateUtils;
import org.nutz.ioc.loader.annotation.Inject;

public class CronTest {
	
	@Inject
	private Logger log ;

	@DefaultExecute
	public void execute() throws InterruptedException {
    Thread.sleep(10000L);
		log.info(DateUtils.formatDate(new Date(), DateUtils.SDF_FORMAT));
	}

}
