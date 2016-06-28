import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.util.dao.BasicDao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 将所有action导入到当前目录
 * 
 * @author ansj
 * 
 */
public class ImportReplaceAllTask {

	private static List<String> create = new ArrayList<>();
	private static List<String> update = new ArrayList<>();
	private static List<String> ignore = new ArrayList<>();
	private static List<String> skip = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		BasicDao dao = new BasicDao("jdbc:mysql://172.21.19.203:3306/kuyun_eye?useUnicode=true&characterEncoding=utf-8", "ky", "ky");
		File base = new File(FileFinder.find("kuyun-data/code"), "com/kuyun/data/code"); // 程序目录
		
		new File("bak/").mkdirs() ;
		
		doZip(base.getAbsolutePath(), new File("bak/").getAbsolutePath()+"/code_"+System.currentTimeMillis()+".zip") ;

		List<Task> search = dao.search(Task.class, "id");

		for (Task task : search) {
			write2File(base, task);
		}

		System.out.println("create: " + create);
		System.out.println("update: " + update);
		System.out.println("ignore: " + ignore);
		System.out.println("skip: " + skip);
	}

	private static void write2File(File base, Task task) throws IOException {

		if (!task.getCodeType().equals("java")) {
			System.err.println("ignore " + task.getName());
			ignore.add(task.getName());
			return;
		}

		String code = task.getCode();

		String path = JavaSourceUtil.findPackage(code);

		String className = JavaSourceUtil.findClassName(code);

		File codeDir = null;
		if (!"com.kuyun.data.code".equals(path)) {
			codeDir = new File(base, path.replace("com.kuyun.data.code.", "").replace(".", "/"));
		} else {
			codeDir = base;
		}

		// 创建目录
		codeDir.mkdirs();

		File codeFile = new File(codeDir, className + ".java");

		if (codeFile.isFile()) {
			List<String> codeList = IOUtil.readFile2List(codeFile, "UTF-8");

			List<String> contentList = IOUtil.readFile2List(new BufferedReader(new StringReader(code)));

			if (codeList.size() == contentList.size()) {
				boolean flag = true;
				for (int i = 0; i < codeList.size(); i++) {
					if (!codeList.get(i).equals(contentList.get(i))) {
						flag = false;
						break;
					}
				}

				if (flag) {
					System.out.println("skip " + task.getName());
					skip.add(task.getName());
					return;
				}
			}

		} else {
			IOUtil.Writer(codeFile.getAbsolutePath(), "UTF-8", code);

			System.out.println("create " + task.getName());
			create.add(task.getName());
			return;
		}

		IOUtil.Writer(codeFile.getAbsolutePath(), "UTF-8", code);

		System.out.println("update " + task.getName());
		update.add(task.getName());
	}

	public static File doZip(String sourceDir, String zipFilePath) throws IOException {

		File file = new File(sourceDir);
		File zipFile = new File(zipFilePath);
		ZipOutputStream zos = null;
		try {
			// 创建写出流操作
			OutputStream os = new FileOutputStream(zipFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			zos = new ZipOutputStream(bos);

			String basePath = null;

			// 获取目录
			if (file.isDirectory()) {
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}

			zipFile(file, basePath, zos);
		} finally {
			if (zos != null) {
				zos.closeEntry();
				zos.close();
			}
		}

		return zipFile;
	}

	private static void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException {
		File[] files = null;
		if (source.isDirectory()) {
			files = source.listFiles();
		} else {
			files = new File[1];
			files[0] = source;
		}

		InputStream is = null;
		String pathName;
		byte[] buf = new byte[1024];
		int length = 0;
		try {
			for (File file : files) {
				if (file.isDirectory()) {
					pathName = file.getPath().substring(basePath.length() + 1) + "/";
					zos.putNextEntry(new ZipEntry(pathName));
					zipFile(file, basePath, zos);
				} else {
					pathName = file.getPath().substring(basePath.length() + 1);
					is = new FileInputStream(file);
					try (BufferedInputStream bis = new BufferedInputStream(is)) {
						zos.putNextEntry(new ZipEntry(pathName));
						while ((length = bis.read(buf)) > 0) {
							zos.write(buf, 0, length);
						}
					}
				}
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}

	}

}
