package org.nlpcn.jcoder.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	/**
	 * 得到一个文件的md5
	 *
	 * @param file
	 * @return
	 */
	public static String md5(File file) {
		if (file.isDirectory()) {
			return MD5Util.md5(file.getName());
		}
		String value = "ERROR";

		try (FileInputStream in = new FileInputStream(file)) {
			value = DigestUtils.md5Hex(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}


	/**
	 * 对一个字符串进行md5
	 * @param content
	 * @return
	 */
	public static String md5(String content) {
		return DigestUtils.md5Hex(content);
	}

	public static String sha1(File file) {
		if (file.isDirectory()) {
			return MD5Util.sha1(file.getName());
		}
		String value = "ERROR";

		try (FileInputStream in = new FileInputStream(file)) {
			value = DigestUtils.sha1Hex(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}


	public static String sha1(String content) {
		return DigestUtils.sha1Hex(content);
	}
}
