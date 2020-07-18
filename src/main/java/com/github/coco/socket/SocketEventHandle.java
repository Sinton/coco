package com.github.coco.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.github.coco.factory.DockerConnector;
import com.github.coco.handle.ExecSession;
import com.github.coco.terminal.TerminalConnect;
import com.github.coco.terminal.WebTerminalUser;
import com.github.coco.thread.OutPutThread;
import com.github.coco.utils.LoggerHelper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        initConnection(client);
        LoggerHelper.fmtInfo(getClass(), String.format("客户端 %s 在%s 接入", client.getSessionId(), client.getRemoteAddress()));
    }

    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        client.disconnect();
        clientMap.remove(client.getSessionId().toString(), client);
        disconnectTerminal(client);
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

    private Map<String, Object> terminalMap = new ConcurrentHashMap<>(16);
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void initConnection(SocketIOClient client) {
        TerminalConnect terminalConnect = new TerminalConnect();
        terminalConnect.setJSch(new JSch());
        terminalConnect.setSocketClient(client);
        String sessionId = client.getSessionId().toString();
        terminalMap.put(sessionId, terminalConnect);
    }

    @OnEvent("connectTerminal")
    public void onConnectTerminal(SocketIOClient client, AckRequest request, Object data) {
        try {
            WebTerminalUser webTerminalUser = JSON.parseObject(JSON.toJSONString(data), WebTerminalUser.class);
            String sessionId = client.getSessionId().toString();
            TerminalConnect terminalConnect = (TerminalConnect) terminalMap.get(sessionId);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        connectTerminal(terminalConnect, webTerminalUser, client);
                    } catch (JSchException | IOException e) {
                        LoggerHelper.fmtError(getClass(), e, "webssh连接异常");
                        disconnectTerminal(client);
                    }
                }
            });
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "Json转换异常");
        }
    }

    @OnEvent("terminal")
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
                        LoggerHelper.fmtError(getClass(), e, "webssh连接异常");
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

    private void connectTerminal(TerminalConnect terminalConnect, WebTerminalUser webTerminalUser, SocketIOClient client) throws JSchException, IOException {
        Session session = terminalConnect.getJSch()
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
        Channel channel = session.openChannel("shell");
        channel.connect(3 * 1000);
        terminalConnect.setChannel(channel);
        // 读取终端返回的信息流
        InputStream inputStream = channel.getInputStream();
        try {
            byte[] buffer = new byte[1024];
            int i = 0;
            // 如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                client.sendEvent("terminal", new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "读取终端命令错误");
        } finally {
            // 断开连接后关闭会话
            session.disconnect();
            channel.disconnect();
            if (inputStream != null) {
                inputStream.close();
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
            //map中移除
            terminalMap.remove(sessionId);
        }
    }
}
