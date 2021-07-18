package com.github.coco.utils;

import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.ConfigBind;
import com.spotify.docker.client.messages.swarm.SecretBind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class DockerContainerHelper {
    /**
     * 配置项ID
     */
    private static final String CONFIG_ID   = "configId";

    /**
     * 配置项名称
     */
    private static final String CONFIG_NAME = "configName";

    /**
     * 秘密配置项ID
     */
    private static final String SECRET_ID   = "secretId";

    /**
     * 秘密配置项名称
     */
    private static final String SECRET_NAME = "secretName";

    /**
     * 生成配置项绑定信息
     *
     * @param configs
     * @return
     */
    public static List<ConfigBind> generateConfig(List<Map<String, String>> configs) {
        List<ConfigBind> configBinds = new ArrayList<>();
        configs.forEach(config -> {
            ConfigBind.Builder configBindBuilder = ConfigBind.builder();
            if (config.containsKey(CONFIG_ID)) {
                configBindBuilder.configId(config.get(CONFIG_ID));
            }
            if (config.containsKey(CONFIG_NAME)) {
                configBindBuilder.configName(config.get(CONFIG_NAME));
            }
            configBinds.add(configBindBuilder.build());
        });
        return configBinds;
    }

    /**
     * 生成秘密配置项绑定信息
     *
     * @param mounts
     * @return
     */
    public static List<Mount> generateMount(List<Map<String, String>> mounts) {
        List<Mount> mountList = new ArrayList<>();
        mounts.forEach(mount -> {
            Mount.Builder mountBuilder = Mount.builder();
            mountList.add(mountBuilder.build());
        });
        return mountList;
    }

    public static List<SecretBind> generateSecret(List<Map<String, String>> secrets) {
        List<SecretBind> secretBinds = new ArrayList<>();
        secrets.forEach(secret -> {
            SecretBind.Builder secretBindBuilder = SecretBind.builder();
            if (secret.containsKey(SECRET_ID)) {
                secretBindBuilder.secretId(secret.get(SECRET_ID));
            }
            if (secret.containsKey(SECRET_NAME)) {
                secretBindBuilder.secretName(secret.get(SECRET_NAME));
            }
            secretBinds.add(secretBindBuilder.build());
        });
        return secretBinds;
    }
}
