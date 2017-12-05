package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.domain.Token;

/**
 * Created by Ansj on 05/12/2017.
 */

public interface SharedSpaceService {

	/**
	 * 增加一个任务到任务队列
	 * @param name
	 */
	void add2TaskQueue(String name);

	/**
	 * 计数器，记录task成功失败个数
	 * @param id
	 * @param success
	 */
	void counter(Long id, boolean success);

	/**
	 * 获得一个task成功次数
	 * @param id
	 * @return
	 */
	long getSuccess(Long id);

	/**
	 * 获得一个task失败次数
	 * @param id
	 * @return
	 */
	long getErr(Long id);

	/**
	 * 获得一个token
	 * @param key
	 * @return
	 */
	Token getToken(String key);

	/**
	 * 注册一个token
	 * @param token
	 */
	void regToken(Token token);

	/**
	 * 移除一个token
	 * @param key
	 * @return
	 */
	Token removeToken(String key);

	/**
	 * 获得一个可执行的任务
	 */
	Long poll() throws InterruptedException;
}
