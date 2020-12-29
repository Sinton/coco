package com.github.coco.cache;

import com.corundumstudio.socketio.SocketIOClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yan
 */
@Component
public class GlobalCache {
    public enum CacheTypeEnum {
        /**
         * 鉴权令牌
         */
        TOKEN("TokenCache");
        String name;

        CacheTypeEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Resource
    private CacheManager cacheManager;

    private Cache tokenCache;
    private static volatile Map<String, DockerClient> dockerClients;
    private static volatile Map<String, SocketIOClient> socketClients;

    @PostConstruct
    private void init() {
        tokenCache = cacheManager.getCache(CacheTypeEnum.TOKEN.getName());
        dockerClients = new ConcurrentHashMap<>(16);
        socketClients = new ConcurrentHashMap<>(16);
    }

    public void putDockerClient(String token, DockerClient dockerClient) {
        dockerClients.put(token, dockerClient);
    }

    public DockerClient getDockerClient(String token) {
        return dockerClients.get(token);
    }

    public void removeDockerClient(String token) {
        dockerClients.remove(token);
    }

    public void putSocketClient(String token, SocketIOClient socketClient) {
        socketClients.put(token, socketClient);
    }

    public SocketIOClient getSocketClient(String token) {
        return socketClients.get(token);
    }

    public void removeSocketClient(String token) {
        socketClients.remove(token);
    }

    public Cache getCache(CacheTypeEnum cacheType) {
        switch (cacheType) {
            case TOKEN:
                return tokenCache;
            default:
                return null;
        }
    }
}
