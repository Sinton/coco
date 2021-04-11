package com.github.coco.annotation;

import java.lang.annotation.*;

/**
 * @author Yan
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebLog {
    /**
     * 日志描述信息
     *
     * @return
     */
    String description() default "";

    /**
     * 请求入参
     *
     * @return
     */
    boolean requestArgs() default true;

    /**
     * 相应出参
     *
     * @return
     */
    boolean responseArgs() default false;
}
