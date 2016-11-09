package org.nlpcn.jcoder.util;

import java.util.concurrent.ExecutionException;

/**
 * api 错误信息
 * 
 * @author ansj
 * 
 */
public class ApiException extends ExecutionException {

	private static final long serialVersionUID = 1L;

	public static final int OK = 200;
	
	/**
	 * 没有权限
	 */
	public static final int Unauthorized = 401;
	/**
	 * 不允许
	 */
	public static final int Forbidden = 403;
	/**
	 * 不允许
	 */
	public static final int NotFound = 404;

	/**
	 * 服务器繁忙
	 */
	public static final int ServerBusy = 420;
	
	/**
	 * 请求格式不正确
	 */
	public static final int UnprocessableEntity = 422;

	/**
	 * 服务器错误
	 */
	public static final int ServerException = 500;

	private int status;

	public ApiException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

}
