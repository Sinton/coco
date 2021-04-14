package com.github.coco.service.impl;

import com.github.coco.mapper.EndpointMapper;
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
    private EndpointMapper endpointMapper;

    @Override
    public void createEndpoint(Endpoint endpoint) {
        endpointMapper.insertEndpoint(endpoint);
    }

    @Override
    public void removeEndpoint(Integer id) {
        endpointMapper.deleteEndpoint(id);
    }

    @Override
    public int modifyEndpoint(Endpoint endpoint) {
        return endpointMapper.updateEndpoint(endpoint);
    }

    @Override
    public Endpoint getEndpoint(Endpoint endpoint) {
        return endpointMapper.selectEndpoint(endpoint);
    }

    @Override
    public List<Endpoint> getEndpoints(int pageNo, int pageSize) {
        return endpointMapper.selectEndpoints((pageNo - 1) * pageSize, pageSize);
    }

    @Override
    public int getEndpointTotal() {
        return endpointMapper.selectEndpointTotal();
    }
}
