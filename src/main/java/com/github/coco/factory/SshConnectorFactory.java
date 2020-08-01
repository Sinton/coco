package com.github.coco.factory;

import com.github.coco.entity.Host;
import com.github.coco.utils.LoggerHelper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.Properties;

/**
 * @author Yan
 */
public class SshConnectorFactory extends BasePooledObjectFactory {
    private Host host;

    public SshConnectorFactory(Host host) {
        this.host = host;
    }

    @Override
    public Object create() {
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(host.getUser(),
                                      host.getIp(),
                                      host.getPort());
            Properties terminalConfig = new Properties();
            terminalConfig.put("StrictHostKeyChecking", "no");
            // 设置session
            session.setConfig(terminalConfig);
            session.setPassword(host.getPassword());
            session.connect(10 * 1000);
            // 设置连接信道
            channel = session.openChannel("shell");
            channel.connect(3 * 1000);
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "读取终端命令错误");
        } finally {
            // 关闭所有会话、连接信道
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        return null;
    }

    @Override
    public PooledObject wrap(Object obj) {
        return new DefaultPooledObject<>(obj);
    }
}
