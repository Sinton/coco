package com.github.coco.utils.docker;

import com.github.coco.compose.ComposeCommandsBuilder;
import com.github.coco.compose.ComposeConfig;
import com.github.coco.compose.ComposeOptionsBuilder;
import com.github.coco.constant.DockerConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.utils.ThreadPoolHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author Yan
 */
@Slf4j
public class DockerComposeHelper {
    /**
     * docker-compose二进制执行程序
     */
    private static final String DEFAULT_COMPOSE_BIN_FILENAME    = "docker-compose";

    /**
     * docker-compose.yml文件存储目录
     */
    private static final String DEFAULT_COMPOSE_STORE_DIRECTORY = "compose";

    /**
     * compose工程文件存储路径
     */
    private static final String DEFAULT_COMPOSE_PATH            = String.format("%s%s%s",
                                                                                GlobalConstant.DEFAULT_STORAGE_PATH,
                                                                                IOUtils.DIR_SEPARATOR,
                                                                                DEFAULT_COMPOSE_STORE_DIRECTORY);

    public enum OperateEnum {
        /**
         * 启动
         */
        START(1),
        /**
         * 停止
         */
        STOP(2),
        /**
         * 重启
         */
        RESTART(3),
        /**
         * 部署
         */
        UP(4),
        /**
         * 卸载
         */
        DOWN(5),
        /**
         * 删除
         */
        RM(6),
        /**
         * 执行命令
         */
        RUN(7),
        /**
         * 进入容器终端
         */
        EXEC(8),
        /**
         * 查看
         */
        PS(9),
        /**
         * 校验
         */
        CONFIG(10),
        /**
         * 日志
         */
        LOGS(11);
        int code;

        OperateEnum(int code) {
            this.code = code;
        }
    }

    public enum ProcessResponseStreamEnum {
        /**
         * 全部信息
         */
        ALL(1),
        /**
         * 正常信息
         */
        NORMAL(2),
        /**
         * 错误异常信息
         */
        ERROR(3);
        int code;

        ProcessResponseStreamEnum(int code) {
            this.code = code;
        }
    }

    /**
     * 操作docker-compose
     *
     * @param operate
     * @param composeConfig
     */
    public static void composeOperate(OperateEnum operate, ComposeConfig composeConfig) {
        ProcessResponseStreamEnum currProcessResponseStream = ProcessResponseStreamEnum.ALL;

        // 构建docker-compose Options参数
        ComposeOptionsBuilder.Builder optionsBuilder = ComposeOptionsBuilder.builder();
        if (StringUtils.isNotBlank(composeConfig.getRemote())) {
            optionsBuilder.remote(composeConfig.getRemote());
        }
        if (StringUtils.isNotBlank(composeConfig.getProject())) {
            optionsBuilder.project(composeConfig.getProject());
        }
        if (composeConfig.getDebug() != null && composeConfig.getDebug()) {
            optionsBuilder.verbose();
        }
        optionsBuilder.composeFile(getComposeYamlFilePath(composeConfig.getProjectId())).build();

        // 构建docker-compose Commands参数
        ComposeCommandsBuilder.Builder commandsBuilder = ComposeCommandsBuilder.builder();
        switch (operate) {
            case START:
                commandsBuilder.start(composeConfig);
                break;
            case STOP:
                commandsBuilder.stop(composeConfig);
                break;
            case RESTART:
                commandsBuilder.restart(composeConfig);
                break;
            case UP:
                commandsBuilder.up(composeConfig);
                break;
            case DOWN:
                commandsBuilder.down(composeConfig);
                break;
            case RM:
                commandsBuilder.rm(composeConfig);
                break;
            case RUN:
                commandsBuilder.run(composeConfig);
                break;
            case EXEC:
                commandsBuilder.exec(composeConfig);
                break;
            case PS:
                commandsBuilder.ps(composeConfig);
                break;
            case CONFIG:
                currProcessResponseStream = ProcessResponseStreamEnum.ERROR;
                commandsBuilder.config(composeConfig);
                break;
            case LOGS:
                commandsBuilder.logs(composeConfig);
                break;
            default:
                break;
        }
        if (currProcessResponseStream == ProcessResponseStreamEnum.ERROR) {
            execute(String.format("%s %s %s",
                                  DEFAULT_COMPOSE_BIN_FILENAME,
                                  optionsBuilder.build(),
                                  commandsBuilder.build()),
                                  currProcessResponseStream);
        } else {
            execute(String.format("%s %s %s", DEFAULT_COMPOSE_BIN_FILENAME, optionsBuilder.build(), commandsBuilder.build()));
        }
    }

    /**
     * 校验docker-compose.yml内容格式
     *
     * @param composeConfig
     * @return
     */
    public static boolean validateComposeFile(ComposeConfig composeConfig) {
        DockerComposeHelper.composeOperate(DockerComposeHelper.OperateEnum.CONFIG, composeConfig);
        return true;
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
            log.info(String.format("execute [ %s ] shell command", cmd));
            process = new ProcessBuilder().command("sh", "-c", cmd).start();
            Process threadHandleProcess = process;
            if (processResponseStream == ProcessResponseStreamEnum.ALL) {
                InputStream[] inputStreams = {process.getInputStream(), process.getErrorStream()};
                for (InputStream stream : inputStreams) {
                    ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE)
                                    .execute(() -> composeProcessStreamHandler(threadHandleProcess, stream));
                }
                int status = process.waitFor();
                log.info("status is " + status);
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
                log.info("status is " + status);
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
        composeProcessStreamHandler(process, inputStream, log::info);
    }

    private static void composeProcessStreamHandler(Process process, InputStream inputStream, Consumer<String> consumer) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                                                                                      StandardCharsets.UTF_8))) {
            // 循环等待进程输出，判断进程存活则循环获取输出流数据
            while (process.isAlive()) {
                while (bufferedReader.ready()) {
                    // 自定义进程输出处理
                    String result = bufferedReader.readLine();
                    consumer.accept(result);
                }
            }
        } catch (IOException e) {
            log.error("处理进程输出异常", e);
        }
    }

    /**
     * 获取compose类型应用栈docker-compose.yml文件的存储路径
     *
     * @param projectId
     * @return
     */
    public static String getComposeYamlFilePath(Integer projectId) {
        String directory = String.format("%s%s%s", DEFAULT_COMPOSE_PATH, IOUtils.DIR_SEPARATOR, projectId);
        return String.format("%s%s%s", directory, IOUtils.DIR_SEPARATOR, DockerConstant.COMPOSE_STACK_FILENAME);
    }
}
