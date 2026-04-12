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
import com.guarantee.finance.service.AccountPostingService;
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
    private final AccountPostingService accountPostingService;

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

        if (dto.getVoucherDate() != null) {
            LocalDate date = parseVoucherDate(dto.getVoucherDate());
            if (date != null) {
                voucher.setYear(date.getYear());
                voucher.setMonth(date.getMonthValue());
            }
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

        if (dto.getVoucherDate() != null) {
            LocalDate date = parseVoucherDate(dto.getVoucherDate());
            if (date != null) {
                voucher.setYear(date.getYear());
                voucher.setMonth(date.getMonthValue());
            }
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
        accountPostingService.postVoucher(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpostVoucher(Long id) {
        accountPostingService.unpostVoucher(id);
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
        try {
            org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream());
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            java.util.Map<String, VoucherDTO> voucherMap = new java.util.LinkedHashMap<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String voucherNo = getCellValue(row.getCell(0));
                if (voucherNo == null || voucherNo.isEmpty()) continue;
                
                VoucherDTO dto = voucherMap.computeIfAbsent(voucherNo, k -> {
                    VoucherDTO newDto = new VoucherDTO();
                    newDto.setVoucherNo(voucherNo);
                    newDto.setVoucherDate(getCellValue(row.getCell(1)));
                    newDto.setPeriod(getCellValue(row.getCell(2)));
                    newDto.setSummary(getCellValue(row.getCell(3)));
                    String voucherTypeStr = getCellValue(row.getCell(4));
                    newDto.setVoucherType(voucherTypeStr != null ? Integer.parseInt(voucherTypeStr) : 1);
                    newDto.setSourceType("2");
                    newDto.setDetails(new java.util.ArrayList<>());
                    return newDto;
                });
                
                VoucherDetailDTO detail = new VoucherDetailDTO();
                detail.setSubjectCode(getCellValue(row.getCell(5)));
                detail.setSubjectName(getCellValue(row.getCell(6)));
                detail.setSummary(getCellValue(row.getCell(7)));
                String debitStr = getCellValue(row.getCell(8));
                detail.setDebitAmount(debitStr != null && !debitStr.isEmpty() ? new BigDecimal(debitStr) : BigDecimal.ZERO);
                String creditStr = getCellValue(row.getCell(9));
                detail.setCreditAmount(creditStr != null && !creditStr.isEmpty() ? new BigDecimal(creditStr) : BigDecimal.ZERO);
                
                dto.getDetails().add(detail);
            }
            
            workbook.close();
            
            for (VoucherDTO dto : voucherMap.values()) {
                try {
                    createVoucher(dto);
                } catch (Exception e) {
                    log.error("导入凭证失败: " + dto.getVoucherNo(), e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("导入凭证失败: " + e.getMessage(), e);
        }
    }
    
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    @Override
    public byte[] exportVouchersToExcel(String period) {
        try {
            List<VoucherVO> vouchers = getVouchersByPeriod(period);
            
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("凭证列表");
            
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"凭证编号", "凭证日期", "会计期间", "摘要", "凭证类型", "状态", "创建人", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            for (VoucherVO voucher : vouchers) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(voucher.getVoucherNo());
                row.createCell(1).setCellValue(voucher.getVoucherDate());
                row.createCell(2).setCellValue(voucher.getPeriod());
                row.createCell(3).setCellValue(voucher.getSummary());
                row.createCell(4).setCellValue(voucher.getVoucherTypeText());
                row.createCell(5).setCellValue(voucher.getStatusText());
                row.createCell(6).setCellValue(voucher.getCreateUserName() != null ? voucher.getCreateUserName() : "");
                row.createCell(7).setCellValue(voucher.getCreateTime() != null ? voucher.getCreateTime().toString() : "");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportVoucherToPdf(Long id) {
        try {
            VoucherVO voucher = getVoucherDetail(id);
            if (voucher == null) {
                throw new RuntimeException("凭证不存在");
            }
            
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            com.itextpdf.text.pdf.BaseFont bfChinese = com.itextpdf.text.pdf.BaseFont.createFont(
                "STSong-Light", "UniGB-UCS2-H", com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(bfChinese, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(bfChinese, 12, com.itextpdf.text.Font.NORMAL);
            
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("记账凭证", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph(" "));
            
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(2);
            table.setWidthPercentage(100);
            
            addPdfCell(table, "凭证编号：" + voucher.getVoucherNo(), normalFont);
            addPdfCell(table, "凭证日期：" + voucher.getVoucherDate(), normalFont);
            addPdfCell(table, "会计期间：" + voucher.getPeriod(), normalFont);
            addPdfCell(table, "凭证类型：" + voucher.getVoucherTypeText(), normalFont);
            
            document.add(table);
            document.add(new com.itextpdf.text.Paragraph(" "));
            
            com.itextpdf.text.pdf.PdfPTable detailTable = new com.itextpdf.text.pdf.PdfPTable(5);
            detailTable.setWidthPercentage(100);
            detailTable.setWidths(new float[]{1, 3, 3, 2, 2});
            
            addPdfCell(detailTable, "行号", normalFont);
            addPdfCell(detailTable, "科目", normalFont);
            addPdfCell(detailTable, "摘要", normalFont);
            addPdfCell(detailTable, "借方金额", normalFont);
            addPdfCell(detailTable, "贷方金额", normalFont);
            
            if (voucher.getDetails() != null) {
                for (VoucherDetailVO detail : voucher.getDetails()) {
                    addPdfCell(detailTable, String.valueOf(detail.getLineNo()), normalFont);
                    addPdfCell(detailTable, detail.getSubjectCode() + " " + detail.getSubjectName(), normalFont);
                    addPdfCell(detailTable, detail.getSummary() != null ? detail.getSummary() : "", normalFont);
                    addPdfCell(detailTable, detail.getDebitAmount() != null ? detail.getDebitAmount().toString() : "0.00", normalFont);
                    addPdfCell(detailTable, detail.getCreditAmount() != null ? detail.getCreditAmount().toString() : "0.00", normalFont);
                }
            }
            
            document.add(detailTable);
            document.add(new com.itextpdf.text.Paragraph(" "));
            document.add(new com.itextpdf.text.Paragraph("状态：" + voucher.getStatusText(), normalFont));
            
            document.close();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出PDF失败: " + e.getMessage(), e);
        }
    }
    
    private void addPdfCell(com.itextpdf.text.pdf.PdfPTable table, String text, com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(text, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String generateVoucherNo(String voucherDate) {
        LocalDate date = parseVoucherDate(voucherDate);
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

    private LocalDate parseVoucherDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            if (dateStr.contains("T")) {
                return LocalDate.parse(dateStr.substring(0, dateStr.indexOf('T')));
            }
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                throw new RuntimeException("日期格式错误，无法解析: " + dateStr);
            }
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
