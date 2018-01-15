package org.nlpcn.jcoder.run.java;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;

public class CronTest {

	@Inject
	private Logger log;

	@Execute
	public void execute() throws InterruptedException, UnsupportedEncodingException, FileNotFoundException {
		log.info("test test");
	}

}
