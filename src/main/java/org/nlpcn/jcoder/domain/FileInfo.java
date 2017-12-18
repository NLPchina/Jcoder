package org.nlpcn.jcoder.domain;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import com.google.common.base.Objects;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;

/**
 * 文件信息的包装类
 */
public class FileInfo implements Comparable<FileInfo> {

	private File file;

	private String name;

	private String md5;

	private boolean directory;

	private String relativePath;

	private long length;

	public FileInfo() {
	}

	public FileInfo(File file) {
		this.file = file;
		this.directory = file.isDirectory();
		this.name = file.getName();
		this.relativePath = file.toURI().toString().replaceFirst(new File(StaticValue.HOME_FILE, "group").toURI().toString(), "/").replaceFirst("/.*?/", "/");
		this.length = file.length();
	}

	public String getName() {
		return name;
	}

	public File file() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getEncodingPath() throws UnsupportedEncodingException {
		return URLEncoder.encode(file.getAbsolutePath(), "utf-8");
	}

	public void setName(String name) {
		this.name = name;
	}

	public synchronized String getMd5() {
		if (StringUtil.isBlank(this.md5)) {
			md5 = MD5Util.getMd5ByFile(file);
		}
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.name, this.directory, this.length, this.md5);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FileInfo)) {
			return false;
		}
		return this.relativePath.equals(((FileInfo) obj).relativePath) && this.md5.equals(((FileInfo) obj).md5);
	}

	public Date lastModified() {
		return new Date(file.lastModified());
	}

	@Override
	public int compareTo(FileInfo ji) {
		return (this.file.getAbsolutePath().compareTo(ji.file.getAbsolutePath()));
	}

}
