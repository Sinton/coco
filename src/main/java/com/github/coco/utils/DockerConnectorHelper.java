package com.github.coco.utils;

import com.github.coco.entity.Endpoint;
import com.github.coco.factory.DockerConnectorFactory;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author Yan
 */
public class DockerConnectorHelper {

    public static DockerClient getDockerClient() {
        return getDockerClient(null);
    }

    public static DockerClient getDockerClient(String ip, int port) {
        Endpoint endpoint = Endpoint.builder()
                                    .ip(ip)
                                    .port(port)
                                    .build();
        return getDockerClient(endpoint);
    }

    public static DockerClient getDockerClient(Endpoint endpoint) {
        try {
            GenericObjectPool<DockerClient> dockerClientPool;
            if (endpoint != null) {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory(endpoint));
            } else {
                dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory());
            }
            return dockerClientPool.borrowObject();
        } catch (Exception e) {
            LoggerHelper.fmtError(DockerConnectorHelper.class, e, "获取Docker连接对象失败");
            return null;
        }
    }
}
