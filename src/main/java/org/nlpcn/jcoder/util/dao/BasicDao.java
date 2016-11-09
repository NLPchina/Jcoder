package org.nlpcn.jcoder.util.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.nutz.castor.Castors;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.ConnCallback;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.Record;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.dao.util.cri.SqlExpression;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * 基本数据库操作类
 * 
 * @author Administrator
 */
public class BasicDao {

	private DruidDataSource ds;

	public BasicDao(String jdbcUrl, String username, String password) {
		ds = new DruidDataSource();
		ds.setUrl(jdbcUrl);
		ds.setUsername(username);
		ds.setPassword(password);
		this.dao = new NutDao(ds);
	}

	public BasicDao(DataSource ds) {
		this.dao = new NutDao(ds);
	}

	public BasicDao() {

	}

	@Inject
	protected Dao dao;

	public Dao getDao() {
		return dao;
	}

	public Pager createPager(int currentPage, int pageSize) {
		return dao.createPager(currentPage, pageSize);
	}

	/**
	 * 根据Id删除数据
	 * 
	 * @param <T>
	 * @param id 持久化Id
	 * @return true 成功删除一条数据,false删除失败
	 */
	public <T> boolean delById(long id, Class<T> c) {
		return dao.delete(c, id) == 1;
	}

	/**
	 * 根据ID查询一个对象
	 * 
	 * @param <T>
	 * @param id 持久化Id
	 * @param c 要查询的表
	 * @return 查询到的对象
	 */
	public <T> T find(int id, Class<T> c) {
		return dao.fetch(c, id);
	}

	/**
	 * 根据ID查询一个对象
	 * 
	 * @param <T>
	 * @param id 持久化Id
	 * @param c 要查询的表
	 * @return 查询到的对象
	 */
	public <T> T find(long id, Class<T> c) {
		return dao.fetch(c, id);
	}

	/**
	 * 查询数据库中的全部数据
	 * 
	 * @param <T>
	 * @param c 查询的表
	 * @param orderby desc 排序的条件
	 * @return List
	 */
	public <T> List<T> search(Class<T> c, String orderby) {
		return dao.query(c, Cnd.orderBy().desc(orderby), null);

	}

	/**
	 * 根据条件查询数据库中满足条件的数据
	 * 
	 * @param <T>
	 * @param c
	 * @param condition
	 * @return
	 */
	public <T> List<T> search(Class<T> c, Condition condition) {
		return dao.query(c, condition);
	}

	/**
	 * 分页查询表中所有数据
	 * 
	 * @param <T>
	 * @param c 查询的表
	 * @param currentPage 当前页数
	 * @param pageSize 每页显示数量
	 * @param orderby desc排序的条件
	 * @return List
	 */
	public <T> List<T> searchByPage(Class<T> c, int currentPage, int pageSize, String orderby) {
		Pager pager = dao.createPager(currentPage, pageSize);

		return dao.query(c, Cnd.orderBy().desc(orderby), pager);
	}

	/**
	 * 分页带条件查询所有数据
	 * 
	 * @param <T>
	 * @param c 查询的表
	 * @param condition 查询条件,用Cnd的静态方法构造
	 * @param currentPage 当前页码
	 * @param pageSize 每页显示的数据量
	 * @return List
	 */
	public <T> List<T> searchByPage(Class<T> c, Condition condition, int currentPage, int pageSize) {
		Pager pager = dao.createPager(currentPage, pageSize);

		return dao.query(c, condition, pager);
	}

	/**
	 * 修改一条数据
	 * 
	 * @param <T>
	 * @param t 修改数据库中的数据
	 * @return true 修改成功,false 修改失败
	 * @throws Exception
	 */
	public <T> boolean update(T t) throws Exception {
		return dao.updateIgnoreNull(t) == 1;
	}

	/**
	 * 根据条件修改指定数据
	 * 
	 * @param <T>
	 * @param c 数据库表
	 * @param chain 修改的内容
	 * @param condition 选择条件
	 * @return true 成功,false失败
	 */
	public <T> boolean update(Class<T> c, Chain chain, Condition condition) {
		return dao.update(c, chain, condition) > 0;
	}

