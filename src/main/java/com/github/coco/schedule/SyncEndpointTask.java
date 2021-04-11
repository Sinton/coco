package com.github.coco.schedule;

import com.alibaba.fastjson.JSON;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.DockerConstant;
import com.github.coco.constant.dict.EndpointStatusEnum;
import com.github.coco.entity.Stack;
import com.github.coco.service.EndpointService;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.StringHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Slf4j
@Component
public class SyncEndpointTask {
    @Resource
    private EndpointService endpointService;

    @Resource
    private StackService stackService;

    @Scheduled(fixedDelayString = "${coco.sync-data.endpoint-interval}")
    public void syncEndpoints() {
        int endpointTotal = endpointService.getEndpointTotal();
        endpointService.getEndpoints(DbConstant.PAGE_NO, endpointTotal).forEach(endpoint -> {
            // TODO mybatis 值为null的BUG
            String bugVlaue = "0";
            if (StringUtils.isBlank(endpoint.getResources()) || endpoint.getResources().equals(bugVlaue)) {
                endpoint.setResources(null);
            }
            if (StringUtils.isBlank(endpoint.getDockerConfig()) || endpoint.getDockerConfig().equals(bugVlaue)) {
                endpoint.setDockerConfig(null);
            }
            DockerClient dockerClient = DockerConnectorHelper.borrowDockerClient(endpoint);
            if (dockerClient != null) {
                // 状态
                int status = EndpointStatusEnum.DOWN.getCode();
                try {
                    if (dockerClient.ping().equals(DockerConstant.PING_OK)) {
                        status = EndpointStatusEnum.UP.getCode();
                        // docker配置
                        Map<String, Object> dockerConfig = new HashMap<>(16);
                        if (dockerClient.info().swarm() != null && dockerClient.info().swarm().controlAvailable()) {
                            dockerConfig.put("services", dockerClient.listServices().size());
                            dockerConfig.put("mode", "cluster");
                        } else {
                            dockerConfig.put("mode", "standalone");
                        }
                        dockerConfig.put("stacks", stackService.getStackTotal(Stack.builder()
                                                                                   .endpoint(endpoint.getPublicIp())
                                                                                   .build()));
                        Map<String, Object> imageConfig = new HashMap<>(4);
                        imageConfig.put("total", dockerClient.listImages().size());
                        imageConfig.put("size", dockerClient.listImages().stream().mapToLong(Image::size).sum());
                        dockerConfig.put("images", imageConfig);

                        dockerConfig.put("volumes", Objects.isNull(dockerClient.listVolumes()) ? 0 : dockerClient.listVolumes().volumes().size());
                        dockerConfig.put("networks", dockerClient.listNetworks().size());

                        Map<String, Object> containerConfig = new HashMap<>(4);
                        containerConfig.put("total", dockerClient.info().containers());
                        containerConfig.put("running", dockerClient.info().containersRunning());
                        containerConfig.put("stoped", dockerClient.listContainers(DockerClient.ListContainersParam.withStatusExited()).size());
                        containerConfig.put("other", Integer.parseInt(containerConfig.get("total").toString()) - Integer.parseInt(containerConfig.get("running").toString()) - Integer.parseInt(containerConfig.get("stoped").toString()));
                        dockerConfig.put("containers", containerConfig);

                        dockerConfig.put("version", dockerClient.version().version());
                        endpoint.setDockerConfig(JSON.toJSONString(dockerConfig));

                        // 资源
                        Map<String, Object> resources = new HashMap<>(2);
                        resources.put("memory", StringHelper.convertSize(dockerClient.info().memTotal()));
                        resources.put("cpus", String.format("%s 核", dockerClient.info().cpus()));
                        endpoint.setResources(JSON.toJSONString(resources));
                    }
                } catch (DockerException | InterruptedException e) {
                    Map<String, String> message = new HashMap<>(4);
                    message.put("name", endpoint.getName());
                    message.put("publicIp", endpoint.getPublicIp());
                    message.put("endpointUrl", endpoint.getEndpointUrl());
                    log.error(String.format("更新Docker终端服务%s时发生异常", JSON.toJSONString(message)), e);
                } finally {
                    endpoint.setStatus(status);
                    endpoint.setUpdateDateTime(System.currentTimeMillis());
                    endpointService.modifyEndpoint(endpoint);
                    DockerConnectorHelper.returnDockerClient(endpoint, dockerClient);
                }
            }
        });
    }
}
