package com.github.coco.service;

import com.github.coco.entity.Endpoint;

import java.util.List;

/**
 * @author Yan
 */
public interface EndpointService {
    void createEndpoint(Endpoint endpoint);

    void removeEndpoint(Integer id);

    int modifyEndpoint(Endpoint endpoint);

    Endpoint getEndpoint(Endpoint endpoint);

    List<Endpoint> getEndpoints(int pageNo, int pageSize);

    int getEndpointTotal();
}
