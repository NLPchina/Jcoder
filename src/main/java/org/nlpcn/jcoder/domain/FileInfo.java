package org.nlpcn.jcoder.domain;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.nlpcn.commons.lang.util.StringUtil;

public class FileInfo implements Comparable<FileInfo> {

	private File file;

	private String name;

	public FileInfo(File file) {
		this.file = file;
	}

	public FileInfo(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public String getName() {
		if (StringUtil.isBlank(name)) {
			return file.getName();
		}
		return name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getEncodingPath() throws UnsupportedEncodingException {
		return URLEncoder.encode(file.getAbsolutePath(), "utf-8");
	}

	@Override
	public int hashCode() {
		return this.file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JarInfo) {
			return this.file.getAbsolutePath().equals(((JarInfo) obj).getFile().getAbsolutePath());
		} else {
			return false;
		}
	}

	public Date getDate() {
		return new Date(file.lastModified());
	}

	@Override
	public int compareTo(FileInfo ji) {
		return (this.file.getAbsolutePath().compareTo(ji.file.getAbsolutePath()));
	}
}
