package org.nlpcn.jcoder.controller;

import com.google.common.collect.Sets;

import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.JavaDocUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@IocBean
public class ApiAction {

	private static final Logger LOG = LoggerFactory.getLogger(ApiAction.class);


	private static final Set<String> DEFAULT_METHODS = Sets.newHashSet("GET", "POST", "PUT", "DELETE");

	@Inject
	private TaskService taskService;

	/**
	 * api
	 *
	 * @return api info
	 */
	@At("/apidoc/info")
	@Ok("json")
	@Filters(@By(type = CrossOriginFilter.class))
	public Object api(@Param(value = "type", df = "1") int type) {

		List<ClassDoc> result = TaskService.findTaskList(type).stream().sorted((t1, t2) -> {
			/*int v = (int) (t1.getGroupId() - t2.getGroupId());
			if (v != 0) {
				return v;
			}*/
			return (int) (t1.getId() - t2.getId());
		}).map((t) -> {
			boolean compile = false;
			ClassDoc cd = null;
			try {
				new JavaRunner(t).compile();
				compile = true;
			} catch (Exception e1) {
				LOG.error(e1.getMessage(), e1);
			}
			try {
				cd = JavaDocUtil.parse(t.getCode());
				cd.getSub().stream().forEach(method -> {
					ExecuteMethod executeMethod = t.codeInfo().getExecuteMethod(method.getName());
					MethodDoc md = (MethodDoc) method;
					if (executeMethod.isRestful()) {
						if (executeMethod.getMethodTypeSet().size() == 0) {
							md.getMethods().addAll(DEFAULT_METHODS);
						} else {
							md.getMethods().addAll(executeMethod.getMethodTypeSet());
						}
					}
					if (executeMethod.isRpc()) {
						md.addMethod("RPC");
					}

				});

			} catch (Exception e) {
				e.printStackTrace();
				cd = new ClassDoc(t.getName());
			}
			cd.setGroup(t.getGroupName());
			cd.setStatus(compile);
			cd.setVersion(t.getVersion());
			cd.setDescription(t.getDescription());
			return cd;
		}).sorted((o1, o2) -> {
			int comp = o1.getGroup().compareToIgnoreCase(o2.getGroup());
			return comp == 0 ? o1.getName().compareToIgnoreCase(o2.getName()) : comp;
		}).collect(Collectors.toList());

		return result;

	}

	@At("/apidoc/validate")
	@Ok("json")
	public Restful validate(String code) throws ApiException, IOException {
		return Restful.ok().obj(StaticValue.RANDOM_CODE.equals(code));
	}


}
