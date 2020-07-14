package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum ServiceStatusEnum {
    /**
     * 运行中
     */
    RUNNING(1),
    /**
     * 退出
     */
    EXIT(2);
    int status;

    ServiceStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
