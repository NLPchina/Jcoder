package org.nlpcn.jcoder.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.BackupService;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;

@IocBean
@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
public class SystemAction {

	@At("/system")
	@Ok("jsp:/system.jsp")
	public void list(HttpServletRequest req) {

		Properties pro = System.getProperties();

		Map<String, Object> properties = new TreeMap<>();

		pro.entrySet().forEach(e -> properties.put(String.valueOf(e.getKey()), String.valueOf(e.getValue())));

		req.setAttribute("properties", properties);
	}

	@At("/system/export_task")
	@Ok("raw")
	public void export(HttpServletResponse response) throws IOException {

		BackupService backupService = StaticValue.getBean(BackupService.class, "backupService");

		String timeStamp = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Disposition", "attachment;filename=" + timeStamp + ".tasks.bak");
		byte[] backup = backupService.backup().getBytes("utf-8");
		response.setContentLength(backup.length);
		response.getOutputStream().write(backup);
	}

	@At("/system/import_task")
	@AdaptBy(type = UploadAdaptor.class)
	@Ok("jsp:/system.jsp")
	public String uploadJar(@Param("file") TempFile[] fileList) throws Exception {
		if (fileList == null) {
			throw new Exception("up file can not empty");
		}

		BackupService backupService = StaticValue.getBean(BackupService.class, "backupService");

		StringBuilder sb = new StringBuilder();

		for (TempFile tempFile : fileList) {
			try (InputStreamReader reader = new InputStreamReader(tempFile.getInputStream())) {
				String restore = backupService.restore(reader);
				sb.append(restore);
				sb.append("\n");
			}
		}
		List<Group> groups = StaticValue.systemDao.search(Group.class, Cnd.NEW());

		Mvcs.getHttpSession().setAttribute("GROUP_LIST", groups);

		list(Mvcs.getReq());

		return sb.toString();
	}

	public static void main(String[] args) {
		
	}
}
