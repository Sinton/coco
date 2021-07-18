package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum SwarmSchedulingModeEnum {
    /**
     * 全局模式
     */
    GLOBAL(1, "global"),
    /**
     * 副本模式
     */
    REPLICATED(2, "replicated");

    int code;
    String name;

    SwarmSchedulingModeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }
}
