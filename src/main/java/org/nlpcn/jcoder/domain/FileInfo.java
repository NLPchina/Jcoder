package org.nlpcn.jcoder.domain;

import com.google.common.base.Objects;
import org.nlpcn.jcoder.service.FileInfoService;
import org.nlpcn.jcoder.util.StaticValue;

import java.io.File;
import java.io.Serializable;

/**
 * 文件信息的包装类
 */
public class FileInfo implements Comparable<FileInfo>, Serializable {

	private File file;

	private String name;

	private String md5;

	private boolean directory;

	private String relativePath;

	private long length;

	private String groupName;

	private long lastModified;

	private FileInfo() {
	}

	/**
	 * 从git创建文件
	 *
	 * @param file
	 * @param git
	 */
	public FileInfo(File file, boolean git) {
		this.file = file;
		this.directory = file.isDirectory();
		this.name = file.getName();
		String groupPath = null;
		if (git) {
			groupPath = file.toURI().toString().replaceFirst(new File(StaticValue.HOME_FILE, "git").toURI().toString(), "/");
		} else {
			groupPath = file.toURI().toString().replaceFirst(new File(StaticValue.HOME_FILE, "group").toURI().toString(), "/");
		}

		this.groupName = groupPath.split("/")[1];
		this.relativePath = groupPath.substring(groupName.length() + 1);
		if (this.relativePath.endsWith("/")) {
			this.relativePath = this.relativePath.substring(0, this.relativePath.length() - 1);
		}
		this.length = file.length();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File file() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public synchronized String getMd5() {
		if (this.md5 == null) {
			md5 = FileInfoService.computerMD5(this);
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

	public long getLastModified() {
		if (this.lastModified == 0 && file != null) {
			this.lastModified = file.lastModified();
		}
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public int compareTo(FileInfo ji) {
		return (this.getRelativePath().compareTo(ji.getRelativePath()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FileInfo fileInfo = (FileInfo) o;
		if (this.length != fileInfo.length) return false;
		if (this.groupName != null ? this.name.equals(fileInfo.groupName) : fileInfo.groupName == null) return false;
		if (this.getLastModified() != fileInfo.getLastModified()) return false;
		return relativePath != null ? relativePath.equals(fileInfo.relativePath) : fileInfo.relativePath == null;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(groupName, relativePath, getLastModified(), length);
	}

}
