package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.SwarmSchedulingModeEnum;
import com.github.coco.dto.CreateServiceDTO;
import com.github.coco.utils.*;
import com.github.coco.utils.docker.DockerBuilderHelper;
import com.github.coco.utils.docker.DockerContainerHelper;
import com.github.coco.utils.docker.DockerNetworkHelper;
import com.github.coco.utils.docker.DockerServiceHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.coco.utils.docker.DockerBuilderHelper.*;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/service")
public class ServiceController extends BaseController {
    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createService(@RequestBody Map<String, Object> params) {
        CreateServiceDTO createServiceDTO = MapHelper.mapConvertObject(params, CreateServiceDTO.class);
        if (StringUtils.isNotBlank(createServiceDTO.getName()) || StringUtils.isNotBlank(createServiceDTO.getImage())) {
            try {
                ServiceSpec.Builder serviceSpecBuilder = ServiceSpec.builder();
                // 指定服务名称
                serviceSpecBuilder.name(createServiceDTO.getName());
                // 指定编排调度方式
                switch (EnumHelper.getEnumType(SwarmSchedulingModeEnum.class, createServiceDTO.getSchedulingMode().toString())) {
                    case GLOBAL:
                        serviceSpecBuilder.mode(ServiceMode.withGlobal());
                        break;
                    case REPLICATED:
                        serviceSpecBuilder.mode(ServiceMode.withReplicas(createServiceDTO.getReplicas()));
                        break;
                    default:
                        break;
                }
                // 指定服务标签
                if (createServiceDTO.getServiceLabels() != null && !createServiceDTO.getServiceLabels().isEmpty()) {
                    serviceSpecBuilder.labels(createServiceDTO.getServiceLabels());
                }
                // 指定服务端口
                if (createServiceDTO.getPorts() != null && !createServiceDTO.getPorts().isEmpty()) {
                    EndpointSpec.Builder endpointSpecBuilder = EndpointSpec.builder();
                    endpointSpecBuilder.ports(DockerServiceHelper.generatePortConfig(createServiceDTO.getPorts()));
                    serviceSpecBuilder.endpointSpec(endpointSpecBuilder.build());
                }
                // 指定服务网络
                if (createServiceDTO.getNetworkingConfigs() != null && !createServiceDTO.getNetworkingConfigs().isEmpty()) {
                    serviceSpecBuilder.networks(DockerNetworkHelper.generateNetworkAttachmentConfig(createServiceDTO.getNetworkingConfigs()));
                }
                // 指定服务更新配置策略
                if (createServiceDTO.getUpdateConfigPolicy() != null && !createServiceDTO.getUpdateConfigPolicy().isEmpty()) {
                    serviceSpecBuilder.updateConfig(DockerServiceHelper.generateUpdateConfig(createServiceDTO.getUpdateConfigPolicy()));
                }

                ContainerSpec.Builder containerSpecBuilder = ContainerSpec.builder();
                // 指定容器镜像
                containerSpecBuilder.image(createServiceDTO.getImage());
                // 指定容器执行命令
                if (StringUtils.isNotBlank(createServiceDTO.getCommand())) {
                    containerSpecBuilder.command(createServiceDTO.getCommand());
                }
                // 指定容器交互方式
                if (createServiceDTO.getTty() != null) {
                    containerSpecBuilder.tty(createServiceDTO.getTty());
                }
                // 指定容器用户
                if (createServiceDTO.getUser() != null && StringUtils.isNotBlank(createServiceDTO.getUser())) {
                    containerSpecBuilder.user(createServiceDTO.getUser());
                }
                // 指定容器工作空间
                if (StringUtils.isNotBlank(createServiceDTO.getWorkingDir())) {
                    containerSpecBuilder.dir(createServiceDTO.getWorkingDir());
                }
                // 指定容器标签
                if (createServiceDTO.getContainerLabels() != null && !createServiceDTO.getContainerLabels().isEmpty()) {
                    containerSpecBuilder.labels(createServiceDTO.getContainerLabels());
                }
                // 指定容器存储卷挂载
                if (createServiceDTO.getVolumes() != null && !createServiceDTO.getVolumes().isEmpty()) {
                    containerSpecBuilder.mounts(DockerContainerHelper.generateMount(createServiceDTO.getVolumes()));
                }
                // 指定容器配置项
                if (createServiceDTO.getConfigs() != null && !createServiceDTO.getConfigs().isEmpty()) {
                    containerSpecBuilder.configs(DockerContainerHelper.generateConfig(createServiceDTO.getConfigs()));
                }
                // 指定容器秘密配置项
                if (createServiceDTO.getSecrets() != null && !createServiceDTO.getSecrets().isEmpty()) {
                    containerSpecBuilder.secrets(DockerContainerHelper.generateSecret(createServiceDTO.getSecrets()));
                }

                TaskSpec.Builder taskSpecBuilder = TaskSpec.builder();
                RestartPolicy restartPolicy = createServiceDTO.getRestartPolicy();
                taskSpecBuilder.containerSpec(containerSpecBuilder.build());
                taskSpecBuilder.restartPolicy(restartPolicy);
                // 指定服务编排任务
                serviceSpecBuilder.taskTemplate(taskSpecBuilder.build());
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                                   getDockerClient().createService(serviceSpecBuilder.build()));
            } catch (Exception e) {
                log.error("创建服务失败", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "服务名不能为空");
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeService(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            getDockerClient().removeService(serviceId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除服务成功");
        } catch (Exception e) {
            log.error("删除服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateService(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            Service service = getDockerClient().inspectService(serviceId);
            getDockerClient().updateService(serviceId, service.version().index(), service.spec());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/scale")
    public Map<String, Object> scale(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        Long replicas = Long.parseLong(params.getOrDefault("replicas", 1).toString());
        String mode = params.getOrDefault("mode", "replicated").toString();
        try {
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec oldServiceSpec = service.spec();
            ServiceMode serviceMode = null;
            switch (mode) {
                case "replicated":
                    ReplicatedService replicated = ReplicatedService.builder().replicas(replicas).build();
                    serviceMode = ServiceMode.builder().replicated(replicated).build();
                    break;
                case "global":
                    GlobalService global = GlobalService.builder().build();
                    serviceMode = ServiceMode.builder().global(global).build();
                    break;
                default:
                    break;
            }
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec, ServiceSpecEnum.MODE, serviceMode);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新编排任务调度成功");
        } catch (Exception e) {
            log.error("更新编排任务调度失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/resources")
    public Map<String, Object> updateResources(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            ResourceRequirements resourceRequirements = ResourceRequirements.builder().build();
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec oldServiceSpec = service.spec();
            TaskSpec taskSpec = DockerBuilderHelper.taskSpecBuilder(service.spec().taskTemplate(),
                                                                    TaskSpecEnum.RESOURCES,
                                                                    resourceRequirements);
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec,
                                                                             ServiceSpecEnum.TASK_TEMPLATE,
                                                                             taskSpec);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/label")
    public Map<String, Object> updateLabel(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        Map<String, String> labels = JsonHelper.jsonStringConvertMap(params.getOrDefault("serviceLabels", "").toString());
        try {
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(service.spec(),
                                                                             ServiceSpecEnum.LABELS,
                                                                             labels);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新标签失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/mount")
    public Map<String, Object> updateMount(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec oldServiceSpec = service.spec();
            ContainerSpec containerSpec = DockerBuilderHelper.containerSpecBuilder(oldServiceSpec.taskTemplate().containerSpec(),
                                                                                   ContainerSpecEnum.MOUNTS,
                                                                                   null);
            TaskSpec taskSpec = DockerBuilderHelper.taskSpecBuilder(oldServiceSpec.taskTemplate(),
                                                                    TaskSpecEnum.CONTAINER_SPEC,
                                                                    containerSpec);
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec,
                                                                             ServiceSpecEnum.TASK_TEMPLATE,
                                                                             taskSpec);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/network")
    public Map<String, Object> updateNetwork(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        String target = params.getOrDefault("target", null).toString();
        String aliases = params.getOrDefault("aliases", null).toString();
        try {
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec oldServiceSpec = service.spec();
            NetworkAttachmentConfig networkConfig = NetworkAttachmentConfig.builder()
                                                                           .aliases(aliases.split(","))
                                                                           .target(target)
                                                                           .build();
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec,
                                                                             ServiceSpecEnum.NETWORKS,
                                                                             networkConfig);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/config")
    public Map<String, Object> updateConfig(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        List<ConfigBind> configsBind = JSON.parseObject(Objects.toString(params.get("configsBind"), "[]"),
                                                        new TypeReference<List<ConfigBind>>() {});
        try {
            ServiceSpec oldServiceSpec = getDockerClient().inspectService(serviceId).spec();
            ContainerSpec containerSpec = DockerBuilderHelper.containerSpecBuilder(oldServiceSpec.taskTemplate().containerSpec(),
                                                                                   ContainerSpecEnum.CONFIG,
                                                                                   configsBind);
            TaskSpec taskTemplate = DockerBuilderHelper.taskSpecBuilder(oldServiceSpec.taskTemplate(),
                                                                        TaskSpecEnum.CONTAINER_SPEC,
                                                                        containerSpec);
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec,
                                                                             ServiceSpecEnum.TASK_TEMPLATE,
                                                                             taskTemplate);
            getDockerClient().updateService(serviceId, getDockerClient().inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/secret")
    public Map<String, Object> updateSecret(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        List<SecretBind> secretsBind = JSON.parseObject(Objects.toString(params.get("secretsBind"), "[]"),
                                                        new TypeReference<List<SecretBind>>() {});
        try {
            Service service = getDockerClient().inspectService(serviceId);
            ServiceSpec oldServiceSpec = service.spec();
            ContainerSpec containerSpec = DockerBuilderHelper.containerSpecBuilder(oldServiceSpec.taskTemplate().containerSpec(),
                                                                                   ContainerSpecEnum.SECRETS,
                                                                                   secretsBind);
            TaskSpec taskSpec = DockerBuilderHelper.taskSpecBuilder(oldServiceSpec.taskTemplate(),
                                                                    TaskSpecEnum.CONTAINER_SPEC,
                                                                    containerSpec);
            ServiceSpec serviceSpec = DockerBuilderHelper.serviceSpecBuilder(oldServiceSpec,
                                                                             ServiceSpecEnum.TASK_TEMPLATE,
                                                                             taskSpec);
            getDockerClient().updateService(serviceId, service.version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            log.error("更新服务失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageServices(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Service> services = getDockerClient().listServices()
                                                      .stream()
                                                      .sorted((x, y) -> y.createdAt().compareTo(x.createdAt()))
                                                      .collect(Collectors.toList());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, services));
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getService(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectService(serviceId));
        } catch (Exception e) {
            log.error("获取服务信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/logs")
    public Map<String, Object> getServiceLog(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        boolean stdErr = Boolean.parseBoolean(params.getOrDefault("stdErr", true).toString());
        boolean stdOut = Boolean.parseBoolean(params.getOrDefault("stdOut", true).toString());
        boolean timestamps = Boolean.parseBoolean(params.getOrDefault("timestamps", false).toString());
        int tail = Integer.parseInt(params.getOrDefault("tail", -1).toString());
        String logs;
        if (serviceId != null) {
            try {
                logs = getDockerClient().serviceLogs(serviceId,
                                                     DockerClient.LogsParam.stderr(stdErr),
                                                     DockerClient.LogsParam.stdout(stdOut),
                                                     DockerClient.LogsParam.timestamps(timestamps),
                                                     DockerClient.LogsParam.tail(tail))
                                   .readFully();
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, logs);
            } catch (Exception e) {
                log.error("获取服务日志失败", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "serviceId不能为空");
        }
    }
}
