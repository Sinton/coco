package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/service")
public class ServiceController extends BaseController {

    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createService(@RequestBody Map<String, Object> params) {
        String serviceName = params.getOrDefault("serviceName", null).toString();
        try {
            ServiceSpec serviceSpec = ServiceSpec.builder().name(serviceName).build();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.createService(serviceSpec));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "创建服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeService(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            dockerClient.removeService(serviceId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "删除服务失败");
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
            ServiceSpec oldServiceSpec = dockerClient.inspectService(serviceId).spec();
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
            ServiceSpec serviceSpec = ServiceSpec.builder()
                                                 .name(oldServiceSpec.name())
                                                 .mode(serviceMode)
                                                 .labels(oldServiceSpec.labels())
                                                 .taskTemplate(oldServiceSpec.taskTemplate())
                                                 .endpointSpec(oldServiceSpec.endpointSpec())
                                                 .networks(oldServiceSpec.networks())
                                                 .updateConfig(oldServiceSpec.updateConfig())
                                                 .build();
            dockerClient.updateService(serviceId, dockerClient.inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新编排任务调度成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新编排任务调度失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/resources")
    public Map<String, Object> updateResources(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            ServiceSpec oldServiceSpec = dockerClient.inspectService(serviceId).spec();
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/mount")
    public Map<String, Object> updateMount(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            ServiceSpec oldServiceSpec = dockerClient.inspectService(serviceId).spec();
            ContainerSpec oldContainerSpec = oldServiceSpec.taskTemplate().containerSpec();
            ContainerSpec containerSpec = ContainerSpec.builder()
                                                       .image(oldContainerSpec.image())
                                                       .args(oldContainerSpec.args())
                                                       .command(oldContainerSpec.command())
                                                       .env(oldContainerSpec.env())
                                                       .groups(oldContainerSpec.groups())
                                                       .tty(oldContainerSpec.tty())
                                                       .build();
            TaskSpec taskTemplate = TaskSpec.builder()
                                            .containerSpec(containerSpec)
                                            .resources(oldServiceSpec.taskTemplate().resources())
                                            .restartPolicy(oldServiceSpec.taskTemplate().restartPolicy())
                                            .networks(oldServiceSpec.taskTemplate().networks())
                                            .placement(oldServiceSpec.taskTemplate().placement())
                                            .logDriver(oldServiceSpec.taskTemplate().logDriver())
                                            .build();
            ServiceSpec serviceSpec = ServiceSpec.builder()
                                                 .name(oldServiceSpec.name())
                                                 .mode(oldServiceSpec.mode())
                                                 .labels(oldServiceSpec.labels())
                                                 .taskTemplate(taskTemplate)
                                                 .endpointSpec(oldServiceSpec.endpointSpec())
                                                 .networks(oldServiceSpec.networks())
                                                 .updateConfig(oldServiceSpec.updateConfig())
                                                 .build();
            dockerClient.updateService(serviceId, dockerClient.inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新服务失败");
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
            ServiceSpec oldServiceSpec = dockerClient.inspectService(serviceId).spec();
            NetworkAttachmentConfig networkConfig = NetworkAttachmentConfig.builder()
                                                                           .aliases(aliases.split(","))
                                                                           .target(target)
                                                                           .build();
            ServiceSpec serviceSpec = ServiceSpec.builder()
                                                 .name(oldServiceSpec.name())
                                                 .mode(oldServiceSpec.mode())
                                                 .labels(oldServiceSpec.labels())
                                                 .taskTemplate(oldServiceSpec.taskTemplate())
                                                 .endpointSpec(oldServiceSpec.endpointSpec())
                                                 .networks(networkConfig)
                                                 .updateConfig(oldServiceSpec.updateConfig())
                                                 .build();
            dockerClient.updateService(serviceId, dockerClient.inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/secret")
    public Map<String, Object> updateSecret(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            ServiceSpec serviceSpec = ServiceSpec.builder().build();
            dockerClient.updateService(serviceId, dockerClient.inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/config")
    public Map<String, Object> updateConfig(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            ServiceSpec serviceSpec = ServiceSpec.builder().build();
            dockerClient.updateService(serviceId, dockerClient.inspectService(serviceId).version().index(), serviceSpec);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "更新服务成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "更新服务失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageServices(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Service> services = dockerClient.listServices()
                                                 .stream()
                                                 .sorted((x, y) -> y.createdAt().compareTo(x.createdAt()))
                                                 .collect(Collectors.toList());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, services));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取服务列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getService(@RequestBody Map<String, Object> params) {
        String serviceId = params.getOrDefault("serviceId", null).toString();
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.inspectService(serviceId));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取服务信息失败");
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
                logs = dockerClient.serviceLogs(serviceId,
                                                DockerClient.LogsParam.stderr(stdErr),
                                                DockerClient.LogsParam.stdout(stdOut),
                                                DockerClient.LogsParam.timestamps(timestamps),
                                                DockerClient.LogsParam.tail(tail))
                                   .readFully();
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, logs);
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "获取服务日志失败");
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "serviceId不能为空");
        }
    }
}
