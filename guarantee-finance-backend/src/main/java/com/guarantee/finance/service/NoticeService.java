package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.NoticeDTO;
import com.guarantee.finance.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {

    List<Notice> getNoticeList();

    Notice getNoticeDetail(Long id);

    IPage<Notice> queryPage(String keyword, String noticeType, Integer status, IPage<Notice> page);

    Long createNotice(NoticeDTO dto);

    void updateNotice(NoticeDTO dto);

    void deleteNotice(Long id);

    void publishNotice(Long id);

    void unpublishNotice(Long id);
}
