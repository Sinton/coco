package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.coco.annotation.WebLog;
import com.github.coco.compose.ComposeConfig;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.EndpointTypeEnum;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.constant.dict.StackTypeEnum;
import com.github.coco.entity.Stack;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerComposeHelper;
import com.github.coco.utils.DockerStackHelper;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @WebLog
    @PostMapping("/create")
    public Map<String, Object> createStack(@RequestBody Map<String, Object> params) {
        try {
            String fileUuid = Objects.toString(params.get("fileUuid"), "");
            String composeContent = Objects.toString(params.get("content"), "");
            Map<String, String> envs = JSON.parseObject(Objects.toString(params.get("envs"), "{}"),
                                                        new TypeReference<Map<String, String>>() {});
            Stack stack = Stack.builder().endpoint(getEndpoint().getEndpointUrl()).build();
            stackService.createStack(stack);

            // 生成docker-compose.yml文件或者docker-stack.yml文件
            String filename = DockerComposeHelper.getComposeYamlFilePath(stack.getId());
            FileUtils.touch(FileUtils.getFile(filename));
            if (StringUtils.isNotBlank(composeContent)) {
                try {
                    IOUtils.copy(IOUtils.toInputStream(composeContent, StandardCharsets.UTF_8),
                                 new FileOutputStream(filename));
                } catch (IOException e) {
                    log.error("生成docker-compose.yml文件发生异常", e);
                }
            } else if (StringUtils.isNotBlank(fileUuid)) {
                try {
                    IOUtils.copy(new FileInputStream(String.format("%s/%s.data",
                                                                   GlobalConstant.TEMP_STORAGE_PATH,
                                                                   fileUuid)),
                                 new FileOutputStream(filename));
                } catch (IOException e) {
                    log.error("生成docker-compose.yml文件发生异常", e);
                }
            } else {
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "docker-compose内容或者文件不能为空");
            }
            ComposeConfig composeConfig;
            ComposeConfig.ComposeConfigBuilder composeConfigBuilder = ComposeConfig.builder().daemon(true);
            if (getEndpoint().getEndpointType().equals(EndpointTypeEnum.URL.getCode())) {
                composeConfigBuilder.remote(getEndpoint().getEndpointUrl());
            }

            if (getDockerClient().info().swarm() != null && getDockerClient().info().swarm().controlAvailable()) {
                String namespace = Objects.toString(params.get("namespace"), "");
                if (StringUtils.isNotBlank(namespace)) {
                    stack.setName(namespace);
                    DockerStackHelper.deployStack(getDockerClient(), namespace);
                    stack.setType(StackTypeEnum.SWARM.getCode());
                    stackService.modifyStack(stack);
                }
            } else {
                String project = Objects.toString(params.get("project"), "");
                if (StringUtils.isNotBlank(project)) {
                    stack.setName(project);
                    composeConfig = composeConfigBuilder.project(project).projectId(stack.getId()).build();
                    stack.setType(StackTypeEnum.COMPOSE.getCode());
                    stackService.modifyStack(stack);
                    System.out.println(JSON.toJSONString(composeConfig));
                    /*if (DockerComposeHelper.validateComposeFile(composeConfig)) {
                        DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.UP, composeConfig);
                        stack.setType(StackTypeEnum.COMPOSE.getCode());
                        stackService.modifyStack(stack);
                    } else {
                        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "docker-compose文件内容校验不通过");
                    }*/
                }
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
