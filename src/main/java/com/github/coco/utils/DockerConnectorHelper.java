package com.github.coco.utils;

import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.factory.DockerConnectorFactory;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author Yan
 */
public class DockerConnectorHelper {
    private static GenericObjectPool<DockerClient> dockerClientPool;

    /**
     * 租借Docker客户端连接池
     * @return
     */
    public static DockerClient borrowDockerClient() {
        return borrowDockerClient(null);
    }

    /**
     * 租借Docker客户端连接池
     *
     * @param ip
     * @param port
     * @return
     */
    public static DockerClient borrowDockerClient(String ip, int port) {
        Endpoint endpoint = Endpoint.builder()
                                    .publicIp(ip)
                                    .port(port)
                                    .tlsEnable(WhetherEnum.NO.getCode())
                                    .endpointType(EndpointTypeEnum.URL.getCode())
                                    .build();
        return borrowDockerClient(endpoint);
    }

    /**
     * 租借Docker客户端连接池
     *
     * @param endpoint
     * @return
     */
    public static DockerClient borrowDockerClient(Endpoint endpoint) {
        try {
            dockerClientPool = generateDockerClientPool(endpoint);
            return dockerClientPool.borrowObject();
        } catch (Exception e) {
            LoggerHelper.fmtError(DockerConnectorHelper.class, e, "获取%s的Docker连接对象失败", endpoint.getPublicIp());
            return null;
        }
    }

    /**
     * 返还Docker客户端连接池
     *
     * @param dockerClient
     */
    public static void returnDockerClient(DockerClient dockerClient) {
        try {
            dockerClientPool.returnObject(dockerClient);
        } catch (Exception e) {
            LoggerHelper.fmtError(DockerConnectorHelper.class, e, "归还%s的Docker连接对象失败", dockerClient.getHost());
        }
    }

    /**
     * 生成Docker客户端连接池
     *
     * @param endpoint
     * @return
     */
    private static GenericObjectPool<DockerClient> generateDockerClientPool(Endpoint endpoint) {
        if (dockerClientPool == null) {
            if (endpoint != null) {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory(endpoint));
            } else {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory());
            }

            // 连接池配置
            GenericObjectPoolConfig<DockerClient> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxWaitMillis(DateHelper.SECOND * 10);
            poolConfig.setMinEvictableIdleTimeMillis(DateHelper.MINUTE);
            dockerClientPool.setConfig(poolConfig);

            // 抛弃条件配置
            AbandonedConfig abandonedConfig = new AbandonedConfig();
            abandonedConfig.setRemoveAbandonedOnBorrow(true);
            abandonedConfig.setRemoveAbandonedOnMaintenance(true);
            abandonedConfig.setRemoveAbandonedTimeout(DateHelper.MINUTE * 10);
            dockerClientPool.setAbandonedConfig(abandonedConfig);
        }
        return dockerClientPool;
    }
}
