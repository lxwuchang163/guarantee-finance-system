package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.PaymentDTO;
import com.guarantee.finance.service.FinPaymentService;
import com.guarantee.finance.vo.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "付款单管理")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private FinPaymentService finPaymentService;

    @Operation(summary = "付款单分页查询")
    @GetMapping("/page")
    public R<PageResult<PaymentVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<PaymentVO> page = finPaymentService.queryPage(keyword, businessType, customerCode, status, startDate, endDate, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "付款单详情")
    @GetMapping("/{id}")
    public R<PaymentVO> detail(@PathVariable Long id) {
        return R.ok(finPaymentService.getDetail(id));
    }

    @Operation(summary = "新增付款单")
    @PostMapping
    public R<Long> create(@RequestBody PaymentDTO dto) {
        return R.ok("创建成功", finPaymentService.create(dto));
    }

    @Operation(summary = "修改付款单")
    @PutMapping
    public R<Void> update(@RequestBody PaymentDTO dto) {
        finPaymentService.update(dto);
        return R.ok("修改成功", null);
    }

    @Operation(summary = "删除付款单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        finPaymentService.delete(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "提交审核")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        finPaymentService.submit(id);
        return R.ok("已提交审核", null);
    }

    @Operation(summary = "审核通过/驳回")
    @PutMapping("/{id}/audit")
    public R<Void> audit(@PathVariable Long id, @RequestParam boolean pass) {
        finPaymentService.audit(id, pass);
        return R.ok(pass ? "审核通过" : "已驳回", null);
    }

    @Operation(summary = "执行付款")
    @PutMapping("/{id}/pay")
    public R<Void> pay(@PathVariable Long id) {
        finPaymentService.pay(id);
        return R.ok("付款指令已发送", null);
    }

    @Operation(summary = "记账")
    @PutMapping("/{id}/post")
    public R<Void> post(@PathVariable Long id) {
        finPaymentService.post(id);
        return R.ok("记账成功", null);
    }

    @Operation(summary = "冲销/作废")
    @PutMapping("/{id}/reverse")
    public R<Void> reverse(@PathVariable Long id) {
        finPaymentService.reverse(id);
        return R.ok("冲销成功", null);
    }
}
