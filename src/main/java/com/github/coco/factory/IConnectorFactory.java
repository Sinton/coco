package com.github.coco.factory;

/**
 * @author Yan
 */
public interface IConnectorFactory {
    /**
     * 获取连接器
     *
     * @param name
     * @return
     */
    public Connector getConnector(String name);
}
