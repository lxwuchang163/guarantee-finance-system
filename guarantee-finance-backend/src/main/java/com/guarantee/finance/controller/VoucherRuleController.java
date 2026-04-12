package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.VoucherRuleDTO;
import com.guarantee.finance.entity.AccVoucherRule;
import com.guarantee.finance.service.VoucherRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "凭证规则管理")
@RestController
@RequestMapping("/accounting/voucher-rule")
public class VoucherRuleController {

    @Autowired
    private VoucherRuleService voucherRuleService;

    @Operation(summary = "规则分页查询")
    @GetMapping("/page")
    public R<PageResult<AccVoucherRule>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<AccVoucherRule> page = voucherRuleService.queryPage(keyword, businessType, status, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "规则详情")
    @GetMapping("/{id}")
    public R<VoucherRuleDTO> detail(@PathVariable Long id) {
        return R.ok(voucherRuleService.getRuleDetail(id));
    }

    @Operation(summary = "创建规则")
    @PostMapping
    public R<Long> create(@RequestBody VoucherRuleDTO dto) {
        return R.ok("创建成功", voucherRuleService.createRule(dto));
    }

    @Operation(summary = "修改规则")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody VoucherRuleDTO dto) {
        dto.setId(id);
        voucherRuleService.updateRule(dto);
        return R.ok("修改成功", null);
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        voucherRuleService.deleteRule(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "启用规则")
    @PutMapping("/{id}/enable")
    public R<Void> enable(@PathVariable Long id) {
        voucherRuleService.enableRule(id);
        return R.ok("启用成功", null);
    }

    @Operation(summary = "禁用规则")
    @PutMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        voucherRuleService.disableRule(id);
        return R.ok("禁用成功", null);
    }

    @Operation(summary = "获取业务类型列表")
    @GetMapping("/business-types")
    public R<List<Map<String, Object>>> businessTypes() {
        return R.ok(voucherRuleService.getBusinessTypes());
    }
}
