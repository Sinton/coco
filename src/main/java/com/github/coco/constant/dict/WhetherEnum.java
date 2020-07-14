package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum WhetherEnum {
    /**
     * 是
     */
    YES(1, "是", true),
    /**
     * 否
     */
    NO(0, "否", false);
    int code;
    String desc;
    boolean value;

    WhetherEnum(int code, String desc, boolean value) {
        this.code = code;
        this.desc = desc;
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
