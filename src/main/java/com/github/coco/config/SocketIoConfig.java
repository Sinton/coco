package com.github.coco.config;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.CacheTypeEnum;
import com.github.coco.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@Configuration
public class SocketIoConfig {
    @Resource
    private GlobalCache globalCache;

    @Value("${socketio.host}")
    private String host;

    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.bossCount}")
    private int bossCount;

    @Value("${socketio.workCount}")
    private int workCount;

    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout}")
    private int pingTimeout;

    @Value("${socketio.pingInterval}")
    private int pingInterval;

    @Bean
    public SocketIOServer socketIOServer() {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);
        config.setHostname(host);
        config.setPort(port);
        config.setBossThreads(bossCount);
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);
        config.setAuthorizationListener(data -> {
            if (data.getUrlParams().containsKey(GlobalConstant.ACCESS_TOKEN)) {
                String token = data.getUrlParams().get(GlobalConstant.ACCESS_TOKEN).get(0);
                if (StringUtils.isNotBlank(token)) {
                     return globalCache.getCache(CacheTypeEnum.TOKEN).get(token, User.class) != null;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        });
        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer ioServer) {
        return new SpringAnnotationScanner(ioServer);
    }
}
