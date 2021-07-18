package com.github.coco.utils;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.EndpointConfig;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class DockerNetworkHelper {
    /**
     * 网络名称
     */
    public static final String NETWORK   = "network";

    /**
     * IPv4地址
     */
    private static final String IPV4     = "ipv4";

    /**
     * IPv6地址
     */
    private static final String IPV6     = "ipv6";

    /**
     * MAC地址
     */
    private static final String MAC_ADDR = "macAddr";

    /**
     * 目标网络
     */
    private static final String TARGET   = "target";

    /**
     * 网络别名
     */
    private static final String ALIASES  = "aliases";

    /**
     * 生成容器的网络配置
     *
     * @param networkingConfig
     * @return
     */
    public static ContainerConfig.NetworkingConfig generateNetworkingConfig(Map<String, String> networkingConfig) {
        EndpointConfig.EndpointIpamConfig.Builder endpointIpamConfigBuilder = EndpointConfig.EndpointIpamConfig.builder();
        if (networkingConfig.containsKey(IPV4)) {
            endpointIpamConfigBuilder.ipv4Address(networkingConfig.get(IPV4));
        }
        if (networkingConfig.containsKey(IPV6)) {
            endpointIpamConfigBuilder.ipv6Address(networkingConfig.get(IPV6));
        }

        EndpointConfig.Builder endpointConfigBuilder = EndpointConfig.builder();
        if (networkingConfig.containsKey(MAC_ADDR)) {
            endpointConfigBuilder.macAddress(networkingConfig.get(MAC_ADDR));
        }
        endpointConfigBuilder.ipamConfig(endpointIpamConfigBuilder.build());

        Map<String, EndpointConfig> endpointsConfig = new HashMap<>(2);
        endpointsConfig.put(networkingConfig.get(NETWORK), endpointConfigBuilder.build());
        return ContainerConfig.NetworkingConfig.create(endpointsConfig);
    }

    /**
     * 生成Service的网络配置
     *
     * @param networkAttachmentConfigs
     * @return
     */
    public static List<NetworkAttachmentConfig> generateNetworkAttachmentConfig(List<Map<String, Object>> networkAttachmentConfigs) {
        List<NetworkAttachmentConfig> networkConfigs = new ArrayList<>();
        networkAttachmentConfigs.forEach(networkAttachmentConfig -> {
            NetworkAttachmentConfig.Builder builder = NetworkAttachmentConfig.builder();
            if (networkAttachmentConfig.containsKey(TARGET)) {
                builder.target(String.valueOf(networkAttachmentConfig.get(TARGET)));
            }
            if (networkAttachmentConfig.containsKey(ALIASES)) {
                builder.aliases(String.valueOf(networkAttachmentConfig.get(ALIASES)));
            }
            networkConfigs.add(builder.build());
        });
        return networkConfigs;
    }
}
