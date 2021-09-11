package com.github.coco.utils.docker;

import com.github.coco.constant.DockerConstant;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.Config;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@Slf4j
public class DockerStackHelper {
    /**
     * 部署应用栈
     *
     * @param dockerClient
     * @param namespace
     */
    public static void deployStack(DockerClient dockerClient, String namespace) {
        try {
            if (dockerClient.info().swarm() != null && dockerClient.info().swarm().controlAvailable()) {
                pruneServices(dockerClient, namespace);
                dockerClient.createNetwork(null);
                dockerClient.createSecret(null);
                dockerClient.createConfig(null);
                dockerClient.createService(null);
            }
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
        }
    }

    /**
     * 删除应用栈
     *
     * @param dockerClient
     * @param namespace
     */
    public static void removeStack(DockerClient dockerClient, String namespace) {
        removeStack(dockerClient, Collections.singletonList(namespace));
    }

    /**
     * 删除多个应用栈
     *
     * @param dockerClient
     * @param namespaces
     */
    public static void removeStack(DockerClient dockerClient, List<String> namespaces) {
        if (namespaces != null && !namespaces.isEmpty()) {
            try {
                Double apiVersion = Double.parseDouble(dockerClient.version().apiVersion());
                namespaces.forEach(namespace -> {
                    removeStackServices(dockerClient, namespace);
                    // 删除时进行版本比较缘由参考docker/cli
                    if (apiVersion.compareTo(1.25D) >= 0) {
                        removeStackSecrets(dockerClient, namespace);
                    }
                    if (apiVersion.compareTo(1.30D) >= 0) {
                        removeStackConfigs(dockerClient, namespace);
                    }
                    removeStackNetworks(dockerClient, namespace);
                });
            } catch (DockerException | InterruptedException e) {
                log.error("", e);
            }
        }
    }

    /**
     * 删除应用栈的Service
     *
     * @param dockerClient
     * @param namespace
     */
    private static void removeStackServices(DockerClient dockerClient, String namespace) {
        getStackServices(dockerClient, namespace).stream()
                                                 .map(Service::id)
                                                 .distinct()
                                                 .forEach(serviceId -> {
                                                     try {
                                                         dockerClient.removeService(serviceId);
                                                     } catch (DockerException | InterruptedException e) {
                                                         log.error("", e);
                                                     }
                                                 });
    }

    /**
     * 删除应用栈的Secret
     *
     * @param dockerClient
     * @param namespace
     */
    private static void removeStackSecrets(DockerClient dockerClient, String namespace) {
        getStackSecrets(dockerClient, namespace).stream()
                                                .map(Secret::id)
                                                .distinct()
                                                .forEach(secretId -> {
                                                    try {
                                                        dockerClient.deleteSecret(secretId);
                                                    } catch (DockerException | InterruptedException e) {
                                                        log.error("", e);
                                                    }
                                                });
    }

    /**
     * 删除应用栈的Config
     *
     * @param dockerClient
     * @param namespace
     */
    private static void removeStackConfigs(DockerClient dockerClient, String namespace) {
        getStackConfigs(dockerClient, namespace).stream()
                                                .map(Config::id)
                                                .distinct()
                                                .forEach(configId -> {
                                                    try {
                                                        dockerClient.deleteConfig(configId);
                                                    } catch (DockerException | InterruptedException e) {
                                                        log.error("", e);
                                                    }
                                                });
    }

    /**
     * 删除应用栈的Network
     *
     * @param dockerClient
     * @param namespace
     */
    private static void removeStackNetworks(DockerClient dockerClient, String namespace) {
        getStackNetworks(dockerClient, namespace).stream()
                                                 .map(Network::id)
                                                 .distinct()
                                                 .forEach(networkId -> {
                                                     try {
                                                         dockerClient.removeNetwork(networkId);
                                                     } catch (DockerException | InterruptedException e) {
                                                         log.error("", e);
                                                     }
                                                 });
    }

