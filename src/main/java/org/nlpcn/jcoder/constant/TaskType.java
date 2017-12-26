package org.nlpcn.jcoder.constant;

/**
 * 任务类型
 * 0:垃圾
 * 1:独立
 * 2:计划
 * 3:调度
 */
public enum TaskType {

    RECYCLE(0), API(1), CRON(2);

    private int value;

    TaskType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