	/**
	 * 增加一条数据
	 * 
	 * @param <T>
	 * @param t
	 * @return 返回增加到数据库的这条数据
	 * @throws Exception
	 */
	public <T> T save(T t) throws Exception {
		return dao.insert(t);
	}

	public void save(String table, Chain chain) {
		dao.insert(table, chain);
	}

	/**
	 * 查询数据库中的数据条数
	 * 
	 * @param <T>
	 * @param c 查询的数据库表
	 * @return int
	 */
	public <T> int searchCount(Class<T> c) {
		return dao.count(c);
	}

	/**
	 * 根据条件查询数据库中的数据条数
	 * 
	 * @param <T>
	 * @param c 查询的数据库表
	 * @param condition 条件,用Cnd的静态方法构造
	 * @return int
	 */
	public <T> int searchCount(Class<T> c, Condition condition) {
		return dao.count(c, condition);
	}

	/**
	 * 计算最大分页数
	 * 
	 * @param count 记录总数
	 * @param pageSize 每页显示多少数据
	 * @return int
	 */
	public int maxPageSize(int count, int pageSize) {
		if (pageSize > 0) {
			if ((count % pageSize) != 0) {
				return (count / pageSize) + 1;
			} else {
				return (count / pageSize);
			}
		}
		return 0;
	}

	/**
	 * 根据多个id 查询数据
	 * 
	 * @param <T>
	 * @param ids 要查询的id,多个用","（逗号）分隔
	 * @param c 要查询的表信息
	 * @return List
	 */
	public <T> List<T> searchByIds(Class<T> c, String ids, String orderby) {
		Entity<T> entity = dao.getEntity(c);

		String id = entity.getIdField().getColumnName();

		String sql = " " + id + " in (" + ids + ") order by " + orderby + " desc";

		return dao.query(c, Cnd.wrap(sql), null);

	}

	/**
	 * 根据多个id 查询数据
	 * 
	 * @param <T>
	 * @param ids 整形的id数组
	 * @param c 要查询的表信息
	 * @return List
	 */
	public <T> List<T> searchByIds(Class<T> c, int[] ids, String orderby) {
		Entity<T> entity = dao.getEntity(c);

		String id = entity.getIdField().getColumnName();

		return dao.query(c, Cnd.where(id, "in", ids).desc(orderby), null);

	}

	/**
	 * 根据多个id删除数据
	 * 
	 * @param <T>
	 * @param c 要操作的表信息
	 * @param ids 要删除的id,多个用","（逗号）分隔
	 * @return true 成功,false 失败
	 */
	public <T> void deleteByIds(Class<T> c, String ids) {
		Entity<T> entity = dao.getEntity(c);

		String table = entity.getTableName();

		String id = entity.getIdField().getColumnName();

		Sql sql = Sqls.create("delete from " + table + " where " + id + " in(" + ids + ")");
		dao.execute(sql);
	}

	/**
	 * 根据条件返回一个条件
	 * 
	 * @param <T>
	 * @param condition 查询条件用Cnd构造
	 * @return T
	 */
	public <T> T findByCondition(Class<T> c, Condition condition) {
		return dao.fetch(c, condition);
	}

	/**
	 * 根据某个条件分页查询数据
	 * 
	 * @param <T>
	 * @param c 查询的表
	 * @param fieldName 匹配字段名
	 * @param value 匹配的值
	 * @param currentPage 当前页码
	 * @param pageSize 每页数据量
	 * @return List
	 */
	public <T> List<T> searchByPage(Class<T> c, String fieldName, String value, int currentPage, int pageSize) {
		Entity<T> entity = dao.getEntity(c);

		String column = entity.getField(fieldName).getColumnName();

		Pager pager = dao.createPager(currentPage, pageSize);

		return dao.query(c, Cnd.where(column, "=", value), pager);
	}

	/**
	 * 分页带条件查询所有数据
	 * 
	 * @param <T>
	 * @param c 查询的表
	 * @param condition 查询条件,用Cnd的静态方法构造
	 * @param currentPage 当前页码
	 * @param pageSize 每页显示的数据量
	 * @return List
	 */
	public <T> List<T> searchByPage(Class<T> c, Condition condition, Pager pager) {
		return dao.query(c, condition, pager);
	}

