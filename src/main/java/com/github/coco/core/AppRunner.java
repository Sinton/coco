package com.github.coco.core;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@Component
public class AppRunner implements ApplicationRunner {
    @Resource
    private SocketIOServer socketServer;

    @Override
    public void run(ApplicationArguments args) {
        socketServer.start();
    }
}
