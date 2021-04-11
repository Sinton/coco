package com.github.coco.controller;

import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.dict.CacheTypeEnum;
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

    /**
     * 从上下文中获取Docker客户端
     *
     * @return
     */
    protected DockerClient getDockerClient() {
        String token = RuntimeContextHelper.getToken();
        return globalCache.getDockerClient(token);
    }

    protected void setDockerClient(DockerClient dockerClient) {
        String token = RuntimeContextHelper.getToken();
        globalCache.putDockerClient(token, dockerClient);
    }

    /**
     * 从上下文中获取Docker服务终端
     *
     * @return
     */
    protected Endpoint getEndpoint() {
        String token = RuntimeContextHelper.getToken();
        return globalCache.getEndpoint(token);
    }

    protected void setEndpoint(Endpoint endpoint) {
        String token = RuntimeContextHelper.getToken();
        globalCache.putEndpoint(token, endpoint);
    }

    /**
     * 从上下文中获取用户Token
     * @return
     */
    protected String getToken() {
        return RuntimeContextHelper.getToken();
    }

    /**
     * 从上下文中获取用户
     *
     * @return
     */
    protected User getUser() {
        return globalCache.getCache(CacheTypeEnum.TOKEN).get(getToken(), User.class);
    }

    /**
     * 从上下文中获取用户ID
     *
     * @return
     */
    protected Integer getUserId() {
        return getUser().getUid();
    }

    /**
     * 清理Token
     */
    protected void evictToken() {
        String token = RuntimeContextHelper.getToken();
        globalCache.getCache(CacheTypeEnum.TOKEN).evict(token);
        globalCache.removeDockerClient(token);
        globalCache.removeSocketClient(token);
        globalCache.removeEndpoint(token);
    }
}
