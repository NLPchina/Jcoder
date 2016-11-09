package org.nlpcn.jcoder.run.java;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nutz.ioc.loader.annotation.Inject;

public class CronTest {

	@Inject
	private Logger log;

	@DefaultExecute
	public void execute() throws InterruptedException, UnsupportedEncodingException, FileNotFoundException {
		log.info("test test");
	}

}
