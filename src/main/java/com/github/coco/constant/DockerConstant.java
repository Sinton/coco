package com.github.coco.constant;

/**
 * @author Yan
 */
public class DockerConstant {
    /**
     * 默认镜像仓库中心
     */
    public static final String IMAGE_DEFAULT_REGISTRY = "docker.io";
    /**
     * 镜像默认版本名称
     */
    public static final String IMAGE_DEFAULT_VERSION  = "latest";
    /**
     * 服务标签
     */
    public static final String SERVICE_LABEL          = "com.docker.swarm.service.id";
    /**
     * Swarm应用栈标签
     */
    public static final String SWARM_STACK_LABEL      = "com.docker.stack.namespace";
    /**
     * Compose应用栈标签
     */
    public static final String COMPOSE_STACK_LABEL    = "com.docker.compose.project";
}
