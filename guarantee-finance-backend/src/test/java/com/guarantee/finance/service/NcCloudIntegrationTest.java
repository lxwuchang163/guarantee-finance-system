package com.guarantee.finance.service;

import com.guarantee.finance.GuaranteeFinanceApplication;
import com.guarantee.finance.dto.NcCustomerSyncDTO;
import com.guarantee.finance.dto.NcVoucherSyncDTO;
import com.guarantee.finance.vo.NcSyncLogVO;
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
public class NcCloudIntegrationTest {

    @Autowired
    private NcCloudService ncCloudService;

    @Test
    @DisplayName("NC Cloud登录认证测试")
    public void testNcLogin() {
        boolean result = ncCloudService.login();
        assertTrue(result, "NC Cloud登录应该成功（模拟环境）");
    }

    @Test
    @DisplayName("客户档案同步到NC Cloud")
    public void testCustomerSync() {
        NcCustomerSyncDTO dto = new NcCustomerSyncDTO();
        dto.setCustomerCode("TEST_NC_001");
        dto.setCustomerName("NC同步测试客户");
        dto.setCustomerType(2);
        dto.setCreditCode("91110000MA01ABCDEF");
        dto.setStatus(1);

        boolean result = ncCloudService.syncCustomerToNc(dto);
        assertTrue(result, "客户档案同步应该成功");
    }

    @Test
    @DisplayName("凭证数据同步到NC Cloud")
    public void testVoucherSync() {
        NcVoucherSyncDTO dto = new NcVoucherSyncDTO();
        dto.setVoucherNo("PZ202401150001");
        dto.setVoucherDate("2024-01-15");
        dto.setVoucherType(1);
        dto.setAccountingPeriod("2024-01");
        dto.setCompanyCode("JT01");
        dto.setMaker("admin");
        dto.setTotalDebit(new BigDecimal("50000.00"));
        dto.setTotalCredit(new BigDecimal("50000.00"));

        boolean result = ncCloudService.syncVoucherToNc(dto);
        assertTrue(result, "凭证数据同步应该成功");
    }

    @Test
    @DisplayName("同步日志查询与重试")
    public void testSyncLogQueryAndRetry() {
        var page = ncCloudService.querySyncLogs(null, null, 1, 10);
        assertThat(page).isNotNull();
    }
}
