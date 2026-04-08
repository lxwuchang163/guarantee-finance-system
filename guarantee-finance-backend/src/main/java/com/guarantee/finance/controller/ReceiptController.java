package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.ReceiptDTO;
import com.guarantee.finance.service.FinReceiptService;
import com.guarantee.finance.vo.ReceiptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "收款单管理")
@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    @Autowired
    private FinReceiptService finReceiptService;

    @Operation(summary = "收款单分页查询")
    @GetMapping("/page")
    public R<PageResult<ReceiptVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<ReceiptVO> page = finReceiptService.queryPage(keyword, businessType, customerCode, status, startDate, endDate, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "收款单详情")
    @GetMapping("/{id}")
    public R<ReceiptVO> detail(@PathVariable Long id) {
        return R.ok(finReceiptService.getDetail(id));
    }

    @Operation(summary = "新增收款单")
    @PostMapping
    public R<Long> create(@RequestBody ReceiptDTO dto) {
        return R.ok(finReceiptService.create(dto), "创建成功");
    }

    @Operation(summary = "修改收款单")
    @PutMapping
    public R<Void> update(@RequestBody ReceiptDTO dto) {
        finReceiptService.update(dto);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除收款单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        finReceiptService.delete(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "提交审核")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        finReceiptService.submit(id);
        return R.ok("已提交审核");
    }

    @Operation(summary = "审核通过/驳回")
    @PutMapping("/{id}/audit")
    public R<Void> audit(@PathVariable Long id, @RequestParam boolean pass) {
        finReceiptService.audit(id, pass);
        return R.ok(pass ? "审核通过" : "已驳回");
    }

    @Operation(summary = "记账")
    @PutMapping("/{id}/post")
    public R<Void> post(@PathVariable Long id) {
        finReceiptService.post(id);
        return R.ok("记账成功");
    }

    @Operation(summary = "冲销/作废")
    @PutMapping("/{id}/reverse")
    public R<Void> reverse(@PathVariable Long id) {
        finReceiptService.reverse(id);
        return R.ok("冲销成功");
    }
}
