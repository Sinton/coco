package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum CacheTypeEnum {
    /**
     * 鉴权令牌
     */
    TOKEN("TokenCache");

    String name;

    CacheTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}