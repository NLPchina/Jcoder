package org.nlpcn.jcoder.constant;

/**
 * 任务状态
 * 0:停止
 * 1:运行
 */
public enum TaskStatus {

	STOP(0), ACTIVE(1);

	private int value;

	TaskStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
