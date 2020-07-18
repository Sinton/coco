package com.github.coco.service;

import com.github.coco.entity.Endpoint;
import com.github.coco.dao.EndpointDAO;
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
    public List<Endpoint> getEndpoints() {
        return endpointDAO.selectEndpoint();
    }
}
