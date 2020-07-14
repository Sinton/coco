package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum ContainerStatusEnum {
    /**
     * 运行中
     */
    RUNNING(1, "running"),
    /**
     * 已创建
     */
    CREATED(2, "created"),
    /**
     * 已暂停
     */
    PAUSED(3, "paused"),
    /**
     * 已重启
     */
    RESTARTING(4, "restarting"),
    /**
     * 退出
     */
    EXITED(5, "exited");

    int code;
    String status;

    ContainerStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
