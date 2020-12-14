package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ContainerActionEnum;
import com.github.coco.constant.dict.ContainerStatusEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.TimeFetchEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.EnumHelper;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import lombok.Data;
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
        private Map<String, String> ports = new HashMap<>(16);
        private Map<String, String> envs = new HashMap<>(16);
        private Map<String, String> labels = new HashMap<>(16);
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
        String name                     = deployContainer.getName();
        String image                    = deployContainer.getImage();
        boolean autoRemove              = deployContainer.getAutoRemove();
        boolean publishAllPorts         = deployContainer.getPublishAllPorts();
        boolean privileged              = deployContainer.getPrivileged();
        Map<String, String> ports       = deployContainer.getPorts();
        Map<String, String> envs        = deployContainer.getEnvs();
        Map<String, String> labels      = deployContainer.getLabels();
        String command                  = deployContainer.getCommand();
        String entrypoint               = deployContainer.getEntrypoint();
        String workingDir               = deployContainer.getWorkingDir();
        String console                  = deployContainer.getConsole();
        String user                     = deployContainer.getUser();
        try {
            Set<String> containerExposedPorts = ports.keySet();
            Map<String, List<PortBinding>> portBindings = new HashMap<>(4);
            for (String exposedPort : containerExposedPorts) {
                portBindings.put(exposedPort,
                                 Collections.singletonList(PortBinding.of(getDockerClient().getHost(),
                                                                          ports.get(exposedPort))));
            }
            HostConfig hostConfig = HostConfig.builder()
                                              .privileged(privileged)
                                              .portBindings(portBindings)
                                              .publishAllPorts(publishAllPorts)
                                              .autoRemove(autoRemove)
                                              .build();

            ContainerConfig.Builder builder = ContainerConfig.builder()
                                                             .attachStdin(false)
                                                             .attachStdout(true)
                                                             .attachStderr(true)
                                                             .image(image)
                                                             .hostConfig(hostConfig);
            if (StringUtils.isNotBlank(command)) {
                builder.cmd(command.split(","));
            }
            if (StringUtils.isNotBlank(entrypoint)) {
                builder.entrypoint(entrypoint.split(","));
            }
            if (StringUtils.isNotBlank(workingDir)) {
                builder.workingDir(workingDir);
            }
            if (StringUtils.isNotBlank(console)) {
                if (console.startsWith("t")) {
                    builder.tty(true);
                }
            }
            if (StringUtils.isNotBlank(user)) {
                builder.user(user);
            }
            if (!containerExposedPorts.isEmpty()) {
                builder.exposedPorts(containerExposedPorts);
            }
            if (!envs.isEmpty()) {
                builder.env(envs.entrySet()
                                .stream()
                                .map(env -> String.format("%s=%s", env.getKey(), env.getValue()))
                                .collect(Collectors.toList()));
            }
            if (!labels.isEmpty()) {
                builder.labels(labels);
            }
            String containerId = getDockerClient().createContainer(builder.build(), name).id();
            getDockerClient().startContainer(containerId);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "部署容器成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "部署容器失败");
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
                boolean autoRemove = getDockerClient().inspectContainer(containerId).hostConfig().autoRemove();
                if (!Objects.equals(getDockerClient().inspectContainer(containerId).state().status(),
                                    ContainerStatusEnum.EXITED.getStatus())) {
                    getDockerClient().killContainer(containerId);
                }
                if (!autoRemove) {
                    getDockerClient().removeContainer(containerId,
                                                      DockerClient.RemoveContainerParam.forceKill(force),
                                                      DockerClient.RemoveContainerParam.removeVolumes(withVolumes));
                }
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除容器成功");
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "删除容器失败");
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
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "重命名容器失败");
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
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "修改容器状态失败");
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
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        List<DockerClient.ListContainersParam> filters = new ArrayList<>();
        if (params.get(DockerFilterHelper.FILTER_KEY) != null) {
            String filter = JSON.toJSONString(params.get(DockerFilterHelper.FILTER_KEY));
            filters = DockerFilterHelper.getContainerFilter(filter);
        }
        try {
            List<Container> containers = getDockerClient().listContainers(DockerFilterHelper.toArray(filters,
                                                                                                     DockerClient.ListContainersParam.class));
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, containers));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器列表失败");
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
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器信息失败");
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
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "获取容器日志失败");
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
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器资源器监控统计失败");
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
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器进行信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
