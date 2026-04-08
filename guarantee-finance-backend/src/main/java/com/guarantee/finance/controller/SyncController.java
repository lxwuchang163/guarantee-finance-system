package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.service.BizCustomerService;
import com.guarantee.finance.vo.CustomerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "基础信息同步")
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    @Autowired
    private BizCustomerService bizCustomerService;

    @Operation(summary = "客户信息分页查询")
    @GetMapping("/customer/page")
    public R<PageResult<CustomerVO>> customerPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer customerType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<CustomerVO> page = bizCustomerService.queryPage(keyword, customerType, status, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "客户详情")
    @GetMapping("/customer/{id}")
    public R<CustomerVO> customerDetail(@PathVariable Long id) {
        return R.ok(bizCustomerService.getDetail(id));
    }

    @Operation(summary = "全量同步客户信息")
    @PostMapping("/customer/full")
    public R<Void> syncCustomerFull() {
        bizCustomerService.syncAll();
        return R.ok("全量同步任务已提交");
    }

    @Operation(summary = "增量同步客户信息")
    @PostMapping("/customer/incremental")
    public R<Void> syncCustomerIncremental(@RequestParam(defaultValue = "") String lastSyncTime) {
        bizCustomerService.syncIncremental(lastSyncTime);
        return R.ok("增量同步任务已提交");
    }

    @Operation(summary = "检查客户编码唯一性")
    @GetMapping("/customer/checkCode")
    public R<Boolean> checkCustomerCode(@RequestParam String code, @RequestParam(required = false) Long id) {
        return R.ok(bizCustomerService.checkCodeUnique(code, id));
    }
}
