package com.github.coco.service;

import com.github.coco.entity.Endpoint;

import java.util.List;

/**
 * @author Yan
 */
public interface EndpointService {
    /**
     * 查询用户
     *
     * @return
     */
    List<Endpoint> getEndpoints();
}
