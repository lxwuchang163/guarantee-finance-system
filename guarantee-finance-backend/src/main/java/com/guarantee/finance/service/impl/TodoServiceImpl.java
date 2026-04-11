package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.TodoItem;
import com.guarantee.finance.mapper.TodoItemMapper;
import com.guarantee.finance.service.TodoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoServiceImpl extends ServiceImpl<TodoItemMapper, TodoItem> implements TodoService {

    @Override
    public List<TodoItem> getTodoList() {
        // 模拟数据，实际项目中应该从数据库查询
        List<TodoItem> todoList = new ArrayList<>();
        
        TodoItem todo1 = new TodoItem();
        todo1.setId(1L);
        todo1.setTitle("收款单 SK20240315001 待审核");
        todo1.setType("收款单");
        todo1.setPriority("high");
        todo1.setStatus("pending");
        todo1.setDescription("需要审核收款单 SK20240315001");
        todo1.setCreateTime(LocalDateTime.parse("2024-03-15T09:30:00"));
        todoList.add(todo1);
        
        TodoItem todo2 = new TodoItem();
        todo2.setId(2L);
        todo2.setTitle("付款单 FK20240314002 待审批");
        todo2.setType("付款单");
        todo2.setPriority("medium");
        todo2.setStatus("pending");
        todo2.setDescription("需要审批付款单 FK20240314002");
        todo2.setCreateTime(LocalDateTime.parse("2024-03-14T15:20:00"));
        todoList.add(todo2);
        
        TodoItem todo3 = new TodoItem();
        todo3.setId(3L);
        todo3.setTitle("银行流水导入完成");
        todo3.setType("对账");
        todo3.setPriority("low");
        todo3.setStatus("done");
        todo3.setDescription("银行流水已成功导入系统");
        todo3.setCreateTime(LocalDateTime.parse("2024-03-15T10:00:00"));
        todoList.add(todo3);
        
        TodoItem todo4 = new TodoItem();
        todo4.setId(4L);
        todo4.setTitle("客户信息同步任务执行完成");
        todo4.setType("同步");
        todo4.setPriority("low");
        todo4.setStatus("done");
        todo4.setDescription("客户信息同步任务已执行完成");
        todo4.setCreateTime(LocalDateTime.parse("2024-03-15T08:00:00"));
        todoList.add(todo4);
        
        return todoList;
    }

    @Override
    public boolean processTodo(Long id) {
        // 模拟处理待办事项，实际项目中应该更新数据库
        TodoItem todo = getById(id);
        if (todo != null) {
            todo.setStatus("done");
            todo.setUpdateTime(LocalDateTime.now());
            return updateById(todo);
        }
        return false;
    }

    @Override
    public TodoItem getTodoDetail(Long id) {
        // 模拟获取待办事项详情，实际项目中应该从数据库查询
        return getById(id);
    }
}
