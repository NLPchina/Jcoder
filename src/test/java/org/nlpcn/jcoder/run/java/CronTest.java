package org.nlpcn.jcoder.run.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nutz.ioc.loader.annotation.Inject;

public class CronTest {

	@Inject
	private Logger log;

	@DefaultExecute
	public void execute() throws InterruptedException, UnsupportedEncodingException, FileNotFoundException {

		log.info("中文");

		log.info("中文");
		log.info("中文");

		log.info("中文");
		log.info("中文");

		log.info("中文");
		log.info("中文");

		log.info("中文");
		log.info("中文");

		log.info("中文");
		List<String> readFile2List = IOUtil.readFile2List("C:\\pdf\\log\\jcoder.log", "utf-8");

		for (String string : readFile2List) {
			System.out.println("aaaaaaaaaaaa"+string);
		}

	}

}
