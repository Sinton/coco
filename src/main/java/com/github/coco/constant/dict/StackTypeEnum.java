package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum StackTypeEnum {
    /**
     * docker-compose应用栈
     */
    COMPOSE(1),
    /**
     * swarm集群应用栈
     */
    SWARM(2);

    int code;

    StackTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
