package com.github.coco.constant.dict;

/**
 * @author Yan
 */
public enum ContainerActionEnum {
    /**
     * 启动
     */
    START(1),
    /**
     * 重启
     */
    RESTART(2),
    /**
     * 停止
     */
    STOP(3),
    /**
     * 暂停
     */
    PAUSE(4),
    /**
     * 恢复
     */
    UPPAUSE(5),
    /**
     * 停杀
     */
    KILL(6);
    int action;

    ContainerActionEnum(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
