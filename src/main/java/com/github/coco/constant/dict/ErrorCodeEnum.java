package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum ErrorCodeEnum {
    /**
     * 成功
     */
    SUCCESS(2000, "成功"),
    /**
     * 异常
     */
    EXCEPTION(4000, "异常");

    int code;
    String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
