package com.github.coco.service.impl;

import com.github.coco.dao.EndpointDAO;
import com.github.coco.entity.Endpoint;
import com.github.coco.service.EndpointService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Yan
 */
@Service
public class EndpointServiceImpl implements EndpointService {
    @Resource
    private EndpointDAO endpointDAO;

    @Override
    public void createEndpoint(Endpoint endpoint) {
        endpointDAO.insertEndpoint(endpoint);
    }

    @Override
    public void removeEndpoint(String id) {
        endpointDAO.deleteEndpoint(id);
    }

    @Override
    public int modifyEndpoint(Endpoint endpoint) {
        return endpointDAO.updateEndpoint(endpoint);
    }

    @Override
    public Endpoint getEndpoint(Endpoint endpoint) {
        return endpointDAO.selectEndpoint(endpoint);
    }

    @Override
    public Endpoint getEndpointById(String id) {
        return endpointDAO.selectEndpointById(id);
    }

    @Override
    public List<Endpoint> getEndpoints() {
        return endpointDAO.selectEndpoints();
    }
}
