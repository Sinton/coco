package com.github.coco.factory;

/**
 * @author Yan
 */
public interface Connector {
    /**
     * 获取资源连接地址
     *
     * @return
     */
    public String getHost();

    /**
     * 获取资源连接端口
     *
     * @return
     */
    public String getPort();

    /**
     * 获取资源连接协议
     *
     * @return
     */
    public String getProtocol();

    /**
     * 获取资源连接配置
     *
     * @return
     */
    public String getConfig();
}
