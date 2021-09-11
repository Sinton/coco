package com.github.coco.utils.docker;

import com.github.coco.utils.EnumHelper;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.UpdateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class DockerServiceHelper {
    /**
     * 端口配置名称
     */
    private static final String NAME           = "name";

    /**
     * 暴露端口
     */
    private static final String PUBLISHED_PORT = "publishedPort";

    /**
     * 容器端口
     */
    private static final String TARGET_PORT    = "targetPort";

    /**
     * 端口协议
     */
    private static final String PROTOCOL       = "protocol";

    /**
     * 端口发布模式
     */
    private static final String PUBLISH_MODE   = "publishMode";

    /**
     * 生成端口配置
     *
     * @param portConfigs
     * @return
     */
    public static List<PortConfig> generatePortConfig(List<Map<String, String>> portConfigs) {
        List<PortConfig> configs = new ArrayList<>();
        portConfigs.forEach(portConfig -> {
            PortConfig.Builder portConfigBuilder = PortConfig.builder();
            if (portConfig.containsKey(NAME)) {
                portConfigBuilder.name(portConfig.get(NAME));
            }
            if (portConfig.containsKey(PUBLISHED_PORT)) {
                portConfigBuilder.publishedPort(Integer.parseInt(portConfig.get(PUBLISHED_PORT)));
            }
            if (portConfig.containsKey(TARGET_PORT)) {
                portConfigBuilder.targetPort(Integer.parseInt(portConfig.get(TARGET_PORT)));
            }
            if (portConfig.containsKey(PROTOCOL)) {
                portConfigBuilder.protocol(portConfig.get(PROTOCOL));
            }
            if (portConfig.containsKey(PUBLISH_MODE)) {
                PortConfig.PortConfigPublishMode publishMode = EnumHelper.getEnumType(PortConfig.PortConfigPublishMode.class,
                                                                                      portConfig.get(PUBLISH_MODE),
                                                                                      PortConfig.PortConfigPublishMode.INGRESS,
                                                                                      "getName");
                portConfigBuilder.publishMode(publishMode);
            }
            configs.add(portConfigBuilder.build());
        });
        return configs;
    }

    /**
     * 生成更新配置策略
     *
     * @param updateConfig
     * @return
     */
    public static UpdateConfig generateUpdateConfig(Map<String, String> updateConfig) {
        Long parallelism     = Long.parseLong(updateConfig.get("parallelism"));
        Long delay           = Long.parseLong(updateConfig.get("delay"));
        String failureAction = updateConfig.get("failureAction");
        return UpdateConfig.create(parallelism, delay, failureAction);
    }
}
