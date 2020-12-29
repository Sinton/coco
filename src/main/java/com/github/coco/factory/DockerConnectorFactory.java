package com.github.coco.factory;

import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Endpoint;
import com.github.coco.utils.DateHelper;
import com.github.coco.utils.EnumHelper;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import com.spotify.docker.client.exceptions.DockerCertificateException;
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
        DefaultDockerClient.Builder builder = new DefaultDockerClient.Builder();
        try {
            EndpointTypeEnum endpointType = EnumHelper.getEnumType(EndpointTypeEnum.class,
                                                                   endpoint.getEndpointType().toString(),
                                                                   EndpointTypeEnum.UNIX,
                                                                   "getCode");
            switch (endpointType) {
                case UNIX:
                    builder = DefaultDockerClient.fromEnv();
                    break;
                case URL:
                    String uri;
                    if (StringUtils.isNotBlank(endpoint.getPublicIp())) {
                        String scheme = endpoint.getTlsEnable().equals(WhetherEnum.YES.getCode()) ? "https" : "http";
                        uri = String.format("%s://%s:%s",
                                            scheme,
                                            endpoint.getPublicIp(),
                                            endpoint.getPort() != null ? endpoint.getPort()
                                                                       : DockerHost.defaultPort());
                    } else {
                        uri = String.format("http://%s:%s", DockerHost.defaultAddress(), DockerHost.defaultPort());
                    }
                    builder = DefaultDockerClient.builder().uri(uri);
                default:
                    break;
            }
        } catch (DockerCertificateException e) {
            return null;
        }
        return builder.connectTimeoutMillis(DateHelper.SECOND * 10)
                      .readTimeoutMillis(DateHelper.SECOND * 10)
                      .build();
    }

    @Override
    public PooledObject<DockerClient> wrap(DockerClient dockerClient) {
        return new DefaultPooledObject<>(dockerClient);
    }
}
