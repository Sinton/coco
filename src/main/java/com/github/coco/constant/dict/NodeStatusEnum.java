package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum NodeStatusEnum {
    /**
     * 运行中
     */
    RUNNING(1);

    int status;

    NodeStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
