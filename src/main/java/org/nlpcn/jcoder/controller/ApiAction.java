package org.nlpcn.jcoder.controller;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

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

			String code = json.getString("task.code");
			String codeType = json.getString("task.codeType");

			Task task = new Task();
			task.setCode(code);
			task.setCodeType(codeType);
			task.setType(2);

			taskName = task.getName() + "@" + "0@" + Mvcs.getReq().getRemoteAddr();

			if (ThreadManager.checkExists(taskName)) {
				LOG.warn(taskName + " has beening run! pleast stop it first!");
			} else {
				LOG.info(taskName + " publish ok ! will be run !");
				ThreadManager.add2ActionTask(taskName, Thread.currentThread());
				LOG.info(taskName + " result : " + new JavaRunner(task).compile().instanceObjByIoc().execute());
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " " + ExceptionUtil.printStackTraceWithOutLine(e));

			return StaticValue.errMessage(e.getMessage());
		} finally {
			if (taskName != null)
				ThreadManager.removeActionIfOver(taskName);
		}

		return StaticValue.okMessage("code run over!");
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
			taskName = json.getString("task.name") + "@" + "0@" + Mvcs.getReq().getRemoteAddr();
			ThreadManager.stop(taskName);
			LOG.info(taskName + " stoped ! ");
			return StaticValue.OK;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " err " + ExceptionUtil.printStackTraceWithOutLine(e));
			return StaticValue.ERR;
		}

	}
}
