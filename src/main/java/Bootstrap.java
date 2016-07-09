
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.webapp.WebAppContext;

public class Bootstrap {

	private static final String PREFIX = "jcoder_";

	private static final Map<String, String> ENV_MAP = new HashMap<>();

	public static void main(String[] args) throws Exception {

		for (String arg : args) {
			if (arg.startsWith("-f") && arg.contains("=")) {
				String[] dim = arg.split("=");
				parseFile(dim[1]);
				break;
			}else{
				System.err.println("are you sure ? -f not -f=file ! it not can use!");
			}
		}

		for (String arg : args) {
			if (arg.startsWith("--") && arg.contains("=")) {
				String[] dim = arg.split("=");
				if (dim.length >= 2) {
					if (dim[0].equals("--host")) {
						ENV_MAP.put(PREFIX + "host", dim[1]);
					} else if (dim[0].equals("--port")) {
						ENV_MAP.put(PREFIX + "port", dim[1]);
					} else if (dim[0].equals("--home")) {
						ENV_MAP.put(PREFIX + "home", dim[1]);
					} else if (dim[0].equals("--log")) {
						ENV_MAP.put(PREFIX + "log", dim[1]);
					} else if (dim[0].equals("--maven")) {
						ENV_MAP.put(PREFIX + "maven", dim[1]);
					}
				}
			}else{
				System.err.println("arg : "+arg +" can use ! not find = over it ");
			}
		}

		getOrCreateEnv(PREFIX + "maven", "mvn");
		getOrCreateEnv(PREFIX + "host", null);
		String logPath = getOrCreateEnv(PREFIX + "log", "log/jcoder.log");

		String home = getOrCreateEnv(PREFIX + "home", new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath());
		int port = Integer.parseInt(getOrCreateEnv(PREFIX + "port", "8080"));

		System.setProperty("java.awt.headless", "true"); //support kaptcha
		
		Server server = new Server(port);
		
		ProtectionDomain domain = Bootstrap.class.getProtectionDomain();
		
		URL location = domain.getCodeSource().getLocation();

		WebAppContext context = new WebAppContext();

		File jcoderHome = new File(home);

		makeFiles(jcoderHome, logPath);

		context.setTempDirectory(new File(jcoderHome, "tmp"));
		context.setContextPath("/");
		context.setServer(server);

		context.setWelcomeFiles(new String[] { "Home.jsp" });

		context.setExtraClasspath(new File(jcoderHome, "resource").getAbsolutePath());

		context.setInitParameter("org.eclipse.jetty.servlet.DefaultServlet.useFileMappedB uffer", "false");

		if (location.toExternalForm().endsWith(".war")) { // 如果是war包
			context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
			context.setWar(location.toExternalForm());
		} else {
			context.setWar("src/main/webapp");
		}
		
		server.setHandler(context);
		
		server.start();
		server.join();
	}

	/**
	 * config log4j setting
	 * 
	 * @param logPath
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void createLog4jConfig(File log4jFile, String logPath) throws FileNotFoundException, IOException {

		StringBuilder sb = new StringBuilder();

		sb.append(
				"log4j.rootLogger=info, stdout,R\n" + "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" + "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout  \n"
						+ "log4j.appender.stdout.layout.ConversionPattern=%c-%-4r %-5p [%d{yyyy-MM-dd HH:mm:ss}]  %m%n\n" + "\n"
						+ "log4j.appender.R=org.apache.log4j.DailyRollingFileAppender\n" + "log4j.appender.R.File=");

		sb.append(logPath);

		sb.append("\n" + "log4j.appender.R.DatePattern = '.'yyyy-MM-dd\n" + "log4j.appender.R.layout=org.apache.log4j.PatternLayout\n"
				+ "log4j.appender.R.layout.ConversionPattern=%d{HH:mm:ss} %c{1} %-5p %m%n\n" + "\n" + "## Disable other log  \n"
				+ "log4j.logger.org.atmosphere.cpr.AsynchronousProcessor=FATAL");

		wirteFile(log4jFile.getAbsolutePath(), "utf-8", sb.toString());

	}

	private static void parseFile(String file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
			String temp = null;

			while ((temp = br.readLine()) != null) {

				temp = temp.trim();

				if (StringUtil.isBlank(temp)) {
					continue;
				}

				if (temp.trim().charAt(0) == '#') {
					continue;
				}

				String[] split = temp.split("=");

				if (split.length == 2) {
					String key = split[0].trim();
					if (!key.startsWith(PREFIX)) {
						key = PREFIX + key;
					}
					ENV_MAP.put(key, split[1].trim());
				} else {
					System.err.println(temp + " format err");
				}

			}
		}
	}

	private static void makeFiles(File JcoderHome, String logPath) throws FileNotFoundException, IOException {
		File libDir = new File(JcoderHome, "lib"); // create jar dir
		if (!libDir.exists()) {
			libDir.mkdirs();
			wirteFile(new File(libDir, "pom.xml").getAbsolutePath(), "utf-8",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
							+ "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
							+ "	<modelVersion>4.0.0</modelVersion>\n" + "	<groupId>org.nlpcn</groupId>\n" + "	<artifactId>jcoder</artifactId>\n" + "	<version>1.2</version>\n"
							+ "	<description>use maven to down jars</description>\n" + "  \n" + "	<dependencies>\n" + "\n" + "	</dependencies>\n" + "\n" + "	<build>  \n"
							+ "		<defaultGoal>compile</defaultGoal>\n" + "	</build>\n" + "</project>");
		}


		File tmpDir = new File(JcoderHome, "tmp"); // create tmp dir
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		
		File pluginDir = new File(JcoderHome, "plugins"); // create tmp dir
		if (!pluginDir.exists()) {
			pluginDir.mkdirs();
		}

		File resourceDir = new File(JcoderHome, "resource"); // create resource
																// dir
		if (!resourceDir.exists()) {
			resourceDir.mkdirs();
		}
		
		File iocFile = new File(JcoderHome, "/resource/ioc.js"); // create ioc file
		if (!iocFile.exists()) {
			wirteFile(iocFile.getAbsolutePath(), "utf-8", "var ioc = {\n\n};");
		}

		createLog4jConfig(new File(resourceDir, "log4j.properties"), logPath);
	}

	private static String getOrCreateEnv(String key, String def) {

		if (!key.startsWith(PREFIX)) {
			key = PREFIX + key;
		}

		String value = ENV_MAP.get(key);

		if (value == null) {
			value = System.getProperty(key);
		}

		if (value == null) {
			value = System.getenv(key);
		}

		if (value == null) {
			value = def;
		}

		if (value != null) {
			System.setProperty(key, value);
		}

		return value;
	}

	/**
	 * 写文件
	 * 
	 * @param filePath
	 * @param encoding
	 * @param content
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void wirteFile(String filePath, String encoding, String content) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
			fos.write(content.getBytes(encoding));
			fos.flush();
		}
	}
}
