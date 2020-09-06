package com.github.coco.service.impl;

import com.github.coco.dao.StackDAO;
import com.github.coco.entity.Stack;
import com.github.coco.service.StackService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Yan
 */
public class StackServiceImpl implements StackService {
    @Resource
    private StackDAO stackDAO;

    @Override
    public void createStack(Stack stack) {
    }

    @Override
    public void deleteStack(Stack stack) {

    }

    @Override
    public int deleteStack(String stackId) {
        return 0;
    }

    @Override
    public int updateStack(Stack stack) {
        return 0;
    }

    @Override
    public Stack getStack(Stack stack) {
        return null;
    }

    @Override
    public Stack getStack(String stackId) {
        return null;
    }

    @Override
    public List<Stack> getStacks() {
        return null;
    }
}
