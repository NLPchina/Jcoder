//package org.nlpcn.jcoder.util;
//
//import com.google.common.collect.Lists;
//import org.apache.maven.model.Dependency;
//import org.apache.maven.model.Model;
//import org.apache.maven.model.Repository;
//import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
//import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
//import org.nutz.http.Http;
//import org.nutz.http.Response;
//import org.nutz.lang.Files;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.util.*;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//import java.util.zip.ZipEntry;
//
//public class MavenUtil {
//
//
//	private static final Logger LOG = LoggerFactory.getLogger(MavenUtil.class);
//
//	/**
//	 * 中央仓库地址
//	 */
//	private static final List<String> MAVEN_CENTER = Lists.newArrayList("http://maven.aliyun.com/nexus/content/groups/public/", "http://central.maven.org/maven2/");
//
//
//	private Set<String> jarNames = new HashSet<>();
//
//	private LinkedList<File> jarFileLink = new LinkedList<>();
//
//
//	public void parseAndDown(String pomContent, String savePath) throws IOException, XmlPullParserException {
//
//		List<String> resps = new ArrayList<>();
//
//		MavenXpp3Reader reader = new MavenXpp3Reader();
//
//		try (StringReader sr = new StringReader(pomContent)) {
//
//			Model model = reader.read(sr);
//
//			/**
//			 * 先确定maven源
//			 */
//			List<Repository> repositories = model.getRepositories();
//			for (Repository repository : repositories) {
//				String url = repository.getUrl();
//				if (!url.endsWith("/")) {
//					url = url + "/";
//				}
//
//				resps.add(url);
//			}
//
//			for (String url : MAVEN_CENTER) {
//				if (resps.contains(url)) {
//					continue;
//				}
//				resps.add(url);
//			}
//
//
//			/**
//			 * 找依赖并下载
//			 */
//			List<Dependency> dependencies = model.getDependencies();
//			for (Dependency dependency : dependencies) {
//				if (!StringUtil.isBlank(dependency.getScope()) && !"compile".equals(dependency.getScope())) {
//					continue;
//				}
//
//
//				for (String resp : resps) {
//					try {
//						downJar(resp, dependency, savePath);
//						break;
//					} catch (Exception e) {
//						LOG.error(e.getMessage());
//					}
//				}
//			}
//
//			File jarFile = null;
//			while ((jarFile = jarFileLink.poll()) != null) {
//				try (JarFile jf = new JarFile(jarFile)) {
//					String jarPomPath = "META-INF/maven/" + dependency.getGroupId() + "/" + dependency.getArtifactId();
//					JarEntry jarEntry = jf.getJarEntry(jarPomPath + "/pom.xml");
//					if (jarEntry != null) {
//						JarEntry propertiesEntry = jf.getJarEntry(jarPomPath + "/pom.properties");
//
//
//						if (propertiesEntry != null) {
//							Properties pps = new Properties();
//							pps.load(jf.getInputStream(propertiesEntry));
//							Enumeration enum1 = pps.propertyNames();//得到配置文件的名字
//							while (enum1.hasMoreElements()) {
//								String strKey = (String) enum1.nextElement();
//								String strValue = pps.getProperty(strKey);
//								evn.put(strKey,strValue) ;
//							}
//						}
//
//						String content = IOUtil.getContent(jf.getInputStream(jarEntry), "utf-8");
//
//
//						parseAndDown(content, savePath);
//					}
//				}
//			}
//
//		}
//	}
//
//	/**
//	 * 下载jar包并且保存
//	 *
//	 * @param resp
//	 * @param dependency
//	 * @param savePath
//	 */
//	private void downJar(String resp, Dependency dependency, String savePath) throws Exception {
//		String jarName = dependency.getArtifactId() + "-" + dependency.getVersion() + "." + dependency.getType();
//		StringBuilder sb = new StringBuilder(resp);
//		sb.append(dependency.getGroupId().replace(".", "/")).append("/");
//		sb.append(dependency.getArtifactId() + "/").append(dependency.getVersion() + "/").append(jarName);
//		String jarUrl = sb.toString();
//		File jarFile = new File(new File(savePath), jarName);
//
//		if (jarFile.exists() && jarFile.isDirectory()) {
//			Files.deleteFile(jarFile);
//			System.gc();
//			Thread.sleep(1000L);
//		}
//
//		if (!jarFile.exists()) {
//			if (!jarFile.getParentFile().exists()) {
//				jarFile.getParentFile().mkdirs();
//			}
//			IOUtil.downFile(jarUrl, jarFile);
//		} else {
//			LOG.info("{} has exists so skip down", jarFile.getCanonicalFile());
//		}
//
//		if (jarNames.contains(jarName)) { //说明解析过。防止循环引用
//			return;
//		}
//
//		jarNameLink.add(jarFile);
//		jarNames.add(jarName);
//
//
//	}
//
//	/**
//	 * 对象是轻量单利的，建议每次new。如果非要用一个要使用这个方法清空历史采集
//	 */
//	public void clear() {
//		jarNames.clear();
//	}
//
//	public static void main(String[] args) throws IOException, XmlPullParserException {
//		new MavenUtil().parseAndDown(IOUtil.getContent("D:\\workspace\\Jcoder\\jcoder_home_9095\\group\\InfcnNlp\\pom.xml", "utf-8"), "D:\\workspace\\Jcoder\\jcoder_home_9095\\group\\InfcnNlp\\lib\\target\\dependency");
//	}
//}
