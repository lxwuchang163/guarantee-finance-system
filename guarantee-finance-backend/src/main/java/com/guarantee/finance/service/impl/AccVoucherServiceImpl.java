package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.VoucherDTO;
import com.guarantee.finance.dto.VoucherDetailDTO;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.mapper.AccVoucherDetailMapper;
import com.guarantee.finance.mapper.AccVoucherMapper;
import com.guarantee.finance.service.AccVoucherService;
import com.guarantee.finance.vo.VoucherDetailVO;
import com.guarantee.finance.vo.VoucherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccVoucherServiceImpl extends ServiceImpl<AccVoucherMapper, AccVoucher> implements AccVoucherService {

    private final AccVoucherMapper accVoucherMapper;
    private final AccVoucherDetailMapper accVoucherDetailMapper;
    private final AccAccountSubjectMapper accAccountSubjectMapper;

    @Override
    public IPage<VoucherVO> queryVouchers(String voucherNo, String period, String voucherDate, Integer status, Page<?> page) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(voucherNo != null, AccVoucher::getVoucherNo, voucherNo)
                .like(period != null, AccVoucher::getPeriod, period)
                .like(voucherDate != null, AccVoucher::getVoucherDate, voucherDate)
                .eq(status != null, AccVoucher::getStatus, status)
                .orderByDesc(AccVoucher::getVoucherDate)
                .orderByDesc(AccVoucher::getId);

        IPage<AccVoucher> voucherPage = accVoucherMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return voucherPage.convert(this::convertToVO);
    }

    @Override
    public VoucherVO getVoucherDetail(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        VoucherVO vo = convertToVO(voucher);
        if (vo != null) {
            List<VoucherDetailVO> details = accVoucherDetailMapper.selectByVoucherId(id);
            vo.setDetails(details);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(VoucherDTO dto) {
        // 生成凭证编号
        String voucherNo = generateVoucherNo(dto.getVoucherDate());
        if (!checkVoucherNoUnique(voucherNo, null)) {
            throw new RuntimeException("凭证编号已存在");
        }

        // 校验借贷平衡
        validateDebitCreditBalance(dto.getDetails());

        // 校验科目合法性
        validateSubjects(dto.getDetails());

        // 创建凭证主表
        AccVoucher voucher = new AccVoucher();
        voucher.setVoucherNo(voucherNo);
        voucher.setVoucherDate(dto.getVoucherDate());
        voucher.setPeriod(dto.getPeriod());
        voucher.setSummary(dto.getSummary());
        voucher.setStatus(0); // 草稿状态
        voucher.setVoucherType(dto.getVoucherType());
        voucher.setSourceType(dto.getSourceType());
        voucher.setSourceId(dto.getSourceId());
        voucher.setRemark(dto.getRemark());
        voucher.setNcSyncStatus("0");

        // 解析年份和月份
        if (dto.getVoucherDate() != null) {
            LocalDate date = LocalDate.parse(dto.getVoucherDate());
            voucher.setYear(date.getYear());
            voucher.setMonth(date.getMonthValue());
        }

        accVoucherMapper.insert(voucher);

        // 创建凭证明细
        createVoucherDetails(voucher.getId(), dto.getDetails());

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVoucher(Long id, VoucherDTO dto) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        // 检查状态，已记账的凭证不可修改
        if (voucher.getStatus() == 3) {
            throw new RuntimeException("已记账的凭证不可修改");
        }

        // 校验借贷平衡
        validateDebitCreditBalance(dto.getDetails());

        // 校验科目合法性
        validateSubjects(dto.getDetails());

        // 更新凭证主表
        voucher.setVoucherDate(dto.getVoucherDate());
        voucher.setPeriod(dto.getPeriod());
        voucher.setSummary(dto.getSummary());
        voucher.setVoucherType(dto.getVoucherType());
        voucher.setSourceType(dto.getSourceType());
        voucher.setSourceId(dto.getSourceId());
        voucher.setRemark(dto.getRemark());

        // 解析年份和月份
        if (dto.getVoucherDate() != null) {
            LocalDate date = LocalDate.parse(dto.getVoucherDate());
            voucher.setYear(date.getYear());
            voucher.setMonth(date.getMonthValue());
        }

        accVoucherMapper.updateById(voucher);

        // 删除旧的明细
        LambdaQueryWrapper<AccVoucherDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(AccVoucherDetail::getVoucherId, id);
        accVoucherDetailMapper.delete(detailWrapper);

        // 创建新的明细
        createVoucherDetails(id, dto.getDetails());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        // 检查状态，已记账的凭证不可删除
        if (voucher.getStatus() == 3) {
            throw new RuntimeException("已记账的凭证不可删除");
        }

        // 删除明细
        LambdaQueryWrapper<AccVoucherDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(AccVoucherDetail::getVoucherId, id);
        accVoucherDetailMapper.delete(detailWrapper);

        // 删除主表
        accVoucherMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的凭证可以提交");
        }

        voucher.setStatus(1); // 已提交状态
        accVoucherMapper.updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() == 4) {
            throw new RuntimeException("凭证已经作废");
        }

        voucher.setStatus(4); // 已作废状态
        accVoucherMapper.updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 4) {
            throw new RuntimeException("只有已作废的凭证可以恢复");
        }

        voucher.setStatus(0); // 恢复为草稿状态
        accVoucherMapper.updateById(voucher);
    }

    @Override
    public List<VoucherVO> getVouchersByPeriod(String period) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucher::getPeriod, period);
        List<AccVoucher> vouchers = accVoucherMapper.selectList(wrapper);
        return vouchers.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkVoucherNoUnique(String voucherNo, Long id) {
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccVoucher::getVoucherNo, voucherNo);
        if (id != null) {
            wrapper.ne(AccVoucher::getId, id);
        }
        return accVoucherMapper.selectCount(wrapper) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 2) {
            throw new RuntimeException("只有已审核的凭证可以记账");
        }

        voucher.setStatus(3); // 已记账
        voucher.setPostUserId(getCurrentUserId());
        accVoucherMapper.updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpostVoucher(Long id) {
        AccVoucher voucher = accVoucherMapper.selectById(id);
        if (voucher == null) {
            throw new RuntimeException("凭证不存在");
        }

        if (voucher.getStatus() != 3) {
            throw new RuntimeException("只有已记账的凭证可以反记账");
        }

        voucher.setStatus(2); // 恢复为已审核
        accVoucherMapper.updateById(voucher);
    }

    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        return 1L; // 临时返回
    }

    private String getCurrentUserName() {
        // TODO: 从Spring Security上下文获取当前用户姓名
        return "admin"; // 临时返回
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importVouchers(org.springframework.web.multipart.MultipartFile file) {
        // TODO: 使用Apache POI实现Excel导入
        // 1. 读取Excel文件
        // 2. 解析凭证数据
        // 3. 批量创建凭证
        throw new UnsupportedOperationException("Import functionality not implemented yet");
    }

    @Override
    public byte[] exportVouchersToExcel(String period) {
        // TODO: 使用Apache POI实现Excel导出
        // 1. 查询指定期间的凭证
        // 2. 创建Excel文件
        // 3. 填充数据
        // 4. 转换为byte[]返回
        throw new UnsupportedOperationException("Export to Excel functionality not implemented yet");
    }

    @Override
    public byte[] exportVoucherToPdf(Long id) {
        // TODO: 使用iText实现PDF导出
        // 1. 查询凭证详情
        // 2. 创建PDF文档
        // 3. 填充数据
        // 4. 转换为byte[]返回
        throw new UnsupportedOperationException("Export to PDF functionality not implemented yet");
    }

    private String generateVoucherNo(String voucherDate) {
        LocalDate date = LocalDate.parse(voucherDate);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PZ" + dateStr;

        // 查询当天最大的凭证编号
        LambdaQueryWrapper<AccVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(AccVoucher::getVoucherNo, prefix)
                .orderByDesc(AccVoucher::getVoucherNo)
                .last("LIMIT 1");
        AccVoucher lastVoucher = accVoucherMapper.selectOne(wrapper);

        if (lastVoucher == null) {
            return prefix + "001";
        }

        String lastNo = lastVoucher.getVoucherNo();
        int seq = Integer.parseInt(lastNo.substring(lastNo.length() - 3)) + 1;
        return prefix + String.format("%03d", seq);
    }

    private void validateDebitCreditBalance(List<VoucherDetailDTO> details) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (VoucherDetailDTO detail : details) {
            totalDebit = totalDebit.add(detail.getDebitAmount() != null ? detail.getDebitAmount() : BigDecimal.ZERO);
            totalCredit = totalCredit.add(detail.getCreditAmount() != null ? detail.getCreditAmount() : BigDecimal.ZERO);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("借贷金额不平衡");
        }
    }

    private void validateSubjects(List<VoucherDetailDTO> details) {
        for (VoucherDetailDTO detail : details) {
            AccAccountSubject subject = accAccountSubjectMapper.selectOne(
                    new LambdaQueryWrapper<AccAccountSubject>()
                            .eq(AccAccountSubject::getSubjectCode, detail.getSubjectCode())
                            .eq(AccAccountSubject::getStatus, 1)
            );
            if (subject == null) {
                throw new RuntimeException("科目不存在或已封存: " + detail.getSubjectCode());
            }
        }
    }

    private void createVoucherDetails(Long voucherId, List<VoucherDetailDTO> details) {
        for (int i = 0; i < details.size(); i++) {
            VoucherDetailDTO dto = details.get(i);
            AccVoucherDetail detail = new AccVoucherDetail();
            detail.setVoucherId(voucherId);
            detail.setLineNo(i + 1);
            detail.setSubjectCode(dto.getSubjectCode());
            detail.setSubjectName(dto.getSubjectName());
            detail.setSummary(dto.getSummary());
            detail.setDebitAmount(dto.getDebitAmount());
            detail.setCreditAmount(dto.getCreditAmount());
            detail.setAuxiliaryInfo(dto.getAuxiliaryInfo());
            detail.setDepartmentCode(dto.getDepartmentCode());
            detail.setProjectCode(dto.getProjectCode());
            detail.setCustomerCode(dto.getCustomerCode());
            detail.setSupplierCode(dto.getSupplierCode());
            detail.setBusinessCode(dto.getBusinessCode());
            detail.setBankCode(dto.getBankCode());
            detail.setRemark(dto.getRemark());
            accVoucherDetailMapper.insert(detail);
        }
    }

    private VoucherVO convertToVO(AccVoucher voucher) {
        if (voucher == null) {
            return null;
        }
        VoucherVO vo = new VoucherVO();
        vo.setId(voucher.getId());
        vo.setVoucherNo(voucher.getVoucherNo());
        vo.setVoucherDate(voucher.getVoucherDate());
        vo.setPeriod(voucher.getPeriod());
        vo.setSummary(voucher.getSummary());
        vo.setStatus(voucher.getStatus());
        vo.setStatusText(getStatusText(voucher.getStatus()));
        vo.setCreateUserId(voucher.getCreateUserId());
        vo.setApproveUserId(voucher.getApproveUserId());
        vo.setPostUserId(voucher.getPostUserId());
        vo.setAuditStatus(voucher.getAuditStatus());
        vo.setAuditOpinion(voucher.getAuditOpinion());
        vo.setVoucherType(voucher.getVoucherType());
        vo.setVoucherTypeText(getVoucherTypeText(voucher.getVoucherType()));
        vo.setSourceType(voucher.getSourceType());
        vo.setSourceId(voucher.getSourceId());
        vo.setNcSyncStatus(voucher.getNcSyncStatus());
        vo.setRemark(voucher.getRemark());
        vo.setYear(voucher.getYear());
        vo.setMonth(voucher.getMonth());
        vo.setCreateTime(voucher.getCreateTime());
        vo.setUpdateTime(voucher.getUpdateTime());
        return vo;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "已提交";
            case 2: return "已审核";
            case 3: return "已记账";
            case 4: return "已作废";
            default: return "未知";
        }
    }

    private String getVoucherTypeText(Integer voucherType) {
        switch (voucherType) {
            case 1: return "记账凭证";
            case 2: return "收款凭证";
            case 3: return "付款凭证";
            case 4: return "转账凭证";
            default: return "未知";
        }
    }
}
