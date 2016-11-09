
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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.webapp.WebAppContext;

public class Bootstrap {

	private static final String PREFIX = "jcoder_";

	public static void main(String[] args) throws Exception {

		for (String arg : args) {
			if (arg.startsWith("-f")) {
				if (arg.contains("=")) {
					String[] dim = arg.split("=");
					parseFile(dim[1]);
				} else {
					System.err.println("are you sure ? -f not -f=file ! it not can use!");
				}
			}
		}

		for (String arg : args) {
			if (arg.startsWith("--") && arg.contains("=")) {
				String[] dim = arg.split("=");
				if (dim.length >= 2) {
					if (dim[0].equals("--host")) {
						putEnv(PREFIX + "host", dim[1]);
					} else if (dim[0].equals("--port")) {
						putEnv(PREFIX + "port", dim[1]);
					} else if (dim[0].equals("--rpcport")) {
						putEnv(PREFIX + "rpcport", dim[1]);
					} else if (dim[0].equals("--home")) {
						putEnv(PREFIX + "home", dim[1]);
					} else if (dim[0].equals("--log")) {
						putEnv(PREFIX + "log", dim[1]);
					} else if (dim[0].equals("--maven")) {
						putEnv(PREFIX + "maven", dim[1]);
					} else if (dim[0].equals("--upload")) {
						putEnv(PREFIX + "upload", dim[1]);
					}
				}
			} else if (!arg.startsWith("-f")) {
				System.err.println("arg : " + arg + " can use ! not find = over it ");
			}
		}

		getOrCreateEnv(PREFIX + "maven", "mvn");
		getOrCreateEnv(PREFIX + "host", null);

		String logPath = getOrCreateEnv(PREFIX + "log", "log/jcoder.log");

		String home = getOrCreateEnv(PREFIX + "home", new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath());

		int port = Integer.parseInt(getOrCreateEnv(PREFIX + "port", "8080"));

		System.setProperty("java.awt.headless", "true"); // support kaptcha

		Server server = new Server(port);

		ProtectionDomain domain = Bootstrap.class.getProtectionDomain();

		URL location = domain.getCodeSource().getLocation();

		WebAppContext context = new WebAppContext();

		File jcoderHome = new File(home);

		makeFiles(jcoderHome, logPath);

		context.setTempDirectory(new File(jcoderHome, "tmp"));
		context.setContextPath("/");
		context.setServer(server);
		context.setMaxFormContentSize(0);

		context.setWelcomeFiles(new String[] { "Home.jsp" });

		context.setExtraClasspath(new File(jcoderHome, "resource").getAbsolutePath());

		if (location.toExternalForm().endsWith(".war")) { // 如果是war包
			context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
			context.setWar(location.toExternalForm());
		} else {
			context.setWar("src/main/webapp");
		}

		HandlerList list = new HandlerList();

		WebAppContext web = new WebAppContext(); // add a web site in jcoder

		web.setContextPath("/web/");

		web.setWar(new File(jcoderHome, "web").getAbsolutePath());

		// Fix for Windows, so Jetty doesn't lock files
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
			web.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		}

		list.addHandler(web);

		list.addHandler(context);

		server.setHandler(list);

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

		if (log4jFile.exists()) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("log4j.rootLogger=info, stdout,R\n" + "log4j.appender.stdout.Encoding=utf-8\n" + "log4j.appender.R.Encoding=utf-8\n"
				+ "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" + "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout  \n"
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
					putEnv(key, split[1].trim());
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
					"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
							+ "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
							+ "	<modelVersion>4.0.0</modelVersion>\n" + "	<groupId>org.nlpcn</groupId>\n" + "	<artifactId>jcoder</artifactId>\n" + "	<version>0.1</version>\n"
							+ "	\n" + "	<dependencies>\n" + "	</dependencies>\n" + "\n" + "	<build>\n" + "		<sourceDirectory>src/main/java</sourceDirectory>\n"
							+ "		<testSourceDirectory>src/test/java</testSourceDirectory>\n" + "		\n" + "		<plugins>\n" + "			<plugin>\n"
							+ "				<artifactId>maven-compiler-plugin</artifactId>\n" + "				<version>3.3</version>\n" + "				<configuration>\n"
							+ "					<source>1.8</source>\n" + "					<target>1.8</target>\n" + "					<encoding>UTF-8</encoding>\n"
							+ "					<compilerArguments>\n" + "						<extdirs>lib</extdirs>\n" + "					</compilerArguments>\n"
							+ "				</configuration>\n" + "			</plugin>\n" + "		</plugins>\n" + "	</build>\n" + "</project>\n" + "");
		}

		File tmpDir = new File(JcoderHome, "tmp"); // create tmp dir
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}

		File pluginDir = new File(JcoderHome, "web"); // create web dir
		if (!pluginDir.exists()) {
			pluginDir.mkdirs();
		}

		File resourceDir = new File(JcoderHome, "resource"); // create resource dir
		if (!resourceDir.exists()) {
			resourceDir.mkdirs();
		}

		String uploadPath = System.getProperty(PREFIX + "upload"); //create upload file

		if (uploadPath == null) {
			uploadPath = new File(JcoderHome, "upload").getAbsolutePath();
			putEnv(PREFIX + "upload", uploadPath);
		}

		File upload = new File(uploadPath);

		if (!upload.exists()) {
			upload.mkdirs();
		}

		File iocFile = new File(JcoderHome, "/resource/ioc.js"); // create ioc file
		if (!iocFile.exists()) {
			wirteFile(iocFile.getAbsolutePath(), "utf-8", "var ioc = {\n\n};");
		}

		createLog4jConfig(new File(resourceDir, "log4j.properties"), logPath);
	}

	/**
	 * get a env by key , if not exits , to put it
	 * 
	 * @param key
	 * @param def
	 * @return
	 */
	private static String getOrCreateEnv(String key, String def) {

		if (!key.startsWith(PREFIX)) {
			key = PREFIX + key;
		}

		String value = System.getProperty(key);

		if (value == null) {
			value = System.getenv(key);
		}

		if (value == null) {
			putEnv(key, def);
			value = def;
		}

		return value;
	}

	/**
	 * put var to java env
	 * 
	 * @param key
	 * @param value
	 */
	private static void putEnv(String key, String value) {
		if (!key.startsWith(PREFIX)) {
			key = PREFIX + key;
		}
		if (value != null) {
			System.setProperty(key, value);
		}
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
