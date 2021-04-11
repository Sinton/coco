package com.github.coco.config;

import com.github.coco.interceptor.AuthenticateInterceptor;
import com.github.coco.interceptor.ContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yan
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private static final String PATH_PATTERNS = "/**";
    private static final String[] PATH_WHITE_LIST = {"/**/login", "/**/logout", "/**/register", "/**/twoFactor"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(PATH_PATTERNS)
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器
        registry.addInterceptor(new AuthenticateInterceptor())
                .addPathPatterns(PATH_PATTERNS)
                .excludePathPatterns(PATH_WHITE_LIST);
        registry.addInterceptor(new ContextInterceptor())
                .addPathPatterns(PATH_PATTERNS)
                .excludePathPatterns(PATH_WHITE_LIST);
    }
}
