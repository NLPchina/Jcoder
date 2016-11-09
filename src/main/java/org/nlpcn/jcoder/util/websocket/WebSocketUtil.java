package org.nlpcn.jcoder.util.websocket;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地代码提交到远程服务器运行的工具类，用于调试和跑参数
 * 
 * @author ansj
 * 
 */
public class WebSocketUtil {

	public static final String STOP = "stop request!";

	public static void remoteRun(String host, Class<?> c, Map<String, String> param) throws Exception {

		// 根据class 获得代码

		File codeFile = FileFinder.find(c.getName().replace(".", System.getProperty("file.separator")) + ".java");

		String code = IOUtil.getContent(codeFile, "utf-8");

		// 将代码提交到远程

		WebSocketClient client = new WebSocketClient();

		try {
			SimpleEchoSocket socket = new SimpleEchoSocket();
			client.start();
			URI echoUri = new URI("ws://" + host + "/console");
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(socket, echoUri, request);

			JSONObject job = new JSONObject();

			job.put("_type", "eclipseRun");
			job.put("task.code", code);
			job.put("task.codeType", "java");
			if (param != null)
				job.put("params", param);

			socket.sendMessage(job.toJSONString());

			while (!socket.stop) {
				Thread.sleep(1000);
			}

			client.stop();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public static void remoteRun(String host, Class<?> c, String... param) throws Exception {
		HashMap<String, String> params = new HashMap<>();
		if (param != null && param.length > 0) {
			for (int i = 0; i < param.length; i += 2) {
				params.put(param[i], param[i + 1]);
			}
		}
		remoteRun(host, c, params);
	}
}
