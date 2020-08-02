package com.github.coco.service;

import com.github.coco.entity.Endpoint;

import java.util.List;

/**
 * @author Yan
 */
public interface EndpointService {
    void createEndpoint(Endpoint endpoint);

    void removeEndpoint(String id);

    int modifyEndpoint(Endpoint endpoint);

    Endpoint getEndpoint(Endpoint endpoint);

    Endpoint getEndpointById(String id);

    List<Endpoint> getEndpoints();
}
