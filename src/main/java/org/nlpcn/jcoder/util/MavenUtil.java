package org.nlpcn.jcoder.util;

import com.google.common.collect.Lists;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class MavenUtil {


	private static final Logger LOG = LoggerFactory.getLogger(MavenUtil.class);

	/**
	 * 中央仓库地址
	 */
	private static final List<String> MAVEN_CENTER = Lists.newArrayList("http://maven.aliyun.com/nexus/content/groups/public/", "http://central.maven.org/maven2/");


	public static void execute(String pomPath, String savePath) throws IOException, XmlPullParserException {

		List<String> resps = new ArrayList<>();

		MavenXpp3Reader reader = new MavenXpp3Reader();

		try (FileReader fileReader = new FileReader(pomPath)) {

			Model model = reader.read(fileReader);

			/**
			 * 先确定maven源
			 */
			List<Repository> repositories = model.getRepositories();
			for (Repository repository : repositories) {
				String url = repository.getUrl();
				if (!url.endsWith("/")) {
					url = url + "/";
				}

				resps.add(url);
			}

			for (String url : MAVEN_CENTER) {
				if (resps.contains(url)) {
					continue;
				}
				resps.add(url);
			}


			/**
			 * 找依赖并下载
			 */
			List<Dependency> dependencies = model.getDependencies();
			for (Dependency dependency : dependencies) {
				if (!StringUtil.isBlank(dependency.getScope()) && !"compile".equals(dependency.getScope())) {
					continue;
				}

				for (String resp : resps) {
					try {
						downJar(resp, dependency, savePath);
						break;
					} catch (Exception e) {
						LOG.error(e.getMessage());
					}
				}
			}

		}
	}

	/**
	 * 下载jar包并且保存
	 *
	 * @param resp
	 * @param dependency
	 * @param savePath
	 */
	private static void downJar(String resp, Dependency dependency, String savePath) throws Exception {
		String jarName = dependency.getArtifactId() + "-" + dependency.getVersion() + "." + dependency.getType();
		StringBuilder sb = new StringBuilder(resp);
		sb.append(dependency.getGroupId().replace(".", "/")).append("/");
		sb.append(dependency.getArtifactId() + "/").append(dependency.getVersion() + "/").append(jarName);
		String jarUrl = sb.toString();
		File jarFile = new File(new File(savePath), jarName);

		if (jarFile.exists() && jarFile.isDirectory()) {
			Files.deleteFile(jarFile);
			System.gc();
			Thread.sleep(1000L);
		}

		if (!jarFile.exists()) {
			if (!jarFile.getParentFile().exists()) {
				jarFile.getParentFile().mkdirs();
			}
			IOUtil.downFile(jarUrl, jarFile);
		} else {
			LOG.info("{} has exists so skip down",jarFile.getCanonicalFile());
		}

		JarFile jf = new JarFile(jarFile) ;

		ZipEntry entry = jf.getEntry("META-INF/maven/" + dependency.getGroupId() + "/" + "/pom.xml");

		String content = IOUtil.getContent(jf.getInputStream(entry), "utf-8");

		System.out.println(content);


	}

	public static void main(String[] args) throws IOException, XmlPullParserException {
		MavenUtil.execute("D:\\workspace\\Jcoder\\jcoder_home_9095\\group\\InfcnNlp\\pom.xml", "D:\\workspace\\Jcoder\\jcoder_home_9095\\group\\InfcnNlp\\lib\\target\\dependency");
	}
}
