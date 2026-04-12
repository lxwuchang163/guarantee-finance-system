package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.TodoItem;

import java.util.List;

public interface TodoService extends IService<TodoItem> {

    List<TodoItem> getTodoList();

    boolean processTodo(Long id);

    TodoItem getTodoDetail(Long id);

    void completeTodoByBusiness(Long businessId, String businessType);
}
