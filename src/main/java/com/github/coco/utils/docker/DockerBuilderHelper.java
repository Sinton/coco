package com.github.coco.utils.docker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.*;

import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class DockerBuilderHelper {
    public enum ServiceSpecEnum {
        NONE,
        MODE,
        LABELS,
        TASK_TEMPLATE,
        ENDPOINT,
        NETWORKS,
        UPDATE_CONFIG;
    }

    public enum TaskSpecEnum {
        NONE,
        CONTAINER_SPEC,
        RESOURCES,
        RESTART_POLICY,
        NETWORKS,
        PLACEMENT,
        LOG_DRIVER
    }

    public enum ContainerSpecEnum {
        NONE,
        CONFIG,
        SECRETS,
        MOUNTS,
        IMAGE
    }

    public static ServiceSpec serviceSpecBuilder(ServiceSpec serviceSpec) {
        return serviceSpecBuilder(serviceSpec, ServiceSpecEnum.NONE, null);
    }

    public static <T> ServiceSpec serviceSpecBuilder(ServiceSpec serviceSpec,
                                                     ServiceSpecEnum serviceSpecEnum,
                                                     T value) {
        ServiceSpec.Builder builder = ServiceSpec.builder()
                                                 .mode(serviceSpec.mode())
                                                 .labels(serviceSpec.labels())
                                                 .taskTemplate(serviceSpec.taskTemplate())
                                                 .endpointSpec(serviceSpec.endpointSpec())
                                                 .networks(serviceSpec.networks())
                                                 .updateConfig(serviceSpec.updateConfig());
        switch (serviceSpecEnum) {
            case MODE:
                if (clazzAssert(value, String.class)) {
                    builder.mode(JSON.parseObject((String) value, ServiceMode.class));
                } else {
                    builder.mode((ServiceMode) value);
                }
                return builder.build();
            case LABELS:
                if (clazzAssert(value, String.class)) {
                    builder.labels(JSON.parseObject((String) value, new TypeReference<Map<String, String>>() {}));
                } else {
                    builder.labels((Map<String, String>) value);
                }
                return builder.build();
            case TASK_TEMPLATE:
                if (clazzAssert(value, String.class)) {
                    builder.taskTemplate(JSON.parseObject((String) value, TaskSpec.class));
                } else {
                    builder.taskTemplate((TaskSpec) value);
                }
                return builder.build();
            case ENDPOINT:
                if (clazzAssert(value, String.class)) {
                    builder.endpointSpec(JSON.parseObject((String) value, EndpointSpec.class));
                } else {
                    builder.endpointSpec((EndpointSpec) value);
                }
                return builder.build();
            case NETWORKS:
                if (clazzAssert(value, String.class)) {
                    builder.networks(JSON.parseObject((String) value,
                                                      new TypeReference<List<NetworkAttachmentConfig>>() {}));
                } else {
                    builder.networks((List<NetworkAttachmentConfig>) value);
                }
                return builder.build();
            case UPDATE_CONFIG:
                if (clazzAssert(value, String.class)) {
                    builder.updateConfig(JSON.parseObject((String) value, UpdateConfig.class));
                } else {
                    builder.updateConfig((UpdateConfig) value);
                }
                return builder.build();
            default:
                return builder.build();
        }
    }

    public static TaskSpec taskSpecBuilder(TaskSpec taskSpec) {
        return taskSpecBuilder(taskSpec, TaskSpecEnum.NONE, null);
    }

    public static <T> TaskSpec taskSpecBuilder(TaskSpec taskSpec, TaskSpecEnum taskSpecEnum, T value) {
        TaskSpec.Builder builder = TaskSpec.builder()
                                           .containerSpec(taskSpec.containerSpec())
                                           .resources(taskSpec.resources())
                                           .restartPolicy(taskSpec.restartPolicy())
                                           .networks(taskSpec.networks())
                                           .placement(taskSpec.placement())
                                           .logDriver(taskSpec.logDriver());
        switch (taskSpecEnum) {
            case CONTAINER_SPEC:
                if (value.getClass().equals(String.class)) {
                    builder.containerSpec(JSON.parseObject((String) value, ContainerSpec.class));
                } else {
                    builder.containerSpec((ContainerSpec) value);
                }
                return builder.build();
            case RESOURCES:
                if (value.getClass().equals(String.class)) {
                    builder.resources(JSON.parseObject((String) value, ResourceRequirements.class));
                } else {
                    builder.resources((ResourceRequirements) value);
                }
                return builder.build();
            case RESTART_POLICY:
                if (value.getClass().equals(String.class)) {
                    builder.restartPolicy(JSON.parseObject((String) value, RestartPolicy.class));
                } else {
                    builder.restartPolicy((RestartPolicy) value);
                }
                return builder.build();
            case NETWORKS:
                if (value.getClass().equals(String.class)) {
                    builder.networks(JSON.parseObject((String) value,
                                                      new TypeReference<List<NetworkAttachmentConfig>>() {}));
                } else {
                    builder.networks((List<NetworkAttachmentConfig>) value);
                }
                return builder.build();
            case PLACEMENT:
                if (value.getClass().equals(String.class)) {
                    builder.placement(JSON.parseObject((String) value, Placement.class));
                } else {
                    builder.placement((Placement) value);
                }
                return builder.build();
            case LOG_DRIVER:
                if (value.getClass().equals(String.class)) {
                    builder.logDriver(JSON.parseObject((String) value, Driver.class));
                } else {
                    builder.logDriver((Driver) value);
                }
                return builder.build();
            default:
                return builder.build();
        }
    }

    public static ContainerSpec containerSpecBuilder(ContainerSpec containerSpec) {
        return containerSpecBuilder(containerSpec, ContainerSpecEnum.NONE, null);
    }

    public static <T> ContainerSpec containerSpecBuilder(ContainerSpec containerSpec,
                                                         ContainerSpecEnum containerSpecEnum,
                                                         T value) {
        ContainerSpec.Builder builder = ContainerSpec.builder()
                                                     .command(containerSpec.command())
                                                     .args(containerSpec.args())
                                                     .env(containerSpec.env())
                                                     .dir(containerSpec.dir())
                                                     .user(containerSpec.user())
                                                     .groups(containerSpec.groups())
                                                     .tty(containerSpec.tty())
                                                     .mounts(containerSpec.mounts())
                                                     .image(containerSpec.image())
                                                     .configs(containerSpec.configs())
                                                     .secrets(containerSpec.secrets());
        switch (containerSpecEnum) {
            case MOUNTS:
                if (clazzAssert(value, String.class)) {
                    builder.mounts(JSON.parseObject((String) value, Mount.class));
                } else if (clazzAssert(value, List.class)) {
                    builder.mounts((List<Mount>) value);
                } else {
                    builder.mounts((Mount[]) value);
                }
                return builder.build();
            case IMAGE:
                if (clazzAssert(value, String.class)) {
                    builder.image((String) value);
                }
                return builder.build();
            case CONFIG:
                if (clazzAssert(value, String.class)) {
                    builder.configs(JSON.parseObject((String) value, new TypeReference<List<ConfigBind>>() {}));
                } else {
                    builder.configs((List<ConfigBind>) value);
                }
                return builder.build();
            case SECRETS:
                if (clazzAssert(value, String.class)) {
                    builder.secrets(JSON.parseObject((String) value, new TypeReference<List<SecretBind>>() {}));
                } else {
                    builder.secrets((List<SecretBind>) value);
                }
                return builder.build();
            default:
                return builder.build();
        }
    }

    public static <V, T> boolean clazzAssert(V value, Class<T> clazz) {
        return value != null && value.getClass().equals(clazz);
    }
}
