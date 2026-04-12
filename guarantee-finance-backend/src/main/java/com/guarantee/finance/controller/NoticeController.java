package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.NoticeDTO;
import com.guarantee.finance.entity.Notice;
import com.guarantee.finance.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统公告管理")
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "公告分页查询")
    @GetMapping("/page")
    public R<PageResult<Notice>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String noticeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<Notice> page = noticeService.queryPage(keyword, noticeType, status, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public R<Notice> detail(@PathVariable Long id) {
        return R.ok(noticeService.getNoticeDetail(id));
    }

    @Operation(summary = "新增公告")
    @PostMapping
    public R<Long> create(@RequestBody NoticeDTO dto) {
        return R.ok("创建成功", noticeService.createNotice(dto));
    }

    @Operation(summary = "修改公告")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody NoticeDTO dto) {
        dto.setId(id);
        noticeService.updateNotice(dto);
        return R.ok("修改成功", null);
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "发布公告")
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        noticeService.publishNotice(id);
        return R.ok("发布成功", null);
    }

    @Operation(summary = "取消发布")
    @PutMapping("/{id}/unpublish")
    public R<Void> unpublish(@PathVariable Long id) {
        noticeService.unpublishNotice(id);
        return R.ok("已取消发布", null);
    }
}
