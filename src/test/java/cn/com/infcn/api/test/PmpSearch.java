package cn.com.infcn.api.test;


import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class PmpSearch {
	@Inject
	private Logger LOG;


	/**
	 * 查询栏目列表
	 *
	 * @param section  栏目code
	 * @param sitecode 网站code
	 * @return
	 */
	@Execute
	@Filters(@By(type = TokenFilter.class, args = {"false"}))
	public Restful searchSectionList(String section, String sitecode) throws ExecutionException {
		return null;
	}

	/**
	 * 搜索提示
	 *
	 * @param word 检索词或拼音
	 * @param size 提示词个数, 默认10
	 */
	@Execute
	public Restful suggest(@Param("word") String word, @Param(value = "size", df = "10") int size) throws ApiException, IOException {
		return null;
	}

	/**
	 * 查询
	 *
	 * @param sectioncode 栏目名称|模版名称,栏目名称1|模版名称1
	 * @param from        开始条数
	 * @param size        每页显示多少条
	 * @param query       查询语句
	 * @param highlight   高亮条件 {keyword:['体育'],query:'体育',begin:'<begin>',end:'<end>',fields:[{field:'content',dist_field:'content_hl',size:0}]}
	 * @param includes    包含
	 * @param excludes    不包含
	 * @param distinct    去重
	 * @param sort        排序
	 * @return
	 * @throws ApiException
	 * @throws IOException
	 */
	@Execute
	@Filters(@By(type = TokenFilter.class, args = {"false"}))
	public Restful search(String[] sectioncode, int from, @Param(value = "size", df = "10") Integer size, String query, String filter, String highlight, String includes,
	                      String excludes, String distinct, String sort) throws ExecutionException, IOException {
		return null;

	}

	/**
	 * 聚合查询
	 *
	 * @param sectioncode 栏目名称|模版名称,栏目名称1|模版名称1
	 * @param field       用哪些字段分面，多字段逗号隔开
	 * @param size        返回条数
	 * @param query       查询语句
	 * @return
	 * @throws ApiException
	 * @throws IOException
	 */
	@Execute
	@Filters(@By(type = TokenFilter.class, args = {"false"}))
	public Restful aggregations(String[] sectioncode, int featchSize, String field, @Param(value = "size", df = "10") int size, String query, String filter)
			throws ExecutionException, IOException {

		return null;
	}

	/**
	 * 根据table查询数据
	 *
	 * @param tableName 表名称
	 * @param includes  包含字段
	 * @param excludes  不包含字段
	 * @param id        数据id
	 * @return 数据内容
	 * @throws ExecutionException
	 */
	@Execute
	@Filters(@By(type = TokenFilter.class, args = {"false"}))
	public Restful findById(String tableName, String includes, String excludes, String id) throws ExecutionException {
		return null;
	}

	/**
	 * 根据ids获取多条数据
	 *
	 * @param client
	 */
	@Execute
	@Filters(@By(type = TokenFilter.class, args = {"false"}))
	public Restful queryRecordsByIds(String tableName, String includes, String excludes, String ids) {
		return null;
	}

	/**
	 * 多个栏目组合为一个dsl
	 *
	 * @param sectioncode
	 * @param query
	 * @param filter
	 * @param tableNames
	 * @return
	 * @throws ExecutionException
	 * @throws IOException
	 */
	private String makeDslQuery(String[] sectioncode, String query, String filter, Set<String> tableNames) throws ExecutionException, IOException {
		return null;

	}


}
