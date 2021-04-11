package com.github.coco.interceptor;

import com.alibaba.fastjson.JSON;
import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.core.AppContext;
import com.github.coco.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Yan
 */
@Slf4j
@Component
public class AuthenticateInterceptor implements HandlerInterceptor {
    private GlobalCache globalCache = AppContext.getBean(GlobalCache.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 判断token是否为空是否有效
        String token = request.getHeader(GlobalConstant.ACCESS_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            if (globalCache.getUser(token) != null) {
                return true;
            } else {
                printContent(response);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    private void printContent(HttpServletResponse response) {
        String content = JSON.toJSONString(new ApiResponseDTO().returnResult(GlobalConstant.SUCCESS_CODE, "token过期,请重新登陆"));
        try {
            response.reset();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Cache-Control", "no-store");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            PrintWriter pw = response.getWriter();
            pw.write(content);
            pw.flush();
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
