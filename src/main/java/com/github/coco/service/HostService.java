package com.github.coco.service;

import com.github.coco.entity.Host;

import java.util.List;

/**
 * @author Yan
 */
public interface HostService {
    void createHost(Host host);

    int removeHost(Host host);

    void modifyHost(Host host);

    Host getHostById(String id);

    Host getHostById(Host host);

    List<Host> getHosts();
}
