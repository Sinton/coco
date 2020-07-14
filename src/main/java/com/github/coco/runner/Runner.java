package com.github.coco.runner;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Yan
 */
@Component
public class Runner implements CommandLineRunner {
    @Resource
    private SocketIOServer wsServer;

    @Override
    public void run(String... args) {
        wsServer.start();
    }
}
