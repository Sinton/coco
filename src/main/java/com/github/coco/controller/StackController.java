package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.compose.ComposeConfig;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.StackTypeEnum;
import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Stack;
import com.github.coco.schedule.SyncStackTask;
import com.github.coco.service.StackService;
import com.github.coco.utils.docker.DockerComposeHelper;
import com.github.coco.utils.docker.DockerStackHelper;
import com.github.coco.utils.EnumHelper;
import com.github.coco.utils.ThreadPoolHelper;
import com.spotify.docker.client.exceptions.DockerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

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
            Map<String, String> envs = JSON.parseObject(Objects.toString(params.get("envs"), "{}"),
                                                        new TypeReference<Map<String, String>>() {});
            String stackName = Objects.toString(params.get("name"), "");
            StackTypeEnum stackType = EnumHelper.getEnumType(StackTypeEnum.class,
                                                             Objects.toString(params.get("type"),
                                                                              String.valueOf(StackTypeEnum.COMPOSE)));
            String yamlType = Objects.toString(params.get("yamlType"), "");
            String yamlContent = Objects.toString(params.get("content"), "");

            Stack stack = Stack.builder()
                               .name(stackName)
                               .type(stackType.getCode())
                               .endpoint(getEndpoint().getPublicIp())
                               .owner(getUserId())
                               .internal(WhetherEnum.YES.getCode())
                               .build();
            if (StringUtils.isNotBlank(stackName)) {
                if (stackService.getStack(stack) == null) {
                    stackService.createStack(stack);
                    // 生成docker-compose.yml文件或者docker-stack.yml文件
                    String filename = DockerComposeHelper.getComposeYamlFilePath(stack.getId());
                    FileUtils.touch(FileUtils.getFile(filename));
                    stack.setProjectPath(filename);
                    switch (yamlType) {
                        case "online":
                            if (StringUtils.isNotBlank(yamlContent)) {
                                try {
                                    IOUtils.copy(IOUtils.toInputStream(yamlContent, StandardCharsets.UTF_8),
                                                 new FileOutputStream(filename));
                                } catch (IOException e) {
                                    log.error("生成docker-compose.yml文件发生异常", e);
                                }
                            } else {
                                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "docker-compose内容或者文件不能为空");
                            }
                            break;
                        case "upload":
                            String fileUuid = Objects.toString(params.get("fileUuid"), "");
                            try {
                                IOUtils.copy(new FileInputStream(String.format("%s/%s.data",
                                                                               GlobalConstant.TEMP_STORAGE_PATH,
                                                                               fileUuid)),
                                                                 new FileOutputStream(filename));
                            } catch (IOException e) {
                                log.error("生成docker-compose.yml文件发生异常", e);
                            }
                            break;
                        default:
                            break;
                    }

                    // 执行应用栈部署
                    switch (stackType) {
                        case COMPOSE:
                            ComposeConfig.ComposeConfigBuilder composeConfigBuilder = ComposeConfig.builder().daemon(true);
                            // 是否需要远程TCP通讯连接
                            if (getEndpoint().getEndpointType().equals(EndpointTypeEnum.URL.getCode())) {
                                composeConfigBuilder.remote(getEndpoint().getEndpointUrl());
                            }
                            ComposeConfig composeConfig = composeConfigBuilder.project(stackName)
                                                                              .projectId(stack.getId())
                                                                              .build();

                            if (DockerComposeHelper.validateComposeFile(composeConfig)) {
                                DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.UP, composeConfig);
                            } else {
                                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "docker-compose文件内容校验不通过");
                            }
                            break;
                        case SWARM:
                            if (getDockerClient().info().swarm() != null && getDockerClient().info().swarm().controlAvailable()) {
                                DockerStackHelper.deployStack(getDockerClient(), stackName);
                            } else {
                                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "该节点不是Swarm集群，无法部署Swarm模式的应用栈");
                            }
                            break;
                        default:
                            break;
                    }
                    stackService.modifyStack(stack);
                } else {
                    return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "已经存在同名的应用栈");
                }
            } else {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "应用栈名称不能为空");
            }
        } catch (DockerException | InterruptedException | IOException e) {
            log.error("创建/部署应用栈失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "创建/部署应用栈成功");
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeStack(@RequestBody Map<String, Object> params) {
        try {
            StackTypeEnum stackType = EnumHelper.getEnumType(StackTypeEnum.class,
                                                             Objects.toString(params.get("type"),
                                                                              String.valueOf(StackTypeEnum.COMPOSE)));
            Integer stackId = Integer.parseInt(Objects.toString(params.get("stackId"), "0"));
            boolean pruneVolume = Boolean.parseBoolean(Objects.toString(params.get("pruneVolume"), Boolean.FALSE.toString()));
            Stack stack = stackService.getStack(Stack.builder().id(stackId).type(stackType.getCode()).build());
            if (stack != null) {
                // 执行应用栈部署
                switch (stackType) {
                    case COMPOSE:
                        ComposeConfig composeConfig = ComposeConfig.builder()
                                                                   .project(stack.getName())
                                                                   .projectId(stack.getId())
                                                                   .pruneVolume(pruneVolume)
                                                                   .build();
                        DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.DOWN, composeConfig);
                        break;
                    case SWARM:
                        if (getDockerClient().info().swarm() != null && getDockerClient().info().swarm().controlAvailable()) {
                            DockerStackHelper.removeStack(getDockerClient(), stack.getName());
                        } else {
                            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "该节点不是Swarm集群，无法卸载Swarm模式的应用栈");
                        }
                        break;
                    default:
                        break;
                }
                // 删除应用栈数据、docker-compose.yml文件
                stackService.removeStack(stack);
                FileUtils.deleteDirectory(new File(DockerComposeHelper.getComposeYamlFilePath(stack.getId())).getParentFile());
            } else {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "应用栈ID不能为空");
            }
        } catch (DockerException | InterruptedException | IOException e) {
            log.error("删除应用栈失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
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
        // 异步更新应用栈
        ThreadPoolExecutor threadPool = ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE);
        threadPool.submit(() -> syncStackTask.syncStacks());

        int pageNo = Integer.parseInt(Objects.toString(params.get("pageNo"), String.valueOf(DbConstant.PAGE_NO)));
        int pageSize = Integer.parseInt(Objects.toString(params.get("pageSize"), String.valueOf(DbConstant.PAGE_SIZE)));
        try {
            List<Stack> stacks = stackService.getStacks(Stack.builder().endpoint(getEndpoint().getPublicIp()).build(),
                                                        pageNo,
                                                        pageSize);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, stacks));
        } catch (Exception e) {
            log.error("获取应用栈列表失败", e);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
