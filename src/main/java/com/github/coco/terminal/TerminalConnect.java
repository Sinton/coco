package com.github.coco.terminal;

import com.corundumstudio.socketio.SocketIOClient;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Data;

/**
 * @author Yan
 */
@Data
public class TerminalConnect {
    private SocketIOClient socketClient;
    private JSch jSch;
    private Channel channel;
}
