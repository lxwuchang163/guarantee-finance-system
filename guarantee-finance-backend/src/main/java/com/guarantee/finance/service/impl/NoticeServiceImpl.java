package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.Notice;
import com.guarantee.finance.mapper.NoticeMapper;
import com.guarantee.finance.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public List<Notice> getNoticeList() {
        return list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getCreateTime)
                .last("LIMIT 10"));
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        return getById(id);
    }
}
