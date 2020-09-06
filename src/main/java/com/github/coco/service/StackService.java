package com.github.coco.service;

import com.github.coco.entity.Stack;

import java.util.List;

/**
 * @author Yan
 */
public interface StackService {
    void createStack(Stack stack);
    void deleteStack(Stack stack);
    int deleteStack(String stackId);
    int updateStack(Stack stack);
    Stack getStack(Stack stack);
    Stack getStack(String stackId);
    List<Stack> getStacks();
}
