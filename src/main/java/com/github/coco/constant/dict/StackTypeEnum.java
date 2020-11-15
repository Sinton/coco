package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum StackTypeEnum {
    /**
     * 运行中
     */
    SWARM(1),
    COMPOSE(2);

    int code;

    StackTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
