package com.github.coco.thread;

import com.corundumstudio.socketio.SocketIOClient;
import com.github.coco.socket.SocketEventHandle;

import java.io.InputStream;
import java.net.Socket;

/**
 * @author Yan
 */
public class OutPutThread extends Thread {
    private InputStream inputStream;
    private SocketIOClient client;
    private Socket socket;

    public OutPutThread(InputStream inputStream, Socket socket, SocketIOClient client) {
        this.inputStream = inputStream;
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[1024 * 8];
            while (!socket.isClosed()) {
                int n = inputStream.read(bytes);
                if (n > 0) {
                    String msg = new String(bytes, 0, n);
                    client.sendEvent(SocketEventHandle.SOCKET_EVENT_TERMINAL, msg);
                    bytes = new byte[1024 * 8];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}