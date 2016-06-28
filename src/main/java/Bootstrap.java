
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Bootstrap {

	public static void main(String[] args) throws Exception {
		String host = null;
		int port = 8080;
		String contextPath = "/";
		boolean forceHttps = false;

		for (String arg : args) {
			if (arg.startsWith("--") && arg.contains("=")) {
				String[] dim = arg.split("=");
				if (dim.length >= 2) {
					if (dim[0].equals("--host")) {
						host = dim[1];
					} else if (dim[0].equals("--port")) {
						port = Integer.parseInt(dim[1]);
					} else if (dim[0].equals("--prefix")) {
						contextPath = dim[1];
					} else if (dim[0].equals("--jcoder.home")) {
						System.setProperty("jcoder.home", dim[1]);
					}
				}
			}
		}

		Server server = new Server(port);

		File JcoderHome = getJcoderHome();

		System.setProperty(JAVA_HOME_NAME, JcoderHome.getAbsolutePath());

		ProtectionDomain domain = Bootstrap.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();

		WebAppContext context = new WebAppContext();

		makeFiles(JcoderHome);

		context.setTempDirectory(new File(JcoderHome, "tmp"));
		context.setContextPath(contextPath);
		context.setServer(server);

		context.setWelcomeFiles(new String[] { "Home.jsp" });

		context.setExtraClasspath(new File(JcoderHome, "resource").getAbsolutePath());

		context.setInitParameter("org.eclipse.jetty.servlet.DefaultServlet.useFileMappedB uffer", "false");

		if (location.toExternalForm().endsWith(".war")) { // 如果是war包
			context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
			context.setWar(location.toExternalForm());
		} else {
			context.setWar("src/main/webapp");
		}

		if (forceHttps) {
			context.setInitParameter("org.scalatra.ForceHttps", "true");
		}

		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", "-1");
		context.setMaxFormContentSize(-1);
		server.setHandler(context);
		server.setStopAtShutdown(true);
		server.start();
		server.join();
	}

	private static void makeFiles(File JcoderHome) throws FileNotFoundException, IOException {
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

		File iocFile = new File(JcoderHome, "ioc.js"); // create ioc file
		if (!iocFile.exists()) {
			wirteFile(iocFile.getAbsolutePath(), "utf-8", "var ioc = {\n\n};");
		}

		File tmpDir = new File(JcoderHome, "tmp"); // create tmp dir
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		

		File resourceDir = new File(JcoderHome, "resource"); // create resource dir
		if (!resourceDir.exists()) {
			resourceDir.mkdirs();
		}
	}

	private static final String JAVA_HOME_NAME = "jcoder.home";
	private static final String EVN_HOME_NAME = "JCODER_HOME";

	private static File getJcoderHome() {
		String home = System.getProperty(JAVA_HOME_NAME);
		if (home != null && home.length() > 0) {
			return new File(home);
		}
		home = System.getenv(EVN_HOME_NAME);
		if (home != null && home.length() > 0) {
			return new File(home);
		}
		return new File(System.getProperty("user.home"), ".jcoder");
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
