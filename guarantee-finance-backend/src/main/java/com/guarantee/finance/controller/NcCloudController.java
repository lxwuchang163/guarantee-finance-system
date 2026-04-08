package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.NcCustomerSyncDTO;
import com.guarantee.finance.dto.NcVoucherSyncDTO;
import com.guarantee.finance.service.NcCloudService;
import com.guarantee.finance.vo.NcSyncLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用友NC Cloud对接")
@RestController
@RequestMapping("/api/nc")
public class NcCloudController {

    @Autowired
    private NcCloudService ncCloudService;

    @Operation(summary = "NC Cloud登录")
    @PostMapping("/login")
    public R<Boolean> login() {
        return R.ok(ncCloudService.login(), ncCloudService.isTokenValid() ? "登录成功" : "登录失败");
    }

    @Operation(summary = "NC Cloud登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        ncCloudService.logout();
        return R.ok("已登出");
    }

    @Operation(summary = "同步客户到NC Cloud")
    @PostMapping("/customer/sync")
    public R<Boolean> syncCustomer(@RequestBody NcCustomerSyncDTO dto) {
        return R.ok(ncCloudService.syncCustomer(dto), "客户同步完成");
    }

    @Operation(summary = "批量同步客户到NC Cloud")
    @PostMapping("/customer/batchSync")
    public R<Boolean> batchSyncCustomers(@RequestBody List<NcCustomerSyncDTO> dtoList) {
        return R.ok(ncCloudService.syncBatchCustomers(dtoList), "批量同步完成");
    }

    @Operation(summary = "同步凭证到NC Cloud")
    @PostMapping("/voucher/sync")
    public R<Boolean> syncVoucher(@RequestBody NcVoucherSyncDTO dto) {
        return R.ok(ncCloudService.syncVoucher(dto), "凭证同步完成");
    }

    @Operation(summary = "批量同步凭证到NC Cloud")
    @PostMapping("/voucher/batchSync")
    public R<Boolean> batchSyncVouchers(@RequestBody List<NcVoucherSyncDTO> dtoList) {
        return R.ok(ncCloudService.syncBatchVouchers(dtoList), "批量同步完成");
    }

    @Operation(summary = "查询同步日志")
    @GetMapping("/syncLog/list")
    public R<PageResult<NcSyncLogVO>> syncLogList(
            @RequestParam(required = false) String syncType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        IPage<NcSyncLogVO> page = ncCloudService.querySyncLog(syncType, status, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "同步日志详情")
    @GetMapping("/syncLog/{id}")
    public R<NcSyncLogVO> syncLogDetail(@PathVariable Long id) {
        return R.ok(ncCloudService.getSyncDetail(id));
    }

    @Operation(summary = "重试同步")
    @PutMapping("/syncLog/{id}/retry")
    public R<Boolean> retrySync(@PathVariable Long id) {
        return R.ok(ncCloudService.retrySync(id), "重试完成");
    }

    @Operation(summary = "检查客户差异")
    @GetMapping("/customer/checkDiff")
    public R<Map<String, Object>> checkDiff(@RequestParam String customerCode) {
        return R.ok(ncCloudService.checkCustomerDiff(customerCode));
    }
}
