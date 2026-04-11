package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.CfcaSignDTO;
import com.guarantee.finance.service.CfcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "CFCA支付认证")
@RestController
@RequestMapping("/cfca")
public class CfcaController {

    @Autowired
    private CfcaService cfcaService;

    @Operation(summary = "证书列表")
    @GetMapping("/certificates")
    public R<List<Map<String, Object>>> listCertificates() {
        return R.ok(cfcaService.listCertificates());
    }

    @Operation(summary = "支付签名")
    @PostMapping("/sign")
    public R<Boolean> signPayment(@RequestBody CfcaSignDTO dto) {
        return R.ok(dto.getSignLevel() >= 2 ? "多人签名完成" : "签名完成", cfcaService.signPaymentData(dto));
    }

    @Operation(summary = "验签")
    @PostMapping("/verify")
    public R<Boolean> verifySignature(@RequestParam String data, @RequestParam String signature) {
        return R.ok(cfcaService.verifySignature(data, signature));
    }

    @Operation(summary = "证书有效期检查")
    @GetMapping("/expiry/check")
    public R<Map<String, Object>> checkExpiry() {
        return R.ok(cfcaService.checkCertificateExpiry());
    }

    @Operation(summary = "刷新到期提醒")
    @PostMapping("/expiry/refresh")
    public R<Void> refreshAlert() {
        cfcaService.refreshExpiryAlert();
        return R.ok("已刷新", null);
    }
}
