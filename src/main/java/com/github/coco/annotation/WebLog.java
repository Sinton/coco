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
}
