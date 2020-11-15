package com.github.coco.service.impl;

import com.github.coco.dao.StackDAO;
import com.github.coco.entity.Stack;
import com.github.coco.service.StackService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Yan
 */
@Service
public class StackServiceImpl implements StackService {
    @Resource
    private StackDAO stackDAO;

    @Override
    public void createStack(Stack stack) {
        stackDAO.insertStack(stack);
    }

    @Override
    public int removeStack(Stack stack) {
        return stackDAO.deleteStack(stack);
    }

    @Override
    public int removeStack(String stackId) {
        return 0;
    }

    @Override
    public int modifyStack(Stack stack) {
        return 0;
    }

    @Override
    public Stack getStack(Stack stack) {
        return stackDAO.selectStack(stack);
    }

    @Override
    public Stack getStack(String stackId) {
        return null;
    }

    @Override
    public List<Stack> getStacks(String endpoint) {
        return stackDAO.selectStacks(endpoint);
    }
}
