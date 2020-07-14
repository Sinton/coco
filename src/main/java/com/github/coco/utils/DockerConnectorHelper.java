package com.github.coco.utils;

import com.github.coco.factory.ConnectorConfig;
import com.github.coco.factory.DockerConnectorFactory;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author Yan
 */
public class DockerConnectorHelper {

    public static DockerClient getDockerClient(String host, int port) {
        try {
            ConnectorConfig connectorConfig = ConnectorConfig.builder()
                                                             .host(host)
                                                             .port(port)
                                                             .build();
            return getDockerClient(connectorConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DockerClient getDockerClient(ConnectorConfig connectorConfig) {
        try {
            GenericObjectPool<DockerClient> dockerClientPool = new GenericObjectPool<>(new DockerConnectorFactory(connectorConfig));
            return dockerClientPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
