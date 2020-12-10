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
    public void removeEndpoint(Integer id) {
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
    public List<Endpoint> getEndpoints(int pageNo, int pageSize) {
        return endpointDAO.selectEndpoints((pageNo - 1) * pageSize, pageSize);
    }

    @Override
    public int getEndpointTotal() {
        return endpointDAO.selectEndpointTotal();
    }
}
