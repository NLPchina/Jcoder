package org.nlpcn.jcoder.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.controller.ConsoleAction;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintConsoleJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PrintConsoleJob.class);

	private int count = 0;

	@Override
	public void run() {
		File file = new File(StaticValue.LOG_PATH);
		
		ConsoleAction websocketService = StaticValue.getSystemIoc().get(ConsoleAction.class) ;
		
		while (true) {
			try {
				if (websocketService.count() > 0 && file.exists()) {
					try (FileInputStream fis = new FileInputStream(file)) {
						long length = file.length() - 5000;
						fis.skip(length <= 0 ? 0 : length);
						try (BufferedReader br = IOUtil.getReader(fis, "utf-8")) {
							
							String line = null;
							
							while (websocketService.count() > 0) {
								line = br.readLine();
								if (StringUtil.isBlank(line)) {
									Thread.sleep(10L);
								} else {
									websocketService.sendMessage(line);
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							LOG.error("websocket send message err ",e);
							websocketService.sendMessage(e.getMessage());
						}
					}
				}
				
				if (count++ > 60) {
					LOG.debug("no connect ! wait for client conn!");
					count = 0;
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
