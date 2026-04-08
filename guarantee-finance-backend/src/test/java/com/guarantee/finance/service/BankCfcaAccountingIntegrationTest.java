package com.guarantee.finance.service;

import com.guarantee.finance.GuaranteeFinanceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GuaranteeFinanceApplication.class)
@ActiveProfiles("test")
@Transactional
public class BankCfcaAccountingIntegrationTest {

    @Autowired
    private BankReconciliationService reconciliationService;

    @Autowired
    private BankDirectConnectService bankDirectConnectService;

    @Autowired
    private CfcaService cfcaService;

    @Autowired
    private AccountingPlatformService accountingPlatformService;

    @Test
    @DisplayName("银行对账: 自动对账流程")
    public void testAutoReconciliation() {
        String accountNo = "6222021234567890123";
        String reconcileDate = "2024-01-15";
        assertDoesNotThrow(() -> reconciliationService.autoReconcile(accountNo, reconcileDate));
    }

    @Test
    @DisplayName("银企直连: 余额查询")
    public void testBankBalanceQuery() {
        var result = bankDirectConnectService.queryBalance("6222021234567890123");
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("银企直连: 批量余额查询")
    public void testBatchBalanceQuery() {
        var results = bankDirectConnectService.batchQueryBalances();
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("CFCA证书管理: 列表查询和到期检查")
    public void testCfcaCertificateManagement() {
        var certs = cfcaService.listCertificates();
        assertThat(certs).isNotNull();

        cfcaService.checkExpiry();

        cfcaService.refreshCertificates();
    }

    @Test
    @DisplayName("CFCA签名: 分级签名测试 - 单签(<=5万)")
    public void testCfcaSingleSign() {
        com.guarantee.finance.dto.CfcaSignDTO dto = new com.guarantee.finance.dto.CfcaSignDTO();
        dto.setPaymentId(1L);
        dto.setPaymentNo("FK202401150001");
        dto.setAmount(new BigDecimal("30000"));
        assertDoesNotThrow(() -> cfcaService.signPaymentData(dto));
    }

    @Test
    @DisplayName("CFCA签名: 分级签名测试 - 双签(5万~50万)")
    public void testCfcaDoubleSign() {
        com.guarantee.finance.dto.CfcaSignDTO dto = new com.guarantee.finance.dto.CfcaSignDTO();
        dto.setPaymentId(2L);
        dto.setPaymentNo("FK202401150002");
        dto.setAmount(new BigDecimal("100000"));
        assertDoesNotThrow(() -> cfcaService.signPaymentData(dto));
    }

    @Test
    @DisplayName("CFCA签名: 分级签名测试 - 三签+审批(>50万)")
    public void testCfcaTripleSign() {
        com.guarantee.finance.dto.CfcaSignDTO dto = new com.guarantee.finance.dto.CfcaSignDTO();
        dto.setPaymentId(3L);
        dto.setPaymentNo("FK202401150003");
        dto.setAmount(new BigDecimal("600000"));
        assertDoesNotThrow(() -> cfcaService.signPaymentData(dto));
    }

    @Test
    @DisplayName("会计平台: 凭证生成与审核冲销")
    public void testVoucherLifecycle() {
        com.guarantee.finance.dto.VoucherGenerateDTO genDto = new com.guarantee.finance.dto.VoucherGenerateDTO();
        genDto.setSourceBillId(1L);
        genDto.setSourceBillType("RECEIPT");
        genDto.setSourceBillNo("SK202401150001");
        genDto.setVoucherType("1");
        genDto.setCustomerCode("CUST001");
        genDto.setCustomerName("测试客户A");
        genDto.setAmount(new BigDecimal("50000.00"));

        Long voucherId = accountingPlatformService.generateVoucher(genDto);
        assertNotNull(voucherId);

        accountingPlatformService.auditVoucher(voucherId);

        accountingPlatformService.reverseVoucher(voucherId);
    }
}
