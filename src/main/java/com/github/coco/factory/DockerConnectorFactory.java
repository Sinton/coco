package com.github.coco.factory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Yan
 */
public class DockerConnectorFactory extends BasePooledObjectFactory<DockerClient> {
    private ConnectorConfig connectorConfig;

    public DockerConnectorFactory(ConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public DockerConnectorFactory() {
        DockerHost dockerHost = DockerHost.fromEnv();
        this.connectorConfig.setHost(dockerHost.host());
        this.connectorConfig.setPort(dockerHost.port());
    }

    @Override
    public DockerClient create() {
        String uri = String.format("http://%s:%s", connectorConfig.getHost(), connectorConfig.getPort());
        return DefaultDockerClient.builder()
                                  .uri(uri)
                                  .connectTimeoutMillis(50 * 1000)
                                  .readTimeoutMillis(50 * 1000)
                                  .build();
    }

    @Override
    public PooledObject<DockerClient> wrap(DockerClient obj) {
        return new DefaultPooledObject(obj);
    }
}
