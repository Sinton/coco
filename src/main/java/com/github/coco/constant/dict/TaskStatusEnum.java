package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum TaskStatusEnum {
    /**
     * 运行中
     */
    RUNNING(1);

    int status;

    TaskStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
