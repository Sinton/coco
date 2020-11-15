package com.github.coco.service;

import com.github.coco.entity.Stack;

import java.util.List;

/**
 * @author Yan
 */
public interface StackService {
    void createStack(Stack stack);

    int removeStack(Stack stack);

    int removeStack(String stackId);

    int modifyStack(Stack stack);

    Stack getStack(Stack stack);

    Stack getStack(String stackId);

    List<Stack> getStacks(String endpoint);
}