	/**
	 * 根据指定条件返回一个对象
	 * 
	 * @param <T>
	 * @param fileName 匹配名称
	 * @param value 匹配值
	 * @return T
	 */
	public <T> T findByCondition(Class<T> c, String fileName, String value) {
		return dao.fetch(c, Cnd.where(fileName, "=", value));
	}

	/**
	 * 添加一条数据到数据库中， 该数据包括关联的多个其他数据
	 * 
	 * @param <T>
	 * @param t 插入数据库的对象
	 * @param fieldName 关联数据的字段名，一般为List对象
	 * @return T
	 */
	public <T> T saveWidth(T t, String fieldName) {
		return dao.insertWith(t, fieldName);

	}

	/**
	 * 获取关联对象
	 * 
	 * @param <T>
	 * @param t 查询的对象
	 * @param fieldName 关联的对象
	 * @return T
	 */
	public <T> T findLink(T t, String fieldName) {
		return dao.fetchLinks(t, fieldName);
	}

	/**
	 * 更新自身和关联的对象
	 * 
	 * @param <T>
	 * @param t 修改的对象
	 * @param fieldName 关联对象
	 * @return T
	 */
	public <T> T updateWidth(T t, String fieldName) {
		return dao.updateWith(t, fieldName);
	}

	/**
	 * 仅修改关联的对象的数据
	 * 
	 * @param <T>
	 * @param t 查询条件
	 * @param fieldName 修改的对象
	 * @return T
	 */
	public <T> T updateLink(T t, String fieldName) {
		return dao.updateLinks(t, fieldName);
	}

	/**
	 * 删除自身和关联对象
	 * 
	 * @param <T>
	 * @param t 删除的对象
	 * @param fieldName 关联的对象
	 */
	public <T> void deleteWidth(T t, String fieldName) {
		dao.deleteWith(t, fieldName);
	}

	/**
	 * 根据指定条件删除对象
	 * 
	 * @param <T>
	 * @param Condition 匹配条件
	 * @return int
	 */
	public <T> int delByCondition(Class<T> c, Condition con) {
		return dao.clear(c, con);
	}

	/**
	 * 删除关联的对象，不删除自身
	 * 
	 * @param <T>
	 * @param t 删除的条件
	 * @param fieldName 删除的关联对象
	 */
	public <T> void deleteLink(T t, String fieldName) {
		dao.deleteLinks(t, fieldName);
	}

	/**
	 * 保存对象的多对多 关系
	 * 
	 * @param <T>
	 * @param t
	 * @param fieldName
	 */
	public <T> T saveRelation(T t, String fieldName) {
		return dao.insertRelation(t, fieldName);
	}

	/**
	 * 保存对象的关联关系,不保存对象本身
	 * 
	 * @param <T>
	 * @param t
	 * @param fieldName
	 * @return
	 */
	public <T> T saveLink(T t, String fieldName) {
		return dao.insertLinks(t, fieldName);
	}

	/**
	 * 更新对象的多对多关系
	 * 
	 * @param <T>
	 * @param c 更新的对象的类
	 * @param fieldName 更新的字段名称
	 * @param chain 更新的内容
	 * @param condition 更新的条件
	 * @return true 成功,false 失败
	 */
	public <T> boolean updateRelation(Class<T> c, String fieldName, Chain chain, Condition condition) {
		return dao.updateRelation(c, fieldName, chain, condition) > 0;
	}

	/**
	 * 对于 '@One' 和 '@Many'，对应的记录将会删除 而 '@ManyMay' 对应的字段，只会清除关联表中的记录
	 * 
	 * @param <T>
	 * @param t
	 * @param fieldName
	 * @return
	 */
	public <T> T clearRelation(T t, String fieldName) {
		return dao.clearLinks(t, fieldName);
	}

