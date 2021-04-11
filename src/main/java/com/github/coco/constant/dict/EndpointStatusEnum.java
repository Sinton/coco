package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum EndpointStatusEnum {
    /**
     * 失联下线
     */
    DOWN(0),
    /**
     * 活跃在线
     */
    UP(1);

    int code;

    EndpointStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
