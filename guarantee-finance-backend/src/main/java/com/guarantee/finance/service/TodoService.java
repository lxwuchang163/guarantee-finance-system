package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.TodoItem;

import java.util.List;

public interface TodoService extends IService<TodoItem> {

    /**
     * 获取待办事项列表
     * @return 待办事项列表
     */
    List<TodoItem> getTodoList();

    /**
     * 处理待办事项
     * @param id 待办事项ID
     * @return 处理结果
     */
    boolean processTodo(Long id);

    /**
     * 获取待办事项详情
     * @param id 待办事项ID
     * @return 待办事项详情
     */
    TodoItem getTodoDetail(Long id);
}
