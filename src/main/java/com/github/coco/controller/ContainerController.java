package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ContainerActionEnum;
import com.github.coco.constant.dict.ContainerStatusEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.TimeFetchEnum;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.EnumHelper;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
        private String cmds;
        private String entrypoint;
        private Map<String, Objects> env;
        private Map<String, String> portMapping;
    }

    /**
     * 部署创建容器
     *
     * @param params
     * @return
     */
    @PostMapping(value = "/create")
    public Map<String, Object> createContainer(@RequestBody Map<String, Object> params) {
        DeployContainer deployContainer = JSON.parseObject(JSON.toJSONString(params), DeployContainer.class);
        String image                    = deployContainer.getImage();
        String name                     = deployContainer.getName();
        String cmds                     = deployContainer.getCmds();
        String entrypoint               = deployContainer.getEntrypoint();
        Map<String, Objects> env        = deployContainer.getEnv();
        Map<String, String> portMapping = deployContainer.getPortMapping();
        try {
            Set<String> ports = portMapping.keySet();
            Map<String, List<PortBinding>> portBindings = new HashMap<>(4);
            for (String port : ports) {
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.hostPort(portMapping.get(port)));
                portBindings.put(port, hostPorts);
            }
            HostConfig hostConfig = HostConfig.builder()
                                              .portBindings(portBindings)
                                              .build();

            ContainerConfig config = ContainerConfig.builder()
                                                    //.cmd(cmds.split(","))
                                                    //.entrypoint(entrypoint.split(","))
                                                    //.env(env.split(","))
                                                    .hostConfig(hostConfig)
                                                    .exposedPorts(ports)
                                                    .attachStdin(true)
                                                    .attachStdout(true)
                                                    .attachStderr(true)
                                                    .image(image)
                                                    .tty(false)
                                                    .build();
            String containerId = dockerClient.createContainer(config, name).id();
            dockerClient.startContainer(containerId);
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
    @PostMapping(value = "/remove")
    public Map<String, Object> removeContainer(@RequestBody Map<String, Object> params) {
        String containerId  = Objects.toString(params.get("containerId"), null);
        boolean force       = Boolean.parseBoolean(params.getOrDefault("force", false).toString());
        boolean withVolumes = Boolean.parseBoolean(params.getOrDefault("withVolumes", false).toString());
        if (containerId != null) {
            try {
                if (!Objects.equals(dockerClient.inspectContainer(containerId).state().status(),
                                    ContainerStatusEnum.EXITED.getStatus())) {
                    dockerClient.killContainer(containerId);
                }
                dockerClient.removeContainer(containerId,
                                             DockerClient.RemoveContainerParam.forceKill(force),
                                             DockerClient.RemoveContainerParam.removeVolumes(withVolumes));
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
    @PostMapping(value = "/rename")
    public Map<String, Object> renameContainer(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String name        = Objects.toString(params.get("name"), null);
        if (containerId != null) {
            try {
                dockerClient.renameContainer(containerId, name);
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
    @PostMapping(value = "/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String action = params.get("action").toString();
        ContainerActionEnum actionEnum = ContainerActionEnum.valueOf(action.toUpperCase());
        try {
            int waitSeconds;
            switch (actionEnum) {
                case START:
                    dockerClient.startContainer(containerId);
                    break;
                case RESTART:
                    waitSeconds = Integer.parseInt(params.getOrDefault("waitSeconds", 10).toString());
                    dockerClient.restartContainer(containerId, waitSeconds);
                    break;
                case STOP:
                    waitSeconds = Integer.parseInt(params.getOrDefault("waitSeconds", 10).toString());
                    dockerClient.stopContainer(containerId, waitSeconds);
                    break;
                case PAUSE:
                    dockerClient.pauseContainer(containerId);
                    break;
                case UPPAUSE:
                    dockerClient.unpauseContainer(containerId);
                    break;
                case KILL:
                    dockerClient.killContainer(containerId);
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
            List<Container> containers = dockerClient.listContainers(DockerFilterHelper.toArray(filters,
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
    @PostMapping(value = "/inspect")
    public Map<String, Object> getContainerInspect(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.inspectContainer(containerId));
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
                String logs = dockerClient.logs(containerId,
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
    @PostMapping(value = "/stats")
    public Map<String, Object> getContainerStats(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.stats(containerId));
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
    @PostMapping(value = "/top")
    public Map<String, Object> getContainerTop(@RequestBody Map<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), null);
        String psArgs      = Objects.toString(params.get("psArgs"), null);
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               dockerClient.topContainer(containerId, psArgs));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取容器进行信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
