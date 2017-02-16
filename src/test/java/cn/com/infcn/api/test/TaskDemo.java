package cn.com.infcn.api.test;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nutz.ioc.loader.annotation.Inject;

@Single(true)
public class TaskDemo {

	@Inject
	private Logger log;
	
	int i = 0 ;

	/**
	 * 测试
	 * 
	 * @return {"FBI":[{"name":"rose","age":"25"},{"name":"jack","age":"23"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}],"NBA":[{"name":"tom","sex":"man"},{"name":"jack","sex":"women"}]}
	 */
	@DefaultExecute
	public Object execute() {
		return i++;
	}
}
