package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.service.AccVoucherService;
import com.guarantee.finance.vo.VoucherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final AccVoucherService accVoucherService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<VoucherVO>> queryVouchers(
            @RequestParam(required = false) String voucherNo,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String voucherDate,
            @RequestParam(required = false) Integer status,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<VoucherVO> result = accVoucherService.queryVouchers(voucherNo, period, voucherDate, status, pagination);
        return R.ok(result);
    }

    @GetMapping("/detail/{id}")
    public R<VoucherVO> getVoucherDetail(@PathVariable Long id) {
        VoucherVO voucher = accVoucherService.getVoucherDetail(id);
        return R.ok(voucher);
    }

    @PostMapping
    public R<Long> createVoucher(@RequestBody VoucherDTO dto) {
        Long id = accVoucherService.createVoucher(dto);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateVoucher(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        accVoucherService.updateVoucher(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteVoucher(@PathVariable Long id) {
        accVoucherService.deleteVoucher(id);
        return R.ok();
    }

    @PutMapping("/{id}/submit")
    public R<Void> submitVoucher(@PathVariable Long id) {
        accVoucherService.submitVoucher(id);
        return R.ok();
    }

    @PutMapping("/{id}/void")
    public R<Void> voidVoucher(@PathVariable Long id) {
        accVoucherService.voidVoucher(id);
        return R.ok();
    }

    @PutMapping("/{id}/restore")
    public R<Void> restoreVoucher(@PathVariable Long id) {
        accVoucherService.restoreVoucher(id);
        return R.ok();
    }

    @GetMapping("/period")
    public R<List<VoucherVO>> getVouchersByPeriod(@RequestParam String period) {
        List<VoucherVO> vouchers = accVoucherService.getVouchersByPeriod(period);
        return R.ok(vouchers);
    }

    @GetMapping("/check-no")
    public R<Boolean> checkVoucherNo(@RequestParam String voucherNo, @RequestParam(required = false) Long id) {
        boolean isUnique = accVoucherService.checkVoucherNoUnique(voucherNo, id);
        return R.ok(isUnique);
    }

    @PutMapping("/{id}/post")
    public R<Void> postVoucher(@PathVariable Long id) {
        accVoucherService.postVoucher(id);
        return R.ok();
    }

    @PutMapping("/{id}/unpost")
    public R<Void> unpostVoucher(@PathVariable Long id) {
        accVoucherService.unpostVoucher(id);
        return R.ok();
    }

    @PostMapping("/import")
    public R<Void> importVouchers(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        accVoucherService.importVouchers(file);
        return R.ok();
    }

    @GetMapping("/export/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportVouchersToExcel(@RequestParam String period) {
        byte[] data = accVoucherService.exportVouchersToExcel(period);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "vouchers_" + period + ".xlsx");
        return new org.springframework.http.ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/export/pdf/{id}")
    public org.springframework.http.ResponseEntity<byte[]> exportVoucherToPdf(@PathVariable Long id) {
        byte[] data = accVoucherService.exportVoucherToPdf(id);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "voucher_" + id + ".pdf");
        return new org.springframework.http.ResponseEntity<>(data, headers, org.springframework.http.HttpStatus.OK);
    }
}
