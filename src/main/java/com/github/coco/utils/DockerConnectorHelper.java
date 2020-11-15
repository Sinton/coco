package com.github.coco.utils;

import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.factory.DockerConnectorFactory;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author Yan
 */
public class DockerConnectorHelper {
    private static GenericObjectPool<DockerClient> dockerClientPool;

    public static DockerClient borrowDockerClient() {
        return borrowDockerClient(null);
    }

    public static DockerClient borrowDockerClient(String ip, int port) {
        Endpoint endpoint = Endpoint.builder()
                                    .publicIp(ip)
                                    .port(port)
                                    .tlsEnable(WhetherEnum.NO.getCode())
                                    .build();
        return borrowDockerClient(endpoint);
    }

    public static DockerClient borrowDockerClient(Endpoint endpoint) {
        try {
            dockerClientPool = getDockerClientPool(endpoint);
            return dockerClientPool.borrowObject();
        } catch (Exception e) {
            LoggerHelper.fmtError(DockerConnectorHelper.class, e, "获取%s的Docker连接对象失败", endpoint.getPublicIp());
            return null;
        }
    }

    public static void returnDockerClient(Endpoint endpoint, DockerClient dockerClient) {
        try {
            dockerClientPool.returnObject(dockerClient);
        } catch (Exception e) {
            LoggerHelper.fmtError(DockerConnectorHelper.class, e, "归还%s的Docker连接对象失败", endpoint.getPublicIp());
        }
    }

    private static GenericObjectPool<DockerClient> getDockerClientPool(Endpoint endpoint) {
        if (dockerClientPool == null) {
            if (endpoint != null) {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory(endpoint));
            } else {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory());
            }
        }
        return dockerClientPool;
    }
}