	/**
	 * 根据中间表分页查询数据
	 * 
	 * @param <T>
	 * @param c 查询主表
	 * @param joinTabel 中间表
	 * @param cloumnName 要获取中间表的字段
	 * @param condition 查询条件
	 * @param group 主查询条件组
	 * @param orderby 排序方式
	 * @param currentPage 当前页面
	 * @param pageSize 每页显示数据
	 * @return
	 */
	public <T> List<T> searchByRelation(Class<T> c, String joinTabel, String cloumnName, Condition condition, SqlExpressionGroup group, String orderby, int currentPage,
			int pageSize) {
		Entity<T> entity = dao.getEntity(c);

		List<Record> records = dao.query(joinTabel, condition, null);

		List<Integer> ids = new ArrayList<>();
		for (Record r : records) {
			int id = r.getInt(cloumnName);
			ids.add(id);
		}
		if (ids.size() == 0) {
			return null;
		}

		Pager pager = dao.createPager(currentPage, pageSize);

		SqlExpression e = Cnd.exp(entity.getIdField().getColumnName(), "in", Castors.me().castTo(ids, int[].class));

		ids = null;

		return dao.query(c, Cnd.where(group.and(e)).desc(orderby), pager);
	}

	/**
	 * 查询数据的总数
	 * 
	 * @param <T>
	 * @param c
	 * @param joinTabel 中间表
	 * @param cloumnName 要获取中间表的字段
	 * @param condition 查询条件
	 * @param group 主查询条件组
	 * @param orderby 排序方式
	 * @return
	 */
	public <T> int searchCount(Class<T> c, String joinTabel, String cloumnName, Condition condition, SqlExpressionGroup group, String orderby) {
		Entity<T> entity = dao.getEntity(c);

		List<Record> records = dao.query(joinTabel, condition, null);

		List<Integer> ids = new ArrayList<Integer>();
		for (Record r : records) {
			int id = r.getInt(cloumnName);
			ids.add(id);
		}
		if (ids.size() == 0) {
			return 0;
		}

		SqlExpression e = Cnd.exp(entity.getIdField().getColumnName(), "in", Castors.me().castTo(ids, int[].class));

		group = group.and(e);

		return dao.count(c, Cnd.where(group).desc(orderby));
	}

	public void delete(String table, Condition condition) {
		dao.clear(table, condition);
	}

	/**
	 * 运行一条sql.update or delete
	 * 
	 * @param sqlStr
	 */
	public void executeSql(String sqlStr) {
		Sql sql = Sqls.create(sqlStr);
		dao.execute(sql);
	}

	/**
	 * 根据sql语句构建对象
	 * 
	 * @param c
	 * @param sqlStr
	 * @return
	 */
	public <T> List<T> searchBySql(Class<T> c, String sqlStr) {
		// TODO Auto-generated method stub
		Sql sql = Sqls.queryEntity(sqlStr);
		sql.setEntity(dao.getEntity(c));
		dao.execute(sql);
		return sql.getList(c);
	}

	/**
	 * 回调方式执行sql
	 * 
	 * @param connCallback
	 */
	public void run(ConnCallback connCallback) {
		dao.run(connCallback);
	}

	/**
	 * 批量插入
	 * 
	 * @param all
	 */
	public <T> void saveAll(List<T> all) {
		dao.fastInsert(all);
	}

	/**
	 * 无pojo查询记录
	 * 
	 * @param tableName
	 * @param cnd
	 * @return
	 */
	public List<Record> query(String tableName, Cnd cnd) {
		return dao.query(tableName, cnd);
	}

	/**
	 * 无pojo查询记录
	 * 
	 * @param tableName
	 * @param cnd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Record> select(String sqlStr) {
		Sql sql = Sqls.create(sqlStr);
		dao.execute(sql.setCallback(new SqlCallback() {
			@Override
			public List<Record> invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
				// TODO Auto-generated method stub
				List<Record> result = new ArrayList<Record>();
				while (rs.next()) {
					result.add(Record.create(rs));
				}
				return result;
			}
		}));
		return (List<Record>) sql.getResult();
	}

	/**
	 * 无pojo查询记录
	 * 
	 * @param tableName
	 * @param cnd
	 * @return
	 */
	public Record selectOne(String sqlStr) {
		List<Record> select = this.select(sqlStr);
		if (select != null && select.size() > 0) {
			return select.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 删除全部
	 * 
	 * @param list
	 */
	public void deleteAll(List<?> list) {
		for (Object object : list) {
			dao.delete(object);
		}
	}

	/**
	 * 关闭数据源
	 */
	public void close() {
		if (ds != null) {
			ds.close();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

}