package com.guarantee.finance.service;

import com.guarantee.finance.GuaranteeFinanceApplication;
import com.guarantee.finance.dto.ReceiptDTO;
import com.guarantee.finance.dto.PaymentDTO;
import com.guarantee.finance.vo.ReceiptVO;
import com.guarantee.finance.vo.PaymentVO;
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
public class ReceiptPaymentIntegrationTest {

    @Autowired
    private FinReceiptService receiptService;

    @Autowired
    private FinPaymentService paymentService;

    @Test
    @DisplayName("收款单完整生命周期: 创建->提交->审核->记账->冲销")
    public void testReceiptFullLifecycle() {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setBusinessType(1);
        dto.setCustomerCode("CUST001");
        dto.setCustomerName("测试客户A");
        dto.setAmount(new BigDecimal("50000.00"));
        dto.setPaymentDate("2024-01-15");
        dto.setCurrency("CNY");

        Long id = receiptService.createReceipt(dto);
        assertThat(id).isNotNull().isGreaterThan(0);

        receiptService.submitReceipt(id);
        ReceiptVO afterSubmit = receiptService.getReceiptDetail(id);
        assertThat(afterSubmit.getStatus()).isEqualTo(1);

        receiptService.auditReceipt(id, true);
        ReceiptVO afterAudit = receiptService.getReceiptDetail(id);
        assertThat(afterAudit.getStatus()).isEqualTo(2);

        receiptService.postReceipt(id);
        ReceiptVO afterPost = receiptService.getReceiptDetail(id);
        assertThat(afterPost.getStatus()).isEqualTo(3);

        receiptService.reverseReceipt(id);
        ReceiptVO afterReverse = receiptService.getReceiptDetail(id);
        assertThat(afterReverse.getStatus()).isEqualTo(5);
    }

    @Test
    @DisplayName("付款单完整生命周期: 创建->提交->审核->付款->记账")
    public void testPaymentFullLifecycle() {
        PaymentDTO dto = new PaymentDTO();
        dto.setBusinessType(3);
        dto.setCustomerCode("CUST002");
        dto.setCustomerName("测试客户B");
        dto.setAmount(new BigDecimal("100000.00"));
        dto.setPaymentDate("2024-01-16");
        dto.setPayeeName("收款方名称");
        dto.setPayeeAccountNo("6222021234567890123");
        dto.setPayeeBankName("中国工商银行");

        Long id = paymentService.createPayment(dto);
        assertThat(id).isNotNull();

        paymentService.submitPayment(id);
        PaymentVO afterSubmit = paymentService.getPaymentDetail(id);
        assertThat(afterSubmit.getStatus()).isEqualTo(1);

        paymentService.auditPayment(id, true);
        PaymentVO afterAudit = paymentService.getPaymentDetail(id);
        assertThat(afterAudit.getStatus()).isEqualTo(2);

        paymentService.executePay(id);
        PaymentVO afterPay = paymentService.getPaymentDetail(id);
        assertThat(afterPay.getStatus()).isEqualTo(3);
    }
}
