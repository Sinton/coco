package com.github.coco.compose;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Yan
 */
public class ComposeCommandsBuilder {
    private ComposeCommandsBuilder() {
    }

    public static final class Builder {
        private final List<String> command;

        public Builder() {
            command = new LinkedList<>();
        }

        /**
         * 创建并启动所有服务的容器
         *
         * @param composeConfig
         * @return
         */
        public Builder up(ComposeConfig composeConfig) {
            if (composeConfig.getDaemon() != null && composeConfig.getDaemon()) {
                command.add("up -d");
            } else {
                command.add("up");
            }
            return this;
        }

        /**
         * 停止并并删除所有服务的容器、网络、数据卷
         * @param composeConfig
         * @return
         */
        public Builder down(ComposeConfig composeConfig) {
            if (composeConfig.getPruneVolume() != null && composeConfig.getPruneVolume()) {
                command.add("down -v");
            } else {
                command.add("down");
            }
            return this;
        }

        /**
         * 启动服务容器
         *
         * @param composeConfig
         * @return
         */
        public Builder start(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                command.add(String.format("start %s", composeConfig.getServiceName()));
            } else {
                command.add("start");
            }
            return this;
        }

        /**
         * 停止服务容器
         *
         * @param composeConfig
         * @return
         */
        public Builder stop(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                command.add(String.format("stop %s", composeConfig.getServiceName()));
            } else {
                command.add("stop");
            }
            return this;
        }

        /**
         * 重启服务容器
         *
         * @param composeConfig
         * @return
         */
        public Builder restart(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                command.add(String.format("restart %s", composeConfig.getServiceName()));
            } else {
                command.add("restart");
            }
            return this;
        }

        /**
         * 删除服务（停止状态）容器
         *
         * @param composeConfig
         * @return
         */
        public Builder rm(ComposeConfig composeConfig) {
            List<String> arguments = new ArrayList<>();
            arguments.add("rm");
            arguments.add("-s");
            arguments.add("-f");
            if (composeConfig.getPruneVolume() != null && composeConfig.getPruneVolume()) {
                arguments.add("-v");
            }
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                arguments.add(composeConfig.getServiceName());
            }
            command.add(String.join(" ", arguments));
            return this;
        }

        /**
         * 在指定服务容器上执行一个命令
         *
         * @param composeConfig
         * @return
         */
        public Builder run(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceName()) && StringUtils.isNotBlank(composeConfig.getCommand())) {
                command.add(String.format("run %s %s", composeConfig.getServiceName(), composeConfig.getCommand()));
            }
            return this;
        }

        /**
         * 进入服务容器
         *
         * @param composeConfig
         * @return
         */
        public Builder exec(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceIndex())) {
                command.add(String.format("exec %s %s %s",
                                          composeConfig.getServiceIndex(),
                                          composeConfig.getServiceName(),
                                          composeConfig.getCommand()));
            } else {
                command.add(String.format("exec %s %s", composeConfig.getServiceName(), composeConfig.getCommand()));
            }
            return this;
        }

        /**
         * 列出工程中所有服务的容器
         *
         * @param composeConfig
         * @return
         */
        public Builder ps(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                command.add(String.format("ps %s", composeConfig.getServiceName()));
            } else {
                command.add("ps");
            }
            return this;
        }

        /**
         * 验证docker-compose.yml文件
         *
         * @param composeConfig
         * @return
         */
        public Builder config(ComposeConfig composeConfig) {
            if (StringUtils.isNotBlank(composeConfig.getModuleName())) {
                command.add(String.format("config -q --%s", composeConfig.getModuleName()));
            } else {
                command.add("config -q");
            }
            return this;
        }

        /**
         * 查看服务容器的输出日志
         *
         * @param composeConfig
         * @return
         */
        public Builder logs(ComposeConfig composeConfig) {
            List<String> arguments = new LinkedList<>();
            arguments.add("logs");
            if (composeConfig.getFollowLogs() != null && composeConfig.getFollowLogs()) {
                arguments.add("-f");
            }
            if (StringUtils.isNotBlank(composeConfig.getServiceName())) {
                arguments.add(composeConfig.getServiceName());
            }
            command.add(String.join(" ", arguments));
            return this;
        }

        public String build() {
            return String.join(" ", command);
        }
    }

    public static ComposeCommandsBuilder.Builder builder() {
        return new Builder();
    }
}
