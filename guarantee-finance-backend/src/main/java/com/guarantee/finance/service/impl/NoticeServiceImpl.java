package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.Notice;
import com.guarantee.finance.mapper.NoticeMapper;
import com.guarantee.finance.service.NoticeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public List<Notice> getNoticeList() {
        // 模拟数据，实际项目中应该从数据库查询
        List<Notice> noticeList = new ArrayList<>();
        
        Notice notice1 = new Notice();
        notice1.setId(1L);
        notice1.setTitle("系统升级维护通知");
        notice1.setContent("系统将于2024年3月18日凌晨2:00-4:00进行升级维护，期间系统将暂时不可用。");
        notice1.setStatus(1);
        notice1.setCreateTime(LocalDateTime.parse("2024-03-15T00:00:00"));
        noticeList.add(notice1);
        
        Notice notice2 = new Notice();
        notice2.setId(2L);
        notice2.setTitle("新功能上线：银企直连模块");
        notice2.setContent("银企直连模块已正式上线，支持实时查询账户余额和交易明细。");
        notice2.setStatus(1);
        notice2.setCreateTime(LocalDateTime.parse("2024-03-10T00:00:00"));
        noticeList.add(notice2);
        
        Notice notice3 = new Notice();
        notice3.setId(3L);
        notice3.setTitle("月度财务报表模板更新");
        notice3.setContent("2024年3月财务报表模板已更新，请使用最新模板进行报表编制。");
        notice3.setStatus(1);
        notice3.setCreateTime(LocalDateTime.parse("2024-03-05T00:00:00"));
        noticeList.add(notice3);
        
        return noticeList;
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        // 模拟获取公告详情，实际项目中应该从数据库查询
        return getById(id);
    }
}
