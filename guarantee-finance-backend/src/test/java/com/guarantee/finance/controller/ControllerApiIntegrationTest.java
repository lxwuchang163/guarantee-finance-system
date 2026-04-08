package com.guarantee.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarantee.finance.GuaranteeFinanceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GuaranteeFinanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ControllerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("收款单API: 分页查询接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testReceiptPageApi() throws Exception {
        mockMvc.perform(get("/api/receipt/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("付款单API: 分页查询接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPaymentPageApi() throws Exception {
        mockMvc.perform(get("/api/payment/page")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("NC Cloud API: 登录认证接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testNcLoginApi() throws Exception {
        mockMvc.perform(post("/api/nc/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("银行对账API: 流水查询接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testReconciliationApi() throws Exception {
        mockMvc.perform(get("/api/reconciliation/transaction/list")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("银企直连API: 账户列表接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testBankAccountListApi() throws Exception {
        mockMvc.perform(get("/api/bank/account/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("CFCA API: 证书列表接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCfcaCertificateListApi() throws Exception {
        mockMvc.perform(get("/api/cfca/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("会计平台API: 凭证列表接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAccountingVoucherListApi() throws Exception {
        mockMvc.perform(get("/api/accounting/voucher/list")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("定时任务API: 调度仪表盘接口")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testScheduleDashboardApi() throws Exception {
        mockMvc.perform(get("/api/schedule/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalJobs").exists());
    }

    @Test
    @DisplayName("未登录访问应返回401或重定向到登录页")
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/receipt/page"))
                .andExpect(status().is3xxRedirectedOrUnauthorized());
    }
}
