package com.github.coco.schedule;

import com.alibaba.fastjson.JSON;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.dict.EndpointStatusEnum;
import com.github.coco.service.EndpointService;
import com.github.coco.service.StackService;
import com.github.coco.utils.DockerConnectorHelper;
import com.github.coco.utils.LoggerHelper;
import com.github.coco.utils.StringHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@Component
public class SyncEndpointTask {
    private static final String PING_RESULT = "OK";

    @Resource
    private EndpointService endpointService;

    @Resource
    private StackService stackService;

    public void syncEndpoints() {
        int endpointTotal = endpointService.getEndpointTotal();
        endpointService.getEndpoints(DbConstant.PAGE_NO, endpointTotal).forEach(endpoint -> {
            DockerClient dockerClient = DockerConnectorHelper.borrowDockerClient(endpoint);
            try {
                if (dockerClient != null) {
                    // 状态
                    int status;
                    if (PING_RESULT.equals(dockerClient.ping())) {
                        status = EndpointStatusEnum.UP.getCode();
                        // docker配置
                        Map<String, Object> dockerConfig = new HashMap<>(16);
                        dockerConfig.put("services", dockerClient.listServices().size());
                        dockerConfig.put("stacks", stackService.getStacks(endpoint.getPublicIp()).size());
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
                        dockerConfig.put("cluster", dockerClient.info().swarm() != null && dockerClient.info().swarm().controlAvailable() ? "Swarm集群" : "单点");
                        endpoint.setDockerConfig(JSON.toJSONString(dockerConfig));

                        // 资源
                        Map<String, Object> resources = new HashMap<>(2);
                        resources.put("memory", StringHelper.convertSize(dockerClient.info().memTotal()));
                        resources.put("cpus", String.format("%s 核", dockerClient.info().cpus()));
                        endpoint.setResources(JSON.toJSONString(resources));
                    } else {
                        status = EndpointStatusEnum.DOWN.getCode();
                    }
                    endpoint.setStatus(status);
                    endpoint.setUpdateDateTime(System.currentTimeMillis());
                    endpointService.modifyEndpoint(endpoint);
                }
            } catch (Exception e) {
                Map<String, String> message = new HashMap<>(4);
                message.put("publicIp", endpoint.getPublicIp());
                message.put("name", endpoint.getName());
                message.put("endpointUrl", endpoint.getEndpointUrl());
                LoggerHelper.fmtError(getClass(), e, "更新Docker终端服务%s异常", JSON.toJSONString(message));
            } finally {
                DockerConnectorHelper.returnDockerClient(dockerClient);
            }
        });
    }
}
