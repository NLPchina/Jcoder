package org.nlpcn.jcoder.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeRunner;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.ActionRunManager;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.JsonResult;
import org.nlpcn.jcoder.util.JsonView;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.TextView;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@IocBean
public class ApiAction {

	private static final Logger LOG = Logger.getLogger(ApiAction.class);

	/**
	 * 执行测试用户的api
	 * 
	 * @param jsonTask
	 * @return
	 */
	@At("/run_api")
	@Ok("raw")
	public Object runApi(@Param("json") String jsonTask) {

		String taskName = null;
		try {
			JSONObject json = JSONObject.parseObject(jsonTask);

			String remoteAddr = Mvcs.getReq().getRemoteAddr();

			String code = json.getString("task.code");
			String codeType = json.getString("task.codeType");

			Task task = new Task();
			task.setCode(code);
			task.setCodeType(codeType);
			task.setType(2);

			taskName = task.getName() + "@" + "0@" + remoteAddr;
			LOG.info(taskName + " publish ok ! will be run !");
			ActionRunManager.add2ThreadPool(taskName, Thread.currentThread());
			LOG.info(taskName + " result : " + new JavaRunner(task).compile().instanceObjByIoc().execute());

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " " + ExceptionUtil.printStackTraceWithOutLine(e));

			return StaticValue.errMessage(e.getMessage());
		} finally {
			if (taskName != null)
				ThreadManager.removeActionTask(taskName);
		}

		return StaticValue.okMessage("代码提交成功!已开始运行");
	}

	/**
	 * 停止一个api任务
	 * 
	 * @param jsonTask
	 * @return
	 */
	@At("/stop_api")
	@Ok("raw")
	public Object stopApi(@Param("json") String jsonTask) {

		String taskName = null;
		try {
			LOG.info(taskName + " will be to stop ! ");
			JSONObject json = JSONObject.parseObject(jsonTask);
			String remoteAddr = Mvcs.getReq().getRemoteAddr();
			taskName = json.getString("task.name") + "@" + "0@" + remoteAddr;
			ActionRunManager.stop(taskName);
			LOG.info(taskName + " stoped ! ");
			return StaticValue.OK;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " err " + ExceptionUtil.printStackTraceWithOutLine(e));
			return StaticValue.ERR;
		}

	}
}
