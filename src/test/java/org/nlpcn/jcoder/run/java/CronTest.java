package org.nlpcn.jcoder.run.java;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class CronTest {

	@Inject
	private Logger log;

	@Execute
	public void execute() throws InterruptedException, UnsupportedEncodingException, FileNotFoundException {
		log.info("test test");
	}

}
