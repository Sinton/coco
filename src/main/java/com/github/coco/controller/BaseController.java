package com.github.coco.controller;

import com.github.coco.cache.GlobalCache;
import com.github.coco.dto.ApiResponseDTO;
import com.github.coco.utils.RuntimeContextHelper;
import com.spotify.docker.client.DockerClient;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@RestController
public class BaseController {
    @Resource
    protected GlobalCache globalCache;

    protected ApiResponseDTO apiResponseDTO = new ApiResponseDTO();

    protected DockerClient getDockerClient() {
        String token = RuntimeContextHelper.getToken().toString();
        return globalCache.getDockerClient(token);
    }

    protected void setDockerClient(DockerClient dockerClient) {
        String token = RuntimeContextHelper.getToken().toString();
        globalCache.putDockerClient(token, dockerClient);
    }

    /**
     * 清理Token
     */
    protected void evictToken() {
        String token = RuntimeContextHelper.getToken().toString();
        globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).evict(token);
        globalCache.removeDockerClient(token);
        globalCache.removeDockerClient(token);
    }
}
