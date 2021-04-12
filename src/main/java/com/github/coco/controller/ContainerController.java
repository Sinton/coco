package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ContainerActionEnum;
import com.github.coco.constant.dict.ContainerStatusEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.TimeFetchEnum;
import com.github.coco.utils.DateHelper;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.DockerNetworkHelper;
import com.github.coco.utils.EnumHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/container")
public class ContainerController extends BaseController {
    @Data
    public static class DeployContainer {
        private String name;
        private String image;
        private Boolean autoRemove = false;
        private Boolean publishAllPorts = false;
        private Boolean privileged = false;
        private String command;
        private String entrypoint;
        private String workingDir;
        private String console;
        private String user;
        private String restartPolicy;
        private Map<String, String> ports = new HashMap<>(16);
        private Map<String, String> envs = new HashMap<>(16);
        private Map<String, String> labels = new HashMap<>(16);
        private Map<String, String> volumes = new HashMap<>(16);
        private Map<String, String> networkingConfig = new HashMap<>(16);
        private Map<String, String> resources = new HashMap<>(16);
    }

    /**
     * 部署创建容器
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/create")
    public Map<String, Object> createContainer(@RequestBody Map<String, Object> params) {
        DeployContainer deployContainer = JSON.parseObject(JSON.toJSONString(params), DeployContainer.class);
        String name                          = deployContainer.getName();
        String image                         = deployContainer.getImage();
        boolean autoRemove                   = deployContainer.getAutoRemove();
        boolean publishAllPorts              = deployContainer.getPublishAllPorts();
        boolean privileged                   = deployContainer.getPrivileged();
        String command                       = deployContainer.getCommand();
        String entrypoint                    = deployContainer.getEntrypoint();
        String workingDir                    = deployContainer.getWorkingDir();
        String console                       = deployContainer.getConsole();
        String user                          = deployContainer.getUser();
        String restartPolicy                 = deployContainer.getRestartPolicy();
        Map<String, String> ports            = deployContainer.getPorts();
        Map<String, String> envs             = deployContainer.getEnvs();
        Map<String, String> labels           = deployContainer.getLabels();
        Map<String, String> volumes          = deployContainer.getVolumes();
        Map<String, String> networkingConfig = deployContainer.getNetworkingConfig();
        Map<String, String> resources        = deployContainer.getResources();
        try {
            Set<String> containerExposedPorts = ports.keySet();
            Map<String, List<PortBinding>> portBindings = new HashMap<>(2);
            for (String exposedPort : containerExposedPorts) {
                portBindings.put(exposedPort,
                                 Collections.singletonList(PortBinding.of(getDockerClient().getHost(),
                                                                          ports.get(exposedPort))));
            }
            HostConfig.Builder hostConfigBuilder = HostConfig.builder()
                                                             .privileged(privileged)
                                                             .portBindings(portBindings)
                                                             .publishAllPorts(publishAllPorts)
                                                             .autoRemove(autoRemove);
            if (StringUtils.isNotBlank(restartPolicy)) {
                boolean restartPolicyConflict = autoRemove;
                switch (restartPolicy) {
                    case "always":
                        hostConfigBuilder.restartPolicy(HostConfig.RestartPolicy.always());
                        break;
                    case "unlessStopped":
                        hostConfigBuilder.restartPolicy(HostConfig.RestartPolicy.unlessStopped());
                        break;
                    case "onFailure":
                        hostConfigBuilder.restartPolicy(HostConfig.RestartPolicy.onFailure(0));
                        break;
                    default:
                        restartPolicyConflict = false;
                        break;
                }
                if (restartPolicyConflict) {
                    return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "开启自动删除后，无法对容器设置重启策略");
                }
            }
            if (!volumes.isEmpty()) {
                List<String> volumeBinds = volumes.entrySet()
                                                  .stream()
                                                  .map(volume -> String.format("%s:%s", volume.getKey(), volume.getValue()))
                                                  .collect(Collectors.toList());
                hostConfigBuilder.binds(volumeBinds);
            }
            if (!resources.isEmpty()) {
                double cpuLimits = (getDockerClient().info().cpus() * Math.pow(10, 9) * Double.parseDouble(resources.get("cpuLimits")) / 100);
                hostConfigBuilder.nanoCpus(new Double(cpuLimits).longValue());
                hostConfigBuilder.memoryReservation(Long.parseLong(resources.get("memoryReservations")) * 1024 * 1024);
                hostConfigBuilder.memory(Long.parseLong(resources.get("memoryLimits")) * 1024 * 1024);
            }
            HostConfig hostConfig = hostConfigBuilder.build();

            ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
                                                                            .attachStdin(false)
                                                                            .attachStdout(true)
                                                                            .attachStderr(true)
                                                                            .image(image)
                                                                            .hostConfig(hostConfig);
            if (StringUtils.isNotBlank(command)) {
                containerConfigBuilder.cmd("sh", "-c", command);
            }
            if (StringUtils.isNotBlank(entrypoint)) {
                containerConfigBuilder.entrypoint(entrypoint.split(GlobalConstant.SYMBOL_MARK_COMMA));
            }
            if (StringUtils.isNotBlank(workingDir)) {
                containerConfigBuilder.workingDir(workingDir);
            }
            if (StringUtils.isNotBlank(console)) {
                switch (console) {
                    case "it":
                        containerConfigBuilder.tty(true);
                        containerConfigBuilder.openStdin(true);
                        break;
                    case "i":
                        containerConfigBuilder.tty(false);
                        containerConfigBuilder.openStdin(true);
                        break;
                    case "t":
                        containerConfigBuilder.tty(true);
                        containerConfigBuilder.openStdin(false);
                        break;
                    default:
                        containerConfigBuilder.tty(false);
                        containerConfigBuilder.openStdin(false);
                        break;
                }
            }
            if (StringUtils.isNotBlank(user)) {
                containerConfigBuilder.user(user);
            }
            if (!containerExposedPorts.isEmpty()) {
                containerConfigBuilder.exposedPorts(containerExposedPorts);
            }
            if (!envs.isEmpty()) {
                containerConfigBuilder.env(envs.entrySet()
                                               .stream()
                                               .map(env -> String.format("%s=%s", env.getKey(), env.getValue()))
                                               .collect(Collectors.toList()));
            }
            if (!labels.isEmpty()) {
                containerConfigBuilder.labels(labels);
            }
            if (!volumes.isEmpty()) {
                Set<String> volumeBinds = new HashSet<>(volumes.values());
                containerConfigBuilder.volumes(volumeBinds);
            }
            if (!networkingConfig.isEmpty() && networkingConfig.containsKey(DockerNetworkHelper.NETWORK)) {
                containerConfigBuilder.networkingConfig(DockerNetworkHelper.generateNetworkingConfig(networkingConfig));
            }
            String containerId = getDockerClient().createContainer(containerConfigBuilder.build(), name).id();
            getDockerClient().startContainer(containerId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "部署容器成功");
        } catch (DockerException | InterruptedException e) {
            log.error("部署容器失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    /**
     * 删除容器
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeContainer(@RequestBody Map<String, Object> params) {
        String containerId  = Objects.toString(params.get("containerId"), null);
        boolean force       = Boolean.parseBoolean(params.getOrDefault("force", false).toString());
        boolean withVolumes = Boolean.parseBoolean(params.getOrDefault("withVolumes", false).toString());
        if (containerId != null) {
            try {
                boolean autoRemove = false;
                if (getDockerClient().inspectContainer(containerId).hostConfig() != null) {
                    autoRemove = Boolean.parseBoolean(Objects.toString(getDockerClient().inspectContainer(containerId).hostConfig().autoRemove(),
                                                                       Boolean.FALSE.toString()));
                }
                ContainerStatusEnum status = EnumHelper.getEnumType(ContainerStatusEnum.class,
                                                                    getDockerClient().inspectContainer(containerId).state().status(),
                                                                    ContainerStatusEnum.EXITED,
                                                                    "getStatus");
                switch (status) {
                    case RUNNING:
                    case PAUSED:
                    case RESTARTING:
                        getDockerClient().killContainer(containerId);
                        break;
                    case CREATED:
                    case EXITED:
                        getDockerClient().stopContainer(containerId, DateHelper.SECOND);
                    default:
                        getDockerClient().stopContainer(containerId, DateHelper.SECOND);
                        break;
                }
                if (!autoRemove) {
                    getDockerClient().removeContainer(containerId,
                                                      DockerClient.RemoveContainerParam.forceKill(force),
                                                      DockerClient.RemoveContainerParam.removeVolumes(withVolumes));
                }
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除容器成功");
            } catch (DockerException | InterruptedException e) {
                log.error("删除容器失败", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "containerId不能为空");
        }
    }

    /**
     * 容器重命名
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/rename")
    public Map<String, Object> renameContainer(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String name        = Objects.toString(params.get("name"), null);
        if (containerId != null) {
            try {
                getDockerClient().renameContainer(containerId, name);
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "重命名容器成功");
            } catch (DockerException | InterruptedException e) {
                log.error("重命名容器失败", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "containerId不能为空");
        }
    }

    /**
     * 变更容器生命状态
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String action = params.get("action").toString();
        ContainerActionEnum actionEnum = ContainerActionEnum.valueOf(action.toUpperCase());
        try {
            int waitSeconds;
            switch (actionEnum) {
                case START:
                    getDockerClient().startContainer(containerId);
                    break;
                case RESTART:
                    waitSeconds = Integer.parseInt(params.getOrDefault("waitSeconds", 10).toString());
                    getDockerClient().restartContainer(containerId, waitSeconds);
                    break;
                case STOP:
                    waitSeconds = Integer.parseInt(params.getOrDefault("waitSeconds", 10).toString());
                    getDockerClient().stopContainer(containerId, waitSeconds);
                    break;
                case PAUSE:
                    getDockerClient().pauseContainer(containerId);
                    break;
                case UPPAUSE:
                    getDockerClient().unpauseContainer(containerId);
                    break;
                case KILL:
                    getDockerClient().killContainer(containerId);
                    break;
                default:
                    // TODO 未识别动作异常
                    return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(),
                                                       "未识别对容器的操作动作");
            }
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               String.format("%s成功", action));
        } catch (DockerException | InterruptedException e) {
            log.error("修改容器状态失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    /**
     * 获取容器列表
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageContainers(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(Objects.toString(params.getOrDefault("pageNo", DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(Objects.toString(params.getOrDefault("pageSize", DbConstant.PAGE_SIZE)));
        List<DockerClient.ListContainersParam> filters = new ArrayList<>();
        if (params.get(DockerFilterHelper.FILTER_KEY) != null) {
            String filter = JSON.toJSONString(params.get(DockerFilterHelper.FILTER_KEY));
            filters = DockerFilterHelper.getContainerFilter(filter);
        }
        try {
            List<Container> containers = getDockerClient().listContainers(filters.toArray(new DockerClient.ListContainersParam[]{}));
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, containers));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    /**
     * 获取容器摘要信息
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getContainerInspect(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectContainer(containerId));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    /**
     * 获取容器日志
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/logs")
    public Map<String, Object> getContainerLog(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        boolean follow     = Boolean.parseBoolean(Objects.toString(params.get("follow"), "false"));
        boolean stdErr     = Boolean.parseBoolean(Objects.toString(params.get("stdErr"), "true"));
        boolean stdOut     = Boolean.parseBoolean(Objects.toString(params.get("stdOut"), "true"));
        boolean timestamps = Boolean.parseBoolean(Objects.toString(params.get("timestamps"), "false"));
        int tail           = Integer.parseInt(params.getOrDefault("tail", -1).toString());
        String timeFetch   = Objects.toString(params.get("timeFetch"), "all");
        int since;
        switch (EnumHelper.getEnumType(TimeFetchEnum.class, timeFetch, TimeFetchEnum.ALL, "getValue")) {
            case LAST_DAY:
                since = (int) (System.currentTimeMillis() / 1000 - 60 * 60 * 24);
                break;
            case LAST_HOUR:
                since = (int) System.currentTimeMillis() / 1000 - 60 * 60;
                break;
            case LAST_10_MIN:
                since = (int) System.currentTimeMillis() / 1000 - 60 * 10;
                break;
            case LAST_4_HOURS:
                since = (int) System.currentTimeMillis() / 1000 - 60 * 60 * 4;
                break;
            default:
                since = 0;
                break;
        }
        if (containerId != null) {
            try {
                String logs = getDockerClient().logs(containerId,
                                                     DockerClient.LogsParam.stderr(stdErr),
                                                     DockerClient.LogsParam.stdout(stdOut),
                                                     DockerClient.LogsParam.follow(follow),
                                                     DockerClient.LogsParam.tail(tail),
                                                     DockerClient.LogsParam.since(since),
                                                     DockerClient.LogsParam.timestamps(timestamps))
                                               .readFully();
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, logs);
            } catch (DockerException | InterruptedException e) {
                log.error("获取容器日志失败", e);
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
            }
        } else {
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "containerId不能为空");
        }
    }

    /**
     * 获取容器资源器监控统计
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/stats")
    public Map<String, Object> getContainerStats(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().stats(containerId));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器资源器监控统计失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    /**
     * 获取容器进行信息
     *
     * @param params
     * @return
     */
    @WebLog
    @PostMapping(value = "/top")
    public Map<String, Object> getContainerTop(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String psArgs      = Objects.toString(params.get("psArgs"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().topContainer(containerId, psArgs));
        } catch (DockerException | InterruptedException e) {
            log.error("获取容器进行信息失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