    /**
     * 应用栈列表
     *
     * @param dockerClient
     * @return
     */
    public static List<String> listStacks(DockerClient dockerClient) {
        try {
            return dockerClient.listServices()
                               .stream()
                               .map(service -> {
                                   if (service.spec().labels() != null && !service.spec().labels().isEmpty()) {
                                       return service.spec()
                                                     .labels()
                                                     .getOrDefault(DockerConstant.SWARM_STACK_LABEL, null);
                                   } else {
                                       return null;
                                   }
                               })
                               .distinct()
                               .filter(StringUtils::isNotBlank)
                               .collect(Collectors.toList());
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取应用栈的Service
     *
     * @param dockerClient
     * @param namespace
     * @return
     */
    public static List<Service> getStackServices(DockerClient dockerClient, String namespace) {
        try {
            return dockerClient.listServices()
                               .stream()
                               .filter(service -> {
                                   if (service.spec().labels() != null && !service.spec().labels().isEmpty()) {
                                       if (service.spec().labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                           return service.spec()
                                                         .labels()
                                                         .get(DockerConstant.SWARM_STACK_LABEL)
                                                         .equals(namespace);
                                       } else {
                                           return false;
                                       }
                                   } else {
                                       return false;
                                   }
                               }).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取应用栈的Secret
     *
     * @param dockerClient
     * @param namespace
     * @return
     */
    public static List<Secret> getStackSecrets(DockerClient dockerClient, String namespace) {
        try {
            return dockerClient.listSecrets()
                               .stream()
                               .filter(secret -> {
                                   if (secret.secretSpec().labels() != null && !secret.secretSpec().labels().isEmpty()) {
                                       if (secret.secretSpec().labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                           return secret.secretSpec()
                                                        .labels()
                                                        .get(DockerConstant.SWARM_STACK_LABEL)
                                                        .equals(namespace);
                                       } else {
                                           return false;
                                       }
                                   } else {
                                       return false;
                                   }
                               }).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取应用栈的Config
     *
     * @param dockerClient
     * @param namespace
     * @return
     */
    public static List<Config> getStackConfigs(DockerClient dockerClient, String namespace) {
        try {
            return dockerClient.listConfigs()
                               .stream()
                               .filter(config -> {
                                   if (config.configSpec().labels() != null && !config.configSpec().labels().isEmpty()) {
                                       if (config.configSpec().labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                           return config.configSpec()
                                                        .labels()
                                                        .get(DockerConstant.SWARM_STACK_LABEL)
                                                        .equals(namespace);
                                       } else {
                                           return false;
                                       }
                                   } else {
                                       return false;
                                   }
                               }).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取应用栈的Network
     *
     * @param dockerClient
     * @param namespace
     * @return
     */
    public static List<Network> getStackNetworks(DockerClient dockerClient, String namespace) {
        try {
            return dockerClient.listNetworks()
                               .stream()
                               .filter(network -> {
                                   if (network.labels() != null && !network.labels().isEmpty()) {
                                       if (network.labels().containsKey(DockerConstant.SWARM_STACK_LABEL)) {
                                           return network.labels()
                                                         .get(DockerConstant.SWARM_STACK_LABEL)
                                                         .equals(namespace);
                                       } else {
                                           return false;
                                       }
                                   } else {
                                       return false;
                                   }
                               }).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除已存在的应用栈服务
     *
     * @param dockerClient
     * @param namespace
     */
    public static void pruneServices(DockerClient dockerClient, String namespace) {
        try {
            List<String> serviceIds = dockerClient.listServices()
                                                  .stream()
                                                  .filter(service -> service.spec()
                                                                            .name()
                                                                            .startsWith(String.format("%s_", namespace)))
                                                  .map(Service::id)
                                                  .distinct()
                                                  .collect(Collectors.toList());
            for (String serviceId : serviceIds) {
                dockerClient.removeService(serviceId);
            }
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
        }
    }

    public static List<?> getServicesDeclaredNetworks(DockerClient dockerClient) {
        try {
            dockerClient.listServices().forEach(service -> {
                service.spec().networks().forEach(networkAttachmentConfig -> {
                });
            });
        } catch (DockerException | InterruptedException e) {
            log.error("", e);
        }
        return null;
    }
}
