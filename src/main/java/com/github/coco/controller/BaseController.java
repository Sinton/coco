package com.github.coco.controller;

import com.github.coco.cache.GlobalCache;
import com.github.coco.dto.ApiResponseDTO;
import com.github.coco.utils.RuntimeContextHelper;
import com.spotify.docker.client.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yan
 */
@RestController
public class BaseController {
    @Autowired
    protected GlobalCache globalCache;

    protected ApiResponseDTO apiResponseDTO = new ApiResponseDTO();

    protected DockerClient getDockerClient() {
        String token = RuntimeContextHelper.getToken().toString();
        return getDockerClient(token);
    }

    protected DockerClient getDockerClient(String token) {
        return globalCache.getDockerClient(token);
    }

    protected void setDockerClient(String token, DockerClient dockerClient) {
        globalCache.putDockerClient(token, dockerClient);
    }
}
