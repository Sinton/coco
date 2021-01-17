package com.github.coco.compose;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yan
 */
public class ComposeOptionsBuilder {
    private ComposeOptionsBuilder() {
    }

    public static final class Builder {
        private final List<String> command;

        public Builder() {
            command = new LinkedList<>();
        }

        /**
         * 指定compose文件名称
         *
         * @param filename
         * @return
         */
        public Builder composeFile(String filename) {
            command.add(String.format("-f %s", filename));
            return this;
        }

        /**
         * 指定项目名称
         *
         * @param projectName
         * @return
         */
        public Builder project(String projectName) {
            command.add(String.format("-p %s", projectName));
            return this;
        }

        /**
         * 指定Compose项目工作目录
         *
         * @param projectDirectory
         * @return
         */
        public Builder directory(String projectDirectory) {
            command.add(String.format("--project-directory %s", projectDirectory));
            return this;
        }

        /**
         * Docker守护进程远程socket连接终端
         *
         * @param endpoint
         * @return
         */
        public Builder remote(String endpoint) {
            command.add(String.format("-H %s", endpoint));
            return this;
        }

        /**
         * 指定环境变量文件
         *
         * @param filename
         * @return
         */
        public Builder environment(String filename) {
            command.add(String.format("--env-file %s", filename));
            return this;
        }

        /**
         * 输出更多调试信息
         *
         * @return
         */
        public Builder verbose() {
            command.add("--verbose");
            return this;
        }

        public String build() {
            return String.join(" ", command);
        }
    }

    public static ComposeOptionsBuilder.Builder builder() {
        return new Builder();
    }
}
