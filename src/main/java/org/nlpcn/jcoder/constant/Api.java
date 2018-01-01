package org.nlpcn.jcoder.constant;

/**
 * 内部API路径, 调用的方法命名约定: __xxx__
 */
public enum Api {

    TASK_LIST(Api.BASE_PATH + "/task/__list__"),
    TASK_CHECK(Api.BASE_PATH + "/task/__check__"),
    TASK_SAVE(Api.BASE_PATH + "/task/__save__"),
    TASK_DELETE(Api.BASE_PATH + "/task/__delete__"),
    TASK_TASK(Api.BASE_PATH + "/task/__task__"),
    TASK_CRON(Api.BASE_PATH + "/task/__cron__");

    private static final String BASE_PATH = "/admin";

    private String path;

    Api(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
