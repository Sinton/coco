package com.github.coco.factory;

import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Endpoint;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Yan
 */
public class DockerConnectorFactory extends BasePooledObjectFactory<DockerClient> {
    private Endpoint endpoint;

    public DockerConnectorFactory(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public DockerConnectorFactory() {
        this.endpoint.setPublicIp(DockerHost.defaultAddress());
        this.endpoint.setPort(DockerHost.defaultPort());
    }

    @Override
    public DockerClient create() {
        String uri;
        if (StringUtils.isNotBlank(endpoint.getPublicIp())) {
            String scheme = endpoint.getTlsEnable().equals(WhetherEnum.YES.getCode()) ? "https" : "http";
            uri = String.format("%s://%s:%s",
                                scheme,
                                endpoint.getPublicIp(),
                                endpoint.getPort() != null ? endpoint.getPort() : DockerHost.defaultPort());
        } else {
            uri = String.format("http://%s:%s", DockerHost.defaultAddress(), DockerHost.defaultPort());
        }
        return DefaultDockerClient.builder()
                                  .uri(uri)
                                  .connectTimeoutMillis(10 * 1000)
                                  .readTimeoutMillis(10 * 1000)
                                  .build();
    }

    @Override
    public PooledObject<DockerClient> wrap(DockerClient dockerClient) {
        return new DefaultPooledObject<>(dockerClient);
    }
}
