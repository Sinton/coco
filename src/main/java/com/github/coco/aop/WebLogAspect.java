package com.github.coco.aop;

import com.github.coco.annotation.WebLog;
import com.github.coco.utils.LoggerHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author Yan
 */
@Aspect
@Component
public class WebLogAspect {
    @Pointcut("@annotation(com.github.coco.annotation.WebLog)")
    private void webLogPointCut() {
    }

    @Before("webLogPointCut()")
    private void doBefore(JoinPoint joinPoint) throws Exception {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取 @WebLog 注解的描述信息
        String methodDescription = getAspectDescription(joinPoint);

        // 打印请求相关参数
        LoggerHelper.fmtInfo(this.getClass(), "========================================== Start ==========================================");
        // 打印请求 url
        LoggerHelper.fmtInfo(this.getClass(), "URL            : {0}", request.getRequestURL().toString());
        // 打印描述信息
        LoggerHelper.fmtInfo(this.getClass(), "Description    : {0}", methodDescription);
        // 打印 Http method
        LoggerHelper.fmtInfo(this.getClass(), "HTTP Method    : {0}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        LoggerHelper.fmtInfo(this.getClass(), "Class Method   : {0}.{0}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        LoggerHelper.fmtInfo(this.getClass(), "IP             : {0}", request.getRemoteAddr());
        // 打印请求入参
        LoggerHelper.fmtInfo(this.getClass(), "Request Args   : {0}", joinPoint.getArgs());
    }

    @After("webLogPointCut()")
    private void doAfter(JoinPoint joinPoint) {
        // 接口结束后换行，方便分割查看
        LoggerHelper.fmtInfo(this.getClass(), "=========================================== End ===========================================");
    }

    @AfterReturning("webLogPointCut()")
    private void doReturning(JoinPoint joinPoint) {
    }

    /**
     * 拦截异常操作，有异常时执行
     *
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(value = "webLogPointCut()", throwing = "e")
    private void doThrowing(JoinPoint joinPoint, Exception e) {
    }

    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLogPointCut()")
    private Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
        LoggerHelper.fmtInfo(this.getClass(), "Response Args  : {0}", result);
        // 执行耗时
        LoggerHelper.fmtInfo(this.getClass(), "Time-Consuming : {0} ms", System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 获取切面注解的描述
     *
     * @param joinPoint 切点
     * @return 描述信息
     * @throws Exception
     */
    private String getAspectDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class<?> targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description.append(method.getAnnotation(WebLog.class).description());
                    break;
                }
            }
        }
        return description.toString();
    }
}
