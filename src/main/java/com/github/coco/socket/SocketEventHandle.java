package com.github.coco.socket;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.coco.cache.GlobalCache;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.handle.ExecSession;
import com.github.coco.terminal.TerminalConnect;
import com.github.coco.terminal.WebTerminalUser;
import com.github.coco.thread.OutPutThread;
import com.github.coco.utils.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerHost;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yan
 */
@Component
public class SocketEventHandle {
    public static final String SOCKET_EVENT_CONNECT_TERMINAL           = "connectTerminal";
    public static final String SOCKET_EVENT_TERMINAL                   = "terminal";
    public static final String SOCKET_EVENT_CONTAINER_TERMINAL         = "containerTerminal";
    public static final String SOCKET_EVENT_CONNECT_CONTAINER_TERMINAL = "connectContainerTerminal";

    @Resource
    private GlobalCache globalCache;

    public static volatile Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>(16);
    private static Map<String, ExecSession> execSessionMap = new ConcurrentHashMap<>(16);
    private static Map<String, Object> terminalMap = new ConcurrentHashMap<>(16);

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam(GlobalConstant.ACCESS_TOKEN);
        globalCache.putSocketClient(token, client);
        clientMap.put(token, client);
        LoggerHelper.fmtInfo(getClass(), String.format("客户端 %s 建立连接", token));
        initConnection(client);
    }

    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam(GlobalConstant.ACCESS_TOKEN);
        client.disconnect();
        globalCache.removeSocketClient(token);
        clientMap.remove(token);
        disconnectTerminal(client);
        LoggerHelper.fmtInfo(getClass(), String.format("客户端 %s 断开连接", token));
    }

    @OnEvent(SOCKET_EVENT_CONTAINER_TERMINAL)
    public void onConnectContainerTerminal(SocketIOClient client, AckRequest request, HashMap<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), "");
        String cmd         = params.getOrDefault("command", "").toString();
        boolean stdErr     = Boolean.parseBoolean(Objects.toString(params.get("stdErr"), "true"));
        boolean stdOut     = Boolean.parseBoolean(Objects.toString(params.get("stdOut"), "true"));
        boolean stdIn      = Boolean.parseBoolean(Objects.toString(params.get("stdIn"), "true"));
        boolean privileged = Boolean.parseBoolean(Objects.toString(params.get("privileged"), "true"));
        boolean tty        = Boolean.parseBoolean(Objects.toString(params.get("tty"), "true"));
        boolean detach     = Boolean.parseBoolean(Objects.toString(params.get("detach"), "false"));
        try {
            ExecSession execSession = execSessionMap.get(containerId);
            if (execSession != null) {
                OutputStream out = execSession.getSocket().getOutputStream();
                out.write(cmd.getBytes(StandardCharsets.UTF_8));
                out.flush();
            } else {
                String token = client.getHandshakeData().getSingleUrlParam(GlobalConstant.ACCESS_TOKEN);
                DockerClient dockerClient = globalCache.getDockerClient(token);
                String execId = dockerClient.execCreate(containerId,
                                                        new String[]{"bash"},
                                                        DockerClient.ExecCreateParam.attachStderr(stdErr),
                                                        DockerClient.ExecCreateParam.attachStdin(stdIn),
                                                        DockerClient.ExecCreateParam.attachStdout(stdOut),
                                                        DockerClient.ExecCreateParam.privileged(privileged),
                                                        DockerClient.ExecCreateParam.tty(tty),
                                                        DockerClient.ExecCreateParam.detach(detach))
                                            .id();
                Socket socket = connectContainerTerminalSocket(client, dockerClient, execId);
                getTerminalContent(containerId, client, socket);
                // resizeTty(dockerClient, execId, 200, 200);
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "与容器[%s]建立终端连接异常", containerId);
        }
    }

    @OnEvent("closeContainerTerminal")
    public void onCloseContainerTerminal(SocketIOClient client, AckRequest request, HashMap<String, Object> params) {
        String containerId = Objects.toString(params.get("containerId"), "");
        ExecSession execSession = execSessionMap.get(containerId);
        if (execSession != null) {
            execSessionMap.remove(containerId);
        }
    }

    private void resizeTty(DockerClient dockerClient, String execId, int height, int width) {
        try {
            dockerClient.execResizeTty(execId, height, width);
        } catch (DockerException | InterruptedException e) {
            LoggerHelper.fmtError(getClass(), e, "对容器终端调整tty异常");
        }
    }

    private Socket connectContainerTerminalSocket(SocketIOClient client, DockerClient dockerClient, String execId) throws IOException {
        Map<String, Object> args = new HashMap<>(2);
        args.put("Detach", false);
        args.put("Tty", true);
        Socket socket = new Socket(dockerClient.getHost(), DockerHost.defaultPort());
        socket.setKeepAlive(true);
        StringBuilder httpHeader = new StringBuilder().append("POST /exec/")
                                                      .append(execId)
                                                      .append("/start HTTP/1.1\r\n")
                                                      .append("Host: ")
                                                      .append(dockerClient.getHost())
                                                      .append(":2375\r\n")
                                                      .append("User-Agent: Docker-Client\r\n")
                                                      .append("Content-Type: application/json\r\n")
                                                      .append("Connection: Upgrade\r\n")
                                                      .append("Content-Length: ")
                                                      .append(JSON.toJSONString(args).length())
                                                      .append("\r\n")
                                                      .append("Upgrade: tcp\r\n")
                                                      .append("\r\n")
                                                      .append(JSON.toJSONString(args));
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(httpHeader.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        return socket;
    }

    private void getTerminalContent(String containerId, SocketIOClient client, Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        // 删除请求信息
        StringBuilder terminalContent = new StringBuilder();
        byte[] bytes = new byte[1024 * 8];
        while (true) {
            int n = inputStream.read(bytes);
            String msg = new String(bytes, 0, n);
            terminalContent.append(msg);
            bytes = new byte[1024 * 8];
            if (terminalContent.indexOf("\r\n\r\n") != -1) {
                client.sendEvent("terminal", terminalContent.substring(terminalContent.indexOf("\r\n\r\n") + 4, terminalContent.length()));
                break;
            }
        }
        OutPutThread outPutThread = new OutPutThread(inputStream, socket, client);
        outPutThread.start();
        execSessionMap.put(containerId, new ExecSession(socket, outPutThread));
    }

    public void initConnection(SocketIOClient client) {
        TerminalConnect terminalConnect = new TerminalConnect();
        terminalConnect.setJSch(new JSch());
        terminalConnect.setSocketClient(client);
        String sessionId = client.getSessionId().toString();
        terminalMap.put(sessionId, terminalConnect);
    }

    @OnEvent(SOCKET_EVENT_CONNECT_TERMINAL)
    public void onConnectTerminal(SocketIOClient client, AckRequest request, Object data) {
        try {
            WebTerminalUser webTerminalUser = JSON.parseObject(JSON.toJSONString(data), WebTerminalUser.class);
            String sessionId = client.getSessionId().toString();
            TerminalConnect terminalConnect = (TerminalConnect) terminalMap.get(sessionId);
            ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE).execute(() -> {
                connectTerminal(terminalConnect, webTerminalUser, client);
            });
        } catch (Exception e) {
            disconnectTerminal(client);
            LoggerHelper.fmtError(getClass(), e, "Terminal连接异常");
        }
    }

    @OnEvent(SOCKET_EVENT_TERMINAL)
    public void onTerminal(SocketIOClient client, AckRequest request, Object data) {
        try {
            WebTerminalUser webTerminalUser = JSON.parseObject(JSON.toJSONString(data), WebTerminalUser.class);
            String sessionId = client.getSessionId().toString();
            if ("command".equals(webTerminalUser.getOperate())) {
                String command = webTerminalUser.getCommand();
                TerminalConnect terminalConnect = (TerminalConnect) terminalMap.get(sessionId);
                if (terminalConnect != null) {
                    try {
                        if (terminalConnect.getChannel() != null) {
                            OutputStream outputStream = terminalConnect.getChannel().getOutputStream();
                            outputStream.write(command.getBytes());
                            outputStream.flush();
                        }
                    } catch (IOException e) {
                        LoggerHelper.fmtError(getClass(), e, "Terminal连接异常");
                        disconnectTerminal(client);
                    }
                }
            } else {
                LoggerHelper.error(getClass(), "不支持的操作");
                disconnectTerminal(client);
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "Json转换异常");
        }
    }

    private void connectTerminal(TerminalConnect terminalConnect, WebTerminalUser webTerminalUser, SocketIOClient client) {
        Session session = null;
        Channel channel = null;
        InputStream inputStream = null;
        try {
            session = terminalConnect.getJSch()
                                     .getSession(webTerminalUser.getUsername(),
                                                 webTerminalUser.getHost(),
                                                 webTerminalUser.getPort());
            Properties terminalConfig = new Properties();
            terminalConfig.put("StrictHostKeyChecking", "no");
            // 设置session
            session.setConfig(terminalConfig);
            session.setPassword(webTerminalUser.getPassword());
            session.connect(30 * 1000);
            // 设置连接信道
            channel = session.openChannel("shell");
            channel.connect(3 * 1000);
            terminalConnect.setChannel(channel);
            // 读取终端返回的信息流
            inputStream = channel.getInputStream();
            byte[] buffer = new byte[1024];
            int i = 0;
            // 如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                client.sendEvent("terminal", new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "读取终端命令错误");
        } finally {
            // 关闭所有会话、连接信道、连接
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    public void disconnectTerminal(SocketIOClient session) {
        String sessionId = String.valueOf(session.getSessionId());
        TerminalConnect terminalConnect = (TerminalConnect) terminalMap.get(sessionId);
        if (terminalConnect != null) {
            // 断开连接
            if (terminalConnect.getChannel() != null) {
                terminalConnect.getChannel().disconnect();
            }
            // 移除终端连接
            terminalMap.remove(sessionId);
        }
    }

    /**
     * 每5分钟定时清理token失效的Socket客户端连接
     */
    @Scheduled(fixedDelay = 5 * DateHelper.MINUTE)
    private void clearInvalidSocketClient() {
        List<String> invalidTokens = new ArrayList<>();
        clientMap.forEach((token, client) -> {
            if (globalCache.getCache(GlobalCache.CacheTypeEnum.TOKEN).get(token) == null) {
                invalidTokens.add(token);
            }
        });
        invalidTokens.forEach(token -> clientMap.remove(token));
    }
}
