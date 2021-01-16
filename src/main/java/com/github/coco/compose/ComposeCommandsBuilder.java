package com.github.coco.compose;

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

        public Builder start() {
            command.add("start");
            return this;
        }

        public Builder stop() {
            command.add("stop");
            return this;
        }

        public Builder restart() {
            command.add("restart");
            return this;
        }

        public Builder up() {
            command.add("up");
            return this;
        }

        public Builder down() {
            command.add("down");
            return this;
        }

        public Builder rm() {
            command.add("rm");
            return this;
        }

        public Builder run() {
            command.add("run");
            return this;
        }

        public Builder exec() {
            command.add("exec");
            return this;
        }

        public Builder ps() {
            command.add("ps");
            return this;
        }

        public Builder config() {
            command.add("config");
            return this;
        }

        public Builder config(String config) {
            command.add(String.format("config %s", config));
            return this;
        }

        public Builder logs(String serviceName) {
            command.add(String.format("logs %s", serviceName));
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
