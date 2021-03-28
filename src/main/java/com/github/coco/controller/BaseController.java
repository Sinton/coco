package com.github.coco.controller;

import com.github.coco.cache.GlobalCache;
import com.github.coco.dto.ApiResponseDTO;
import com.github.coco.entity.Endpoint;
import com.github.coco.entity.User;
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
        String token = RuntimeContextHelper.getToken();
        return globalCache.getDockerClient(token);
    }

    protected void setDockerClient(DockerClient dockerClient) {
        String token = RuntimeContextHelper.getToken();
        globalCache.putDockerClient(token, dockerClient);
    }

    protected Endpoint getEndpoint() {
        String token = RuntimeContextHelper.getToken();
        return globalCache.getEndpoint(token);
    }

    protected void setEndpoint(Endpoint endpoint) {
        String token = RuntimeContextHelper.getToken();
        globalCache.putEndpoint(token, endpoint);
    }

    protected String getToken() {
        return RuntimeContextHelper.getToken();
    }

    protected Integer getUserId() {
        return globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).get(getToken(), User.class).getUid();
    }

    /**
     * 清理Token
     */
    protected void evictToken() {
        String token = RuntimeContextHelper.getToken();
        globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).evict(token);
        globalCache.removeDockerClient(token);
        globalCache.removeSocketClient(token);
        globalCache.removeEndpoint(token);
    }
}
