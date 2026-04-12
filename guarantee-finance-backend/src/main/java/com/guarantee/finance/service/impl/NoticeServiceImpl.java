package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.NoticeDTO;
import com.guarantee.finance.entity.Notice;
import com.guarantee.finance.mapper.NoticeMapper;
import com.guarantee.finance.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public List<Notice> getNoticeList() {
        return list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getTopFlag)
                .orderByDesc(Notice::getPublishTime)
                .orderByDesc(Notice::getCreateTime)
                .last("LIMIT 10"));
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        return getById(id);
    }

    @Override
    public IPage<Notice> queryPage(String keyword, String noticeType, Integer status, IPage<Notice> page) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Notice::getTitle, keyword).or().like(Notice::getContent, keyword));
        }
        if (noticeType != null && !noticeType.isEmpty()) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        wrapper.orderByDesc(Notice::getTopFlag).orderByDesc(Notice::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Long createNotice(NoticeDTO dto) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setStatus(0);
        notice.setDeleted(0);
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        if (notice.getTopFlag() == null) {
            notice.setTopFlag(0);
        }
        if (notice.getNoticeType() == null || notice.getNoticeType().isEmpty()) {
            notice.setNoticeType("system");
        }
        save(notice);
        return notice.getId();
    }

    @Override
    public void updateNotice(NoticeDTO dto) {
        Notice notice = getById(dto.getId());
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        if (dto.getTitle() != null) notice.setTitle(dto.getTitle());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (dto.getNoticeType() != null) notice.setNoticeType(dto.getNoticeType());
        if (dto.getTopFlag() != null) notice.setTopFlag(dto.getTopFlag());
        if (dto.getRemark() != null) notice.setRemark(dto.getRemark());
        notice.setUpdateTime(LocalDateTime.now());
        updateById(notice);
    }

    @Override
    public void deleteNotice(Long id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        notice.setDeleted(1);
        notice.setUpdateTime(LocalDateTime.now());
        updateById(notice);
    }

    @Override
    public void publishNotice(Long id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        notice.setStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        updateById(notice);
    }

    @Override
    public void unpublishNotice(Long id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        notice.setStatus(0);
        notice.setUpdateTime(LocalDateTime.now());
        updateById(notice);
    }
}
