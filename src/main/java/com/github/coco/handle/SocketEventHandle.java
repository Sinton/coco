package com.github.coco.handle;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.coco.factory.DockerConnector;
import com.github.coco.thread.OutPutThread;
import com.github.coco.utils.LoggerHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yan
 */
@Component
public class SocketEventHandle {
    private static DockerClient dockerClient = DockerConnector.getInstance().getDockerClient();

    public static volatile Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>(16);
    private static Map<String, ExecSession> execSessionMap = new ConcurrentHashMap<>(16);

    @OnConnect
    public void onConnect(SocketIOClient client) {
        clientMap.put(client.getSessionId().toString(), client);
        LoggerHelper.fmtInfo(getClass(), String.format("客户端 %s 在%s 接入", client.getSessionId(), client.getRemoteAddress()));
    }

    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        client.disconnect();
        clientMap.remove(client.getSessionId().toString(), client);
        LoggerHelper.fmtInfo(getClass(), String.format("客户端 %s 在%s 断开连接", client.getSessionId(), client.getRemoteAddress()));
    }

    @OnEvent("terminal")
    public void onExecContainerMessage(SocketIOClient client, AckRequest request, HashMap<String, Object> params) {
        String containerId = params.getOrDefault("containerId", null).toString();
        String cmd = params.getOrDefault("cmd", "").toString() + System.lineSeparator();
        boolean stdErr = Boolean.parseBoolean(params.getOrDefault("stdErr", true).toString());
        boolean stdOut = Boolean.parseBoolean(params.getOrDefault("stdOut", true).toString());
        boolean stdIn = Boolean.parseBoolean(params.getOrDefault("stdIn", true).toString());
        boolean privileged = Boolean.parseBoolean(params.getOrDefault("privileged", true).toString());
        boolean tty = Boolean.parseBoolean(params.getOrDefault("tty", true).toString());
        boolean detach = Boolean.parseBoolean(params.getOrDefault("detach", false).toString());
        try {
            ExecSession execSession = execSessionMap.get(containerId);
            if (execSession != null) {
                OutputStream out = execSession.getSocket().getOutputStream();
                out.write(cmd.getBytes(StandardCharsets.UTF_8));
                out.flush();
            } else {
                String execId = dockerClient.execCreate(containerId,
                                                        new String[]{"bash"},
                                                        DockerClient.ExecCreateParam.attachStderr(stdErr),
                                                        DockerClient.ExecCreateParam.attachStdin(stdIn),
                                                        DockerClient.ExecCreateParam.attachStdout(stdOut),
                                                        DockerClient.ExecCreateParam.privileged(privileged),
                                                        DockerClient.ExecCreateParam.tty(tty),
                                                        DockerClient.ExecCreateParam.detach(detach))
                                            .id();
                Socket socket = connectExec(client, dockerClient, execId);
                getExecMessage(containerId, client, socket);
                // resizeTty(dockerClient, execId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resizeTty(DockerClient dockerClient, String execId) throws DockerException, InterruptedException {
        dockerClient.execResizeTty(execId, 200, 200);
    }

    private Socket connectExec(SocketIOClient client, DockerClient dockerClient, String execId) throws IOException {
        Socket socket = new Socket(dockerClient.getHost(), 2375);
        socket.setKeepAlive(true);
        StringBuilder httpHeader = new StringBuilder();
        JSONObject args = new JSONObject();
        args.put("Detach", false);
        args.put("Tty", true);
        String json = args.toJSONString();
        httpHeader.append("POST /exec/")
                  .append(execId)
                  .append("/start HTTP/1.1\r\n")
                  .append("Host: ")
                  .append(dockerClient.getHost())
                  .append(":2375\r\n")
                  .append("User-Agent: Docker-Client\r\n")
                  .append("Content-Type: application/json\r\n")
                  .append("Connection: Upgrade\r\n")
                  .append("Content-Length: ")
                  .append(json.length())
                  .append("\r\n")
                  .append("Upgrade: tcp\r\n")
                  .append("\r\n")
                  .append(json);
        OutputStream out = socket.getOutputStream();
        out.write(httpHeader.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
        return socket;
    }

    private void getExecMessage(String containerId, SocketIOClient client, Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        // 删除请求信息
        StringBuilder execMessage = new StringBuilder();
        byte[] bytes = new byte[1024 * 8];
        while (true) {
            int n = inputStream.read(bytes);
            String msg = new String(bytes, 0, n);
            execMessage.append(msg);
            bytes = new byte[1024 * 8];
            if (execMessage.indexOf("\r\n\r\n") != -1) {
                client.sendEvent("terminal", execMessage.substring(execMessage.indexOf("\r\n\r\n") + 4, execMessage.length()));
                break;
            }
        }
        OutPutThread outPutThread = new OutPutThread(inputStream, socket, client);
        outPutThread.start();
        execSessionMap.put(containerId, new ExecSession(socket,outPutThread));
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void test() {
        clientMap.forEach((token, client) -> {
            client.sendEvent("test", "测试");
        });
    }

    @OnEvent("test")
    public void ontest(SocketIOClient client, AckRequest request, HashMap<String, Object> params) {
        LoggerHelper.fmtInfo(getClass(), params.toString());
    }
}
