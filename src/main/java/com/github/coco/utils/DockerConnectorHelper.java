package com.github.coco.utils;

import com.github.coco.entity.Endpoint;
import com.github.coco.factory.DockerConnectorFactory;
import com.spotify.docker.client.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * @author Yan
 */
@Slf4j
public class DockerConnectorHelper {
    private static GenericKeyedObjectPool<Integer, DockerClient> dockerClientPool;

    /**
     * 租借Docker客户端连接池
     *
     * @param endpoint
     * @return
     */
    public static DockerClient borrowDockerClient(Endpoint endpoint) {
        try {
            dockerClientPool = generateDockerClientPool();
            return dockerClientPool.borrowObject(endpoint.getId());
        } catch (Exception e) {
            log.error(String.format("获取%s的Docker连接对象失败", endpoint), e);
            return null;
        }
    }

    /**
     * 返还Docker客户端连接池
     *
     * @param endpoint
     * @param dockerClient
     */
    public static void returnDockerClient(Endpoint endpoint, DockerClient dockerClient) {
        try {
            dockerClientPool.returnObject(endpoint.getId(), dockerClient);
        } catch (Exception e) {
            log.error(String.format("归还%s的Docker连接对象失败", dockerClient.getHost()), e);
        }
    }

    /**
     * 生成Docker客户端连接池
     *
     * @return
     */
    private static GenericKeyedObjectPool<Integer, DockerClient> generateDockerClientPool() {
        if (dockerClientPool == null) {
            // 连接池配置
            GenericKeyedObjectPoolConfig<DockerClient> poolConfig = new GenericKeyedObjectPoolConfig<>();
            poolConfig.setMaxWaitMillis(DateHelper.SECOND * 10);
            poolConfig.setMinEvictableIdleTimeMillis(DateHelper.MINUTE);
            dockerClientPool = new GenericKeyedObjectPool<>(new DockerConnectorFactory(), poolConfig);
        }
        return dockerClientPool;
    }
}
