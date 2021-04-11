package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum TimeFetchEnum {
    /**
     * 全部
     */
    ALL(1, "all"),
    /**
     * 最近一天
     */
    LAST_DAY(2, "lastday"),
    /**
     * 最近4小时
     */
    LAST_4_HOURS(3, "last4hours"),
    /**
     * 最近1小时
     */
    LAST_HOUR(4, "lasthour"),
    /**
     * 最近10分钟
     */
    LAST_10_MIN(5, "last10min");

    int code;
    String value;

    TimeFetchEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
