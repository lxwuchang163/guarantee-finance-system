package com.guarantee.finance.service;

import com.guarantee.finance.dto.SubjectDTO;
import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.dto.VoucherDetailDTO;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.vo.SubjectVO;
import com.guarantee.finance.vo.VoucherVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class AccAccountingIntegrationTest {

    @Autowired
    private AccAccountSubjectService accAccountSubjectService;

    @Autowired
    private AccVoucherService accVoucherService;

    @Autowired
    private AccVoucherAuditService accVoucherAuditService;

    @Test
    void testSubjectManagement() {
        // 测试科目创建
        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setSubjectCode("1001");
        subjectDTO.setSubjectName("库存现金");
        subjectDTO.setSubjectLevel(1);
        subjectDTO.setParentCode("");
        subjectDTO.setSubjectType(1); // 资产
        subjectDTO.setBalanceDirection(1); // 借方
        subjectDTO.setSystemType("0");

        Long subjectId = accAccountSubjectService.createSubject(subjectDTO);
        assertNotNull(subjectId);

        // 测试科目查询
        SubjectVO subject = accAccountSubjectService.getSubjectDetail(subjectId);
        assertNotNull(subject);
        assertEquals("1001", subject.getSubjectCode());
        assertEquals("库存现金", subject.getSubjectName());

        // 测试科目状态变更
        accAccountSubjectService.changeStatus(subjectId, 0); // 封存
        subject = accAccountSubjectService.getSubjectDetail(subjectId);
        assertEquals(0, subject.getStatus());
    }

    @Test
    void testVoucherManagement() {
        // 先创建测试科目
        createTestSubjects();

        // 测试凭证创建
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setVoucherDate(LocalDate.now().toString());
        voucherDTO.setPeriod(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        voucherDTO.setSummary("测试凭证");
        voucherDTO.setVoucherType(1); // 记账凭证
        voucherDTO.setSourceType("0"); // 手工

        // 构建凭证分录
        List<VoucherDetailDTO> details = new ArrayList<>();

        // 借方：库存现金
        VoucherDetailDTO debitDetail = new VoucherDetailDTO();
        debitDetail.setLineNo(1);
        debitDetail.setSubjectCode("1001");
        debitDetail.setSubjectName("库存现金");
        debitDetail.setSummary("收到现金");
        debitDetail.setDebitAmount(new BigDecimal(1000));
        debitDetail.setCreditAmount(BigDecimal.ZERO);
        details.add(debitDetail);

        // 贷方：主营业务收入
        VoucherDetailDTO creditDetail = new VoucherDetailDTO();
        creditDetail.setLineNo(2);
        creditDetail.setSubjectCode("6001");
        creditDetail.setSubjectName("主营业务收入");
        creditDetail.setSummary("销售收入");
        creditDetail.setDebitAmount(BigDecimal.ZERO);
        creditDetail.setCreditAmount(new BigDecimal(1000));
        details.add(creditDetail);

        voucherDTO.setDetails(details);

        Long voucherId = accVoucherService.createVoucher(voucherDTO);
        assertNotNull(voucherId);

        // 测试凭证查询
        VoucherVO voucher = accVoucherService.getVoucherDetail(voucherId);
        assertNotNull(voucher);
        assertEquals("测试凭证", voucher.getSummary());
        assertEquals(2, voucher.getDetails().size());

        // 测试凭证提交
        accVoucherService.submitVoucher(voucherId);
        voucher = accVoucherService.getVoucherDetail(voucherId);
        assertEquals(1, voucher.getStatus()); // 已提交

        // 测试凭证审核
        // TODO: 实现审核测试

        // 测试凭证记账
        // TODO: 实现记账测试
    }

    private void createTestSubjects() {
        // 创建库存现金科目
        SubjectDTO cashSubject = new SubjectDTO();
        cashSubject.setSubjectCode("1001");
        cashSubject.setSubjectName("库存现金");
        cashSubject.setSubjectLevel(1);
        cashSubject.setParentCode("");
        cashSubject.setSubjectType(1); // 资产
        cashSubject.setBalanceDirection(1); // 借方
        cashSubject.setSystemType("0");
        accAccountSubjectService.createSubject(cashSubject);

        // 创建银行存款科目
        SubjectDTO bankSubject = new SubjectDTO();
        bankSubject.setSubjectCode("1002");
        bankSubject.setSubjectName("银行存款");
        bankSubject.setSubjectLevel(1);
        bankSubject.setParentCode("");
        bankSubject.setSubjectType(1); // 资产
        bankSubject.setBalanceDirection(1); // 借方
        bankSubject.setSystemType("0");
        accAccountSubjectService.createSubject(bankSubject);

        // 创建主营业务收入科目
        SubjectDTO incomeSubject = new SubjectDTO();
        incomeSubject.setSubjectCode("6001");
        incomeSubject.setSubjectName("主营业务收入");
        incomeSubject.setSubjectLevel(1);
        incomeSubject.setParentCode("");
        incomeSubject.setSubjectType(5); // 损益
        incomeSubject.setBalanceDirection(2); // 贷方
        incomeSubject.setSystemType("0");
        accAccountSubjectService.createSubject(incomeSubject);
    }
}
