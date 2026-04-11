package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guarantee.finance.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoItemMapper extends BaseMapper<TodoItem> {
}