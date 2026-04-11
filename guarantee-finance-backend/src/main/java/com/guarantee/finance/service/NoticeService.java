package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {

    /**
     * 获取系统公告列表
     * @return 系统公告列表
     */
    List<Notice> getNoticeList();

    /**
     * 获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    Notice getNoticeDetail(Long id);
}
