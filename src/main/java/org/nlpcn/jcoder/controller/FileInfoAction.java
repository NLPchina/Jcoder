package org.nlpcn.jcoder.controller;

import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * Created by Ansj on 19/12/2017.
 * 这个类提供集群间文件交换下载
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/fileInfo")
@Ok("json")
public class FileInfoAction {

	public Restful listFiles(@Param("groupName") String groupName) {
		return null;
	}

	/**
	 * 获得一个文件的输出流
	 *
	 * @param path
	 */
	public void getFile(@Param("groupName") String groupName, @Param("path") String path, HttpServletResponse response) throws FileNotFoundException {

		File file = new File(StaticValue.GROUP_FILE, groupName + path);

		if (!file.exists()) {
			throw new FileNotFoundException(file.toURI().getPath() + " not found in " + StaticValue.getHostPort());
		}

		byte[] buffer = new byte[10240];

		int len = 0;

		response.addHeader("Content-Disposition", "attachment;filename="+file.getName());
		response.setContentType("application/octet-stream");




	}


}
