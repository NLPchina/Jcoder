package cn.com.infcn.api.test;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;

@Single(true)
public class TaskDemo {

	@Inject
	private Logger log;

	int i = 0;

	/**
	 * 测试
	 * 
	 * @return {"FBI":[{"name":"rose","age":"25"},{"name":"jack","age":"23"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}]}
	 */
	@DefaultExecute
	@Filters(@By(type = TokenFilter.class, args = { "false" }))
	public Object execute(String name, Integer age, Character sex) {
		return "name:" + name +" age:"+age+" sex:"+sex;
	}
}
