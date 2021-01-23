package com.github.coco.factory;

import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.core.AppContext;
import com.github.coco.entity.Endpoint;
import com.github.coco.service.EndpointService;
import com.github.coco.utils.DateHelper;
import com.github.coco.utils.EnumHelper;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Yan
 */
@Slf4j
public class DockerConnectorFactory extends BaseKeyedPooledObjectFactory<Integer, DockerClient> {
    private EndpointService endpointService = AppContext.getBean(EndpointService.class);

    @Override
    public DockerClient create(Integer endpointId) {
        Endpoint endpoint = endpointService.getEndpoint(Endpoint.builder().id(endpointId).build());
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
