package com.github.coco.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yan
 */
public class ErrorConstant {
    public static Map<Integer, String> ErrorMapping = new HashMap<>(16);
    /**
     * API接口调用发生错误异常
     */
    public static final int ERR_API_INVOKE         = 4000;

    /**
     * 业务执行出现未知异常错误
     */
    public static final int ERR_BASE_COMMON        = 4000;

    /**
     * Token失效错误异常
     */
    public static final int ERR_AUTH_TOEKN_EXPIRED = 9026;

    static {
        ErrorMapping.put(ERR_API_INVOKE, "API接口调用发生错误异常");
        ErrorMapping.put(ERR_BASE_COMMON, "业务执行出现未知错误异常");
        ErrorMapping.put(ERR_AUTH_TOEKN_EXPIRED, "Token失效错误异常");
    }
}
