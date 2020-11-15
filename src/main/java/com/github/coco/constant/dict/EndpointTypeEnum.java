package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum EndpointTypeEnum {
    /**
     * 本地unix
     */
    UNIX(1, "unix"),
    /**
     * http远程URL
     */
    URL(2, "url"),
    /**
     * 代理
     */
    AGENT(2, "agent");
    int code;
    String type;

    EndpointTypeEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }
}
