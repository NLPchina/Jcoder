//package org.nlpcn.jcoder.run.java;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.log4j.Logger;
//import org.nlpcn.jcoder.run.annotation.Execute;
//import org.nlpcn.jcoder.util.StaticValue;
//import org.nutz.ioc.loader.annotation.Inject;
//import org.nutz.mvc.Mvcs;
//import org.nutz.mvc.annotation.AdaptBy;
//import org.nutz.mvc.annotation.Param;
//import org.nutz.mvc.upload.TempFile;
//import org.nutz.mvc.upload.UploadAdaptor;
//
///**
// * 转换PDF文件
// */
//public class PDFConvertApi {
//
//	@Inject
//	private Logger LOG;
//
//	private static final String pdfUtilPath = StaticValue.RESOURCE_FILE.getAbsolutePath();
//
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void doc(@Param("file") TempFile file) throws Exception {
//		execute(file, ".doc", FileType.doc);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void docx(@Param("file") TempFile file) throws Exception {
//		execute(file, ".docx", FileType.docx);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void html(@Param("file") TempFile file) throws Exception {
//		execute(file, file.getName()+"_file", FileType.html);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void pptx(@Param("file") TempFile file) throws Exception {
//		execute(file, ".pptx", FileType.pptx);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void rtf(@Param("file") TempFile file) throws Exception {
//		execute(file, ".rtf", FileType.rtf);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void jpeg(@Param("file") TempFile file) throws Exception {
//		execute(file, file.getName()+"_file", FileType.jpeg);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void png(@Param("file") TempFile file) throws Exception {
//		execute(file, file.getName()+"_file", FileType.png);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void tiff(@Param("file") TempFile file) throws Exception {
//		execute(file, ".tiff", FileType.tiff);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void txt(@Param("file") TempFile file) throws Exception {
//		execute(file, ".txt", FileType.txt);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void xml(@Param("file") TempFile file) throws Exception {
//		execute(file, ".xml", FileType.xml);
//	}
//	
//	/**
//	 * 上传PDF文件, 得到转换后的文件
//	 *
//	 * @param file 上传的PDF文件
//	 * @param targetType 目标类型: doc;docx;xlsx;pptx;jpeg;tiff;png;html;rtf;txt;xml
//	 * @return 结果信息
//	 * @throws IOException
//	 */
//	@Execute
//	@AdaptBy(type = UploadAdaptor.class)
//	public void xlsx(@Param("file") TempFile file) throws Exception {
//		execute(file, ".xlsx", FileType.xlsx);
//	}
//	
//	private void execute(TempFile file, String suffix, FileType type) throws Exception, IOException {
//		check(file);
//		String filePath = file.getFile().getAbsolutePath();
//		String targetFilePath = filePath;
//		convertPDF(filePath, targetFilePath, type);
//		out2client(targetFilePath);
//	}
//
//	/**
//	 * 写回到客户端
//	 * 
//	 * @param targetFilePath
//	 * @throws IOException
//	 */
//	private void out2client(String targetFilePath) throws IOException {
//		HttpServletResponse resp = Mvcs.getResp();
//
//		File file = new File(targetFilePath);
//
//		resp.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name()));
//		resp.setContentType("application/octet-stream");
//		resp.setContentLength((int) file.length());
//
//		byte[] bytes = new byte[10240];
//
//		OutputStream fos = resp.getOutputStream();
//
//		try (InputStream content = new FileInputStream(file)) {
//			int len = -1;
//
//			while ((len = content.read(bytes)) > -1) {
//				fos.write(bytes, 0, len);
//			}
//			fos.flush();
//
//			fos.close();
//		} finally {
//			file.delete();
//		}
//
//	}
//
//	private void check(@Param("file") TempFile file) throws Exception {
//		if (file == null) {
//			LOG.warn("no file to convert");
//			throw new Exception("no files");
//		}
//	}
//
//	/**
//	 * 转换PDF文件 通过调用工具PDFConverter.exe来转换, 调用命令: PDFConverter.exe "源PDF文件" "目标文件" "doc"/"html"
//	 *
//	 * @param pdfFilePath PDF文件绝对路径
//	 * @param targetFilePath 目标文件绝对路径
//	 * @param fileType 目标文件类型
//	 * @return 终端的输出
//	 * @throws Exception
//	 */
//	private String convertPDF(String pdfFilePath, String targetFilePath, FileType fileType) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		Process proc = null;
//		try {
//			ProcessBuilder pb = new ProcessBuilder(pdfUtilPath + File.separator + "PDFConverter.exe", pdfFilePath, targetFilePath, fileType.type);
//			pb.directory(new File(pdfUtilPath));
//			pb.redirectErrorStream(true);
//			LOG.info("execute : " + Arrays.toString(pb.command().toArray()));
//
//			// Start the process
//			proc = pb.start();
//			LOG.info("Process started !");
//
//			// Read the process's output
//			try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
//				String line;
//				while ((line = in.readLine()) != null) {
//					sb.append(line).append("\n");
//				}
//			}
//			LOG.info(String.format("convert file[%s] to file[%s] successfully", pdfFilePath, targetFilePath));
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		} finally {
//			// Clean-up
//			if (proc != null) {
//				proc.destroy();
//			}
//		}
//		if (LOG.isDebugEnabled()) {
//			LOG.debug(sb.toString());
//		}
//		return sb.toString();
//	}
//
//	private enum FileType {
//		doc("doc"), docx("docx"), xlsx("xlsx"), pptx("pptx"), jpeg("jpeg"), tiff("tiff"), png("png"), html("html"), rtf("rtf"), txt("plain-text"), xml("xml-1-00");
//
//		private String type;
//
//		FileType(String type) {
//			this.type = type;
//		}
//	}
//
//}
