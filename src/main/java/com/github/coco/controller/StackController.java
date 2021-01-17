package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.compose.ComposeConfig;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.entity.Stack;
import com.github.coco.schedule.SyncStackTask;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerComposeHelper;
import com.github.coco.utils.DockerStackHelper;
import com.spotify.docker.client.exceptions.DockerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/stack")
public class StackController extends BaseController {
    @Resource
    private StackService stackService;

    @Resource
    private SyncStackTask syncStackTask;

    @WebLog
    @PostMapping("/create")
    public Map<String, Object> createStack(@RequestBody Map<String, Object> params) {
        try {
            if (getDockerClient().info().swarm() != null && getDockerClient().info().swarm().controlAvailable()) {
                String namespace = Objects.toString(params.get("namespace"), "");
                if (StringUtils.isNotBlank(namespace)) {
                    DockerStackHelper.deployStack(getDockerClient(), namespace);
                }
            } else {
                String project = Objects.toString(params.get("project"), "");
                if (StringUtils.isNotBlank(project)) {
                    ComposeConfig composeConfig = ComposeConfig.builder()
                                                               .project(project)
                                                               .build();
                    if (DockerComposeHelper.validateComposeFile(composeConfig)) {
                        DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.UP, composeConfig);
                    } else {
                        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "docker-compose文件内容校验不通过");
                    }
                }
            }
        } catch (DockerException | InterruptedException e) {
            log.error("创建/部署应用栈失败", e);
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "创建/部署应用栈成功");
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeStack(@RequestBody Map<String, Object> params) {
        try {
            if (getDockerClient().info().swarm().controlAvailable()) {
                String namespace = Objects.toString(params.get("namespace"), "");
                if (StringUtils.isNotBlank(namespace)) {
                    DockerStackHelper.removeStack(getDockerClient(), namespace);
                }
            } else {
                String project = Objects.toString(params.get("project"), "");
                if (StringUtils.isNotBlank(project)) {
                    ComposeConfig composeConfig = ComposeConfig.builder()
                                                               .project(project)
                                                               .build();
                    if (DockerComposeHelper.validateComposeFile(composeConfig)) {
                        DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.RM, composeConfig);
                    } else {
                        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "docker-compose文件内容校验不通过");
                    }
                }
            }
        } catch (DockerException | InterruptedException e) {
            log.error("删除应用栈失败", e);
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "删除应用栈成功");
    }

    @WebLog
    @PostMapping(value = "/update")
    public Map<String, Object> updateStack(@RequestBody Map<String, Object> params) {
        return createStack(params);
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageStacks(@RequestBody Map<String, Object> params) {
        syncStackTask.syncStacks();

        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        try {
            List<Stack> stacks = stackService.getStacks(getDockerClient().getHost());
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, stacks));
        } catch (Exception e) {
            log.error("获取应用栈列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
