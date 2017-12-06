package org.nlpcn.jcoder.util;

import java.io.*;

/**
 * java 一个简单的io操作
 *
 * @author ansj
 */
public class IOUtil {

    public static final String UTF8 = "utf-8";
    public static final String GBK = "gbk";
    public static final String TAB = "\t";
    public static final String LINE = "\n";

    public static void Writer(String path, String charEncoding, String content) {
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));
            fos.write(content.getBytes(charEncoding));
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
    }

    /**
     * 将输入流转化为字节流
     *
     * @param inputStream
     * @param charEncoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static BufferedReader getReader(InputStream inputStream, String charEncoding) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(inputStream, charEncoding));
    }

    /**
     * 读取文件获得正文
     *
     * @param path
     * @param charEncoding
     * @return
     */
    public static String getContent(String path, String charEncoding) {
        return getContent(new File(path), charEncoding);
    }

    /**
     * 从流中读取正文内容
     *
     * @param is
     * @param charEncoding
     * @return
     */
    public static String getContent(InputStream is, String charEncoding) {
        BufferedReader reader = null;
        try {
            reader = IOUtil.getReader(is, charEncoding);
            return getContent(reader);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 从文件中读取正文内容
     *
     * @param file
     * @param charEncoding
     * @return
     */
    public static String getContent(File file, String charEncoding) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getContent(is, charEncoding);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(is);
        }
        return "";
    }

    /**
     * @param reader
     * @return
     * @throws IOException
     */
    public static String getContent(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                sb.append(temp);
                sb.append("\n");
            }
        } finally {
            close(reader);
        }
        return sb.toString();
    }

    /**
     * 关闭字符流
     *
     * @param reader
     */
    public static void close(Reader reader) {
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 关闭字节流
     *
     * @param is
     */
    public static void close(InputStream is) {
        try {
            if (is != null)
                is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭字节流
     *
     * @param is
     */
    public static void close(OutputStream os) {
        try {
            if (os != null) {
                os.flush();
                os.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}