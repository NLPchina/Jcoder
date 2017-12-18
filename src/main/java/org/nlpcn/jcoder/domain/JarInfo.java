package org.nlpcn.jcoder.domain;

import java.io.File;

import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.util.StaticValue;

public class JarInfo extends FileInfo {

	private int status; // 0 加载成功 1 未加载 2 未释放

	public JarInfo(File file, int status) {
		super(file);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean getIsMavenJar(String groupName) {
		return this.file().getParentFile().equals(new File(StaticValue.HOME + "/" + groupName + "/lib/target/dependency/"));
	}
}
