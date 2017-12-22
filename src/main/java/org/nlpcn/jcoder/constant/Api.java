package org.nlpcn.jcoder.constant;

/**
 * 内部API路径, 调用的方法命名约定: __xxx__
 */
public enum Api {

    TASK_CHECK(Api.BASE_PATH + "/task/__check__"),
    TASK_SAVE(Api.BASE_PATH + "/task/__save__");

    private static final String BASE_PATH = "/admin";

    private String path;

    Api(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
