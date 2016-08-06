package org.nlpcn.jcoder.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.webscoket.WebSocketConsole;

public class PrintConsoleJob implements Runnable {

	private static final Logger LOG = Logger.getLogger(PrintConsoleJob.class);

	private int count = 0;

	@Override
	public void run() {
		File file = new File(StaticValue.LOG_PATH);
		while (true) {
			try {
				if (WebSocketConsole.count() > 0 && file.exists()) {
					try (FileInputStream fis = new FileInputStream(file)) {
						long length = file.length() - 5000;
						fis.skip(length <= 0 ? 0 : length);
						try (BufferedReader br = IOUtil.getReader(fis, "utf-8")) {

							String line = null;
							while (WebSocketConsole.count() > 0) {
								line = br.readLine();

								if (count++ > 100) {
									LOG.info("read about 100 times for log !");
									count = 0;
								}

								if (StringUtil.isBlank(line)) {
									Thread.sleep(1000L);
								} else {
									WebSocketConsole.sendMessage(line);
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							WebSocketConsole.sendMessage(e.getMessage());
						}
					}
				}
				if (count++ > 60) {
					LOG.info("no connect ! wait for client conn!");
					count = 0;
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
