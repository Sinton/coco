package com.github.coco.service;

import com.github.coco.entity.Stack;

import java.util.List;

/**
 * @author Yan
 */
public interface StackService {
    int createStack(Stack stack);

    int removeStack(Stack stack);

    int modifyStack(Stack stack);

    Stack getStack(Stack stack);

    List<Stack> getStacks(Stack stack, int pageNo, int pageSize);

    int getStackTotal(Stack stack);
}
