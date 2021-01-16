package com.github.coco.utils;

import com.github.coco.compose.ComposeConfig;
import com.github.coco.compose.ComposeCommandsBuilder;
import com.github.coco.compose.ComposeOptionsBuilder;
import com.github.coco.constant.DockerConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Yan
 */
@Slf4j
public class DockerComposeHelper {
    private static final String DEFAULT_BIN          = "docker-compose";
    private static final String DEFAULT_COMPOSE_PATH = "/data";

    public enum OperateEnum {
        /**
         * 启动
         */
        UP(1, ""),
        DOWN(2, ""),
        RUN(3, ""),
        EXEC(4, ""),
        PS(5, ""),
        CONFIG(6, "");
        int code;
        String name;

        OperateEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    public enum ProcessResponseStreamEnum {
        /**
         * 全部信息
         */
        ALL(1, "全部"),
        /**
         * 正常信息
         */
        NORMAL(2, "正常"),
        /**
         * 错误异常信息
         */
        ERROR(3, "错误异常");
        int code;
        String name;

        ProcessResponseStreamEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 操作docker-compose
     *
     * @param operate
     * @param composeConfig
     */
    public static void composeOperate(OperateEnum operate, ComposeConfig composeConfig) {
        String directory = String.format("%s/%s", DEFAULT_COMPOSE_PATH, composeConfig.getProjectId());
        String filePath = String.format("%s/%s", directory, DockerConstant.COMPOSE_STACK_FILENAME);
        ComposeCommandsBuilder.Builder commandsBuilder = ComposeCommandsBuilder.builder();
        ComposeOptionsBuilder.Builder optionsBuilder = ComposeOptionsBuilder.builder();
        if (StringUtils.isNotBlank(composeConfig.getRemote())) {
            optionsBuilder.remote(composeConfig.getRemote());
        }
        if (StringUtils.isNotBlank(composeConfig.getProject())) {
            optionsBuilder.project(composeConfig.getProject());
        }
        optionsBuilder.composeFile(filePath).build();
        switch (operate) {
            case UP:
                commandsBuilder.up();
                break;
            case DOWN:
                commandsBuilder.down();
                break;
            case RUN:
                commandsBuilder.run();
                break;
            case EXEC:
                commandsBuilder.exec();
                break;
            case PS:
                commandsBuilder.ps();
                break;
            case CONFIG:
                commandsBuilder.config();
                break;
            default:
                break;
        }
        execute(String.format("%s %s %s", DEFAULT_BIN, optionsBuilder.build(), commandsBuilder.build()));
    }

    /**
     * 执行cmd命令
     *
     * @param cmd
     */
    private static void execute(String cmd) {
        execute(cmd, ProcessResponseStreamEnum.ALL);
    }

    /**
     * 执行cmd命令，并输出指定输出源
     *
     * @param cmd
     * @param processResponseStream
     */
    private static void execute(String cmd, ProcessResponseStreamEnum processResponseStream) {
        Process process = null;
        try {
            process = new ProcessBuilder().command("sh", "-c", cmd).start();
            Process threadHandleProcess = process;
            if (processResponseStream == ProcessResponseStreamEnum.ALL) {
                InputStream[] inputStreams = {process.getInputStream(), process.getErrorStream()};
                for (InputStream stream : inputStreams) {
                    ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE)
                                    .execute(() -> composeProcessStreamHandler(threadHandleProcess, stream));
                }
                int status = process.waitFor();
                System.out.println("status is " + status);
            } else {
                switch (processResponseStream) {
                    case NORMAL:
                        ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE)
                                        .execute(() -> composeProcessStreamHandler(threadHandleProcess,
                                                                                   threadHandleProcess.getInputStream()));
                        break;
                    case ERROR:
                        ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE)
                                        .execute(() -> composeProcessStreamHandler(threadHandleProcess,
                                                                                   threadHandleProcess.getErrorStream()));
                        break;
                    default:
                        break;
                }
                int status = process.waitFor();
                System.out.println("status is " + status);
            }
        } catch (InterruptedException | IOException e) {
            log.error("执行命令出现异常", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 进程输出流处理
     *
     * @param process
     * @param inputStream
     */
    private static void composeProcessStreamHandler(Process process, InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                                                                                      StandardCharsets.UTF_8))) {
            // 循环等待进程输出，判断进程存活则循环获取输出流数据
            while (process.isAlive()) {
                while (bufferedReader.ready()) {
                    // TODO 自定义进程输出处理
                    String result = bufferedReader.readLine();
                    System.out.println(result);
                }
            }
        } catch (IOException e) {
            log.error("处理进程输出异常", e);
        }
    }
}
