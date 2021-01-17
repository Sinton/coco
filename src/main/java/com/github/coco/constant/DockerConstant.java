package com.github.coco.constant;

/**
 * @author Yan
 */
public class DockerConstant {
    /**
     * 默认镜像仓库中心
     */
    public static final String DEFAULT_IMAGE_REGISTRY = "docker.io";
    /**
     * 镜像默认版本名称
     */
    public static final String DEFAULT_IMAGE_VERSION  = "latest";
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

    /**
     * Swarm应用栈默认文件名
     */
    public static final String SWARM_STACK_FILENAME   = "docker-stack.yml";

    /**
     * Compose应用栈默认文件名
     */
    public static final String COMPOSE_STACK_FILENAME = "docker-compose.yml";
}
