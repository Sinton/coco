package com.github.coco.core;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @author Yan
 */
@Component
public class AppRunner {
    @Resource
    private SocketIOServer socketServer;

    @EventListener
    @Async
    public void onRefreshScopeRefreshed(final ContextRefreshedEvent event) {
        this.socketServer.start();
    }

    @PreDestroy
    private void socketDestroy() {
        if (socketServer != null) {
            socketServer.stop();
            socketServer = null;
        }
    }
}
