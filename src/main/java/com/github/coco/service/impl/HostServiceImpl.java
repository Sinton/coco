package com.github.coco.service.impl;

import com.github.coco.dao.HostDAO;
import com.github.coco.entity.Host;
import com.github.coco.service.HostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Yan
 */
@Service
public class HostServiceImpl implements HostService {
    @Resource
    private HostDAO hostDAO;

    @Override
    public void createHost(Host host) {
        hostDAO.insertHost(host);
    }

    @Override
    public int removeHost(Host host) {
        return hostDAO.deleteHost(host);
    }

    @Override
    public void modifyHost(Host host) {
        hostDAO.updateHost(host);
    }

    @Override
    public Host getHostById(String uid) {
        return null;
    }

    @Override
    public Host getHostById(Host host) {
        return hostDAO.selectHost(host);
    }

    @Override
    public List<Host> getHosts(Host host) {
        return hostDAO.selectHosts();
    }
}
