package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.SubjectBalanceInitDTO;
import com.guarantee.finance.dto.SubjectDTO;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.entity.AccSubjectBalance;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.mapper.AccSubjectBalanceMapper;
import com.guarantee.finance.service.AccAccountSubjectService;
import com.guarantee.finance.vo.SubjectVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccAccountSubjectServiceImpl extends ServiceImpl<AccAccountSubjectMapper, AccAccountSubject> implements AccAccountSubjectService {

    @Resource
    private AccAccountSubjectMapper accAccountSubjectMapper;

    @Resource
    private AccSubjectBalanceMapper accSubjectBalanceMapper;

    @Override
    public IPage<SubjectVO> querySubjects(String subjectCode, String subjectName, Integer status, Page<?> page) {
        LambdaQueryWrapper<AccAccountSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(subjectCode != null, AccAccountSubject::getSubjectCode, subjectCode)
                .like(subjectName != null, AccAccountSubject::getSubjectName, subjectName)
                .eq(status != null, AccAccountSubject::getStatus, status)
                .orderByAsc(AccAccountSubject::getSortOrder)
                .orderByAsc(AccAccountSubject::getSubjectCode);

        IPage<AccAccountSubject> subjectPage = accAccountSubjectMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return subjectPage.convert(this::convertToVO);
    }

    @Override
    public SubjectVO getSubjectDetail(Long id) {
        AccAccountSubject subject = accAccountSubjectMapper.selectById(id);
        return convertToVO(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubject(SubjectDTO dto) {
        if (!checkSubjectCodeUnique(dto.getSubjectCode(), null)) {
            throw new RuntimeException("科目编码已存在");
        }

        AccAccountSubject subject = new AccAccountSubject();
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectLevel(dto.getSubjectLevel());
        subject.setParentCode(dto.getParentCode());
        subject.setSubjectType(dto.getSubjectType());
        subject.setBalanceDirection(dto.getBalanceDirection());
        subject.setStatus(1);
        subject.setAuxiliaryDimension(dto.getAuxiliaryDimension());
        subject.setDescription(dto.getDescription());
        subject.setSortOrder(dto.getSortOrder());
        subject.setCategory(dto.getCategory());
        subject.setSystemType(dto.getSystemType());

        accAccountSubjectMapper.insert(subject);
        return subject.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubject(Long id, SubjectDTO dto) {
        AccAccountSubject subject = accAccountSubjectMapper.selectById(id);
        if (subject == null) {
            throw new RuntimeException("科目不存在");
        }

        if (!subject.getSubjectCode().equals(dto.getSubjectCode()) && !checkSubjectCodeUnique(dto.getSubjectCode(), id)) {
            throw new RuntimeException("科目编码已存在");
        }

        subject.setSubjectCode(dto.getSubjectCode());
        subject.setSubjectName(dto.getSubjectName());
        subject.setSubjectLevel(dto.getSubjectLevel());
        subject.setParentCode(dto.getParentCode());
        subject.setSubjectType(dto.getSubjectType());
        subject.setBalanceDirection(dto.getBalanceDirection());
        subject.setAuxiliaryDimension(dto.getAuxiliaryDimension());
        subject.setDescription(dto.getDescription());
        subject.setSortOrder(dto.getSortOrder());
        subject.setCategory(dto.getCategory());
        subject.setSystemType(dto.getSystemType());

        accAccountSubjectMapper.updateById(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSubject(Long id) {
        AccAccountSubject subject = accAccountSubjectMapper.selectById(id);
        if (subject == null) {
            throw new RuntimeException("科目不存在");
        }

        LambdaQueryWrapper<AccAccountSubject> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(AccAccountSubject::getParentCode, subject.getSubjectCode());
        long childCount = accAccountSubjectMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new RuntimeException("该科目下存在子科目，无法删除");
        }

        accAccountSubjectMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        AccAccountSubject subject = accAccountSubjectMapper.selectById(id);
        if (subject == null) {
            throw new RuntimeException("科目不存在");
        }

        subject.setStatus(status);
        accAccountSubjectMapper.updateById(subject);
    }

    @Override
    public List<SubjectVO> getSubjectTree() {
        List<AccAccountSubject> subjects = accAccountSubjectMapper.selectList(new LambdaQueryWrapper<>());
        return buildSubjectTree(subjects);
    }

    @Override
    public List<SubjectVO> getEnabledSubjects() {
        LambdaQueryWrapper<AccAccountSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccAccountSubject::getStatus, 1);
        List<AccAccountSubject> subjects = accAccountSubjectMapper.selectList(wrapper);
        return subjects.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkSubjectCodeUnique(String subjectCode, Long id) {
        LambdaQueryWrapper<AccAccountSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccAccountSubject::getSubjectCode, subjectCode);
        if (id != null) {
            wrapper.ne(AccAccountSubject::getId, id);
        }
        return accAccountSubjectMapper.selectCount(wrapper) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSubjects(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("导入文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new RuntimeException("仅支持Excel文件（.xlsx或.xls）");
        }

        List<AccAccountSubject> importList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("Excel文件为空");
            }

            Map<String, AccAccountSubject> existingMap = accAccountSubjectMapper.selectList(new LambdaQueryWrapper<>())
                    .stream().collect(Collectors.toMap(AccAccountSubject::getSubjectCode, s -> s, (a, b) -> a));

            int rowCount = 0;
            for (Row row : sheet) {
                rowCount++;
                if (rowCount == 1) continue;

                String subjectCode = getCellStringValue(row.getCell(0));
                String subjectName = getCellStringValue(row.getCell(1));
                String subjectTypeStr = getCellStringValue(row.getCell(2));
                String balanceDirectionStr = getCellStringValue(row.getCell(3));
                String parentCode = getCellStringValue(row.getCell(4));

                if (subjectCode == null || subjectCode.trim().isEmpty()) continue;

                AccAccountSubject existing = existingMap.get(subjectCode.trim());
                if (existing != null) {
                    existing.setSubjectName(subjectName != null ? subjectName.trim() : existing.getSubjectName());
                    if (subjectTypeStr != null && !subjectTypeStr.trim().isEmpty()) {
                        existing.setSubjectType(parseSubjectType(subjectTypeStr.trim()));
                    }
                    if (balanceDirectionStr != null && !balanceDirectionStr.trim().isEmpty()) {
                        existing.setBalanceDirection(parseBalanceDirection(balanceDirectionStr.trim()));
                    }
                    if (parentCode != null) existing.setParentCode(parentCode.trim());
                    importList.add(existing);
                } else {
                    AccAccountSubject subject = new AccAccountSubject();
                    subject.setSubjectCode(subjectCode.trim());
                    subject.setSubjectName(subjectName != null ? subjectName.trim() : "");
                    subject.setSubjectType(subjectTypeStr != null ? parseSubjectType(subjectTypeStr.trim()) : 1);
                    subject.setBalanceDirection(balanceDirectionStr != null ? parseBalanceDirection(balanceDirectionStr.trim()) : 1);
                    subject.setParentCode(parentCode != null ? parentCode.trim() : "");
                    subject.setSubjectLevel(calculateLevel(subjectCode.trim()));
                    subject.setStatus(1);
                    subject.setSortOrder(0);
                    subject.setSystemType("1");
                    importList.add(subject);
                }
            }

            if (importList.isEmpty()) {
                throw new RuntimeException("未读取到有效数据");
            }

            for (AccAccountSubject subject : importList) {
                if (subject.getId() != null) {
                    accAccountSubjectMapper.updateById(subject);
                } else {
                    accAccountSubjectMapper.insert(subject);
                }
            }

            if (!errorList.isEmpty()) {
                log.warn("导入科目存在部分错误: {}", String.join("; ", errorList));
            }

        } catch (IOException e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("导入科目失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> validateSubjects() {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<AccAccountSubject> allSubjects = accAccountSubjectMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, AccAccountSubject> codeMap = allSubjects.stream()
                .collect(Collectors.toMap(AccAccountSubject::getSubjectCode, s -> s, (a, b) -> a));

        for (AccAccountSubject subject : allSubjects) {
            if (subject.getParentCode() != null && !subject.getParentCode().isEmpty()) {
                if (!codeMap.containsKey(subject.getParentCode())) {
                    errors.add("科目 " + subject.getSubjectCode() + " 的父科目 " + subject.getParentCode() + " 不存在");
                }
            }

            if (subject.getSubjectLevel() != null && subject.getSubjectLevel() > 1) {
                if (subject.getParentCode() == null || subject.getParentCode().isEmpty()) {
                    errors.add("科目 " + subject.getSubjectCode() + " 层级大于1但未设置父科目编码");
                }
            }

            if (subject.getSubjectType() == null) {
                warnings.add("科目 " + subject.getSubjectCode() + " 未设置科目类型");
            }

            if (subject.getBalanceDirection() == null) {
                warnings.add("科目 " + subject.getSubjectCode() + " 未设置余额方向");
            }
        }

        result.put("total", allSubjects.size());
        result.put("errorCount", errors.size());
        result.put("warningCount", warnings.size());
        result.put("errors", errors);
        result.put("warnings", warnings);
        result.put("passed", errors.isEmpty());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSubjectBalances(List<SubjectBalanceInitDTO> balanceList) {
        if (balanceList == null || balanceList.isEmpty()) {
            throw new RuntimeException("余额初始化数据不能为空");
        }

        Map<String, AccAccountSubject> subjectMap = accAccountSubjectMapper.selectList(new LambdaQueryWrapper<>())
                .stream().collect(Collectors.toMap(AccAccountSubject::getSubjectCode, s -> s, (a, b) -> a));

        for (SubjectBalanceInitDTO dto : balanceList) {
            if (dto.getSubjectCode() == null || dto.getSubjectCode().trim().isEmpty()) {
                throw new RuntimeException("科目编码不能为空");
            }

            AccAccountSubject subject = subjectMap.get(dto.getSubjectCode());
            if (subject == null) {
                throw new RuntimeException("科目编码 " + dto.getSubjectCode() + " 不存在");
            }

            if (subject.getSubjectLevel() == null || subject.getSubjectLevel() < 1) {
                throw new RuntimeException("科目 " + dto.getSubjectCode() + " 层级无效");
            }

            String period = dto.getPeriod();
            if (period == null || period.isEmpty()) {
                if (dto.getYear() != null && dto.getMonth() != null) {
                    period = dto.getYear() + "-" + String.format("%02d", dto.getMonth());
                } else {
                    LocalDate now = LocalDate.now();
                    period = now.getYear() + "-" + String.format("%02d", now.getMonthValue());
                }
            }

            LambdaQueryWrapper<AccSubjectBalance> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(AccSubjectBalance::getSubjectCode, dto.getSubjectCode())
                    .eq(AccSubjectBalance::getPeriod, period);
            AccSubjectBalance existing = accSubjectBalanceMapper.selectOne(existWrapper);

            BigDecimal beginDebit = dto.getBeginDebit() != null ? dto.getBeginDebit() : BigDecimal.ZERO;
            BigDecimal beginCredit = dto.getBeginCredit() != null ? dto.getBeginCredit() : BigDecimal.ZERO;

            if (existing != null) {
                existing.setBeginDebit(beginDebit);
                existing.setBeginCredit(beginCredit);
                existing.setCurrentDebit(BigDecimal.ZERO);
                existing.setCurrentCredit(BigDecimal.ZERO);
                existing.setEndDebit(beginDebit);
                existing.setEndCredit(beginCredit);
                existing.setStatus("1");
                accSubjectBalanceMapper.updateById(existing);
            } else {
                AccSubjectBalance balance = new AccSubjectBalance();
                balance.setSubjectCode(dto.getSubjectCode());
                balance.setPeriod(period);
                balance.setBeginDebit(beginDebit);
                balance.setBeginCredit(beginCredit);
                balance.setCurrentDebit(BigDecimal.ZERO);
                balance.setCurrentCredit(BigDecimal.ZERO);
                balance.setEndDebit(beginDebit);
                balance.setEndCredit(beginCredit);
                balance.setYear(dto.getYear() != null ? dto.getYear() : Integer.parseInt(period.split("-")[0]));
                balance.setMonth(dto.getMonth() != null ? dto.getMonth() : Integer.parseInt(period.split("-")[1]));
                balance.setStatus("1");
                accSubjectBalanceMapper.insert(balance);
            }
        }
    }

    @Override
    public Map<String, Object> validateBalances(String period) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        if (period == null || period.isEmpty()) {
            LocalDate now = LocalDate.now();
            period = now.getYear() + "-" + String.format("%02d", now.getMonthValue());
        }

        LambdaQueryWrapper<AccSubjectBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccSubjectBalance::getPeriod, period);
        List<AccSubjectBalance> balances = accSubjectBalanceMapper.selectList(wrapper);

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (AccSubjectBalance balance : balances) {
            BigDecimal debit = balance.getBeginDebit() != null ? balance.getBeginDebit() : BigDecimal.ZERO;
            BigDecimal credit = balance.getBeginCredit() != null ? balance.getBeginCredit() : BigDecimal.ZERO;
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        boolean balanced = totalDebit.compareTo(totalCredit) == 0;

        result.put("period", period);
        result.put("totalDebit", totalDebit);
        result.put("totalCredit", totalCredit);
        result.put("difference", totalDebit.subtract(totalCredit));
        result.put("balanced", balanced);
        result.put("balanceCount", balances.size());

        if (!balanced) {
            errors.add("期初余额借贷不平衡：借方合计 " + totalDebit + "，贷方合计 " + totalCredit + "，差额 " + totalDebit.subtract(totalCredit));
        }

        Map<String, AccAccountSubject> subjectMap = accAccountSubjectMapper.selectList(new LambdaQueryWrapper<>())
                .stream().collect(Collectors.toMap(AccAccountSubject::getSubjectCode, s -> s, (a, b) -> a));

        for (AccSubjectBalance balance : balances) {
            if (!subjectMap.containsKey(balance.getSubjectCode())) {
                errors.add("余额记录中科目编码 " + balance.getSubjectCode() + " 在科目表中不存在");
            }
        }

        List<AccAccountSubject> leafSubjects = subjectMap.values().stream()
                .filter(s -> s.getSubjectLevel() != null && s.getSubjectLevel() >= 1)
                .collect(Collectors.toList());

        Set<String> balancedCodes = balances.stream().map(AccSubjectBalance::getSubjectCode).collect(Collectors.toSet());
        List<String> uninitialized = new ArrayList<>();
        for (AccAccountSubject subject : leafSubjects) {
            if (!balancedCodes.contains(subject.getSubjectCode())) {
                uninitialized.add(subject.getSubjectCode() + " " + subject.getSubjectName());
            }
        }

        result.put("uninitializedCount", uninitialized.size());
        result.put("uninitializedSubjects", uninitialized.stream().limit(20).collect(Collectors.toList()));
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        result.put("passed", errors.isEmpty() && balanced);

        return result;
    }

    @Override
    public List<SubjectBalanceInitDTO> getSubjectBalances(String period) {
        if (period == null || period.isEmpty()) {
            LocalDate now = LocalDate.now();
            period = now.getYear() + "-" + String.format("%02d", now.getMonthValue());
        }

        List<AccAccountSubject> allSubjects = accAccountSubjectMapper.selectList(
                new LambdaQueryWrapper<AccAccountSubject>().eq(AccAccountSubject::getStatus, 1)
                        .orderByAsc(AccAccountSubject::getSubjectCode));

        Map<String, AccSubjectBalance> balanceMap = accSubjectBalanceMapper.selectList(
                new LambdaQueryWrapper<AccSubjectBalance>().eq(AccSubjectBalance::getPeriod, period))
                .stream().collect(Collectors.toMap(AccSubjectBalance::getSubjectCode, b -> b, (a, c) -> a));

        List<SubjectBalanceInitDTO> result = new ArrayList<>();
        for (AccAccountSubject subject : allSubjects) {
            SubjectBalanceInitDTO dto = new SubjectBalanceInitDTO();
            dto.setSubjectCode(subject.getSubjectCode());
            dto.setSubjectName(subject.getSubjectName());
            dto.setPeriod(period);

            AccSubjectBalance balance = balanceMap.get(subject.getSubjectCode());
            if (balance != null) {
                dto.setBeginDebit(balance.getBeginDebit());
                dto.setBeginCredit(balance.getBeginCredit());
            } else {
                dto.setBeginDebit(BigDecimal.ZERO);
                dto.setBeginCredit(BigDecimal.ZERO);
            }

            result.add(dto);
        }

        return result;
    }

    private SubjectVO convertToVO(AccAccountSubject subject) {
        if (subject == null) return null;
        SubjectVO vo = new SubjectVO();
        vo.setId(subject.getId());
        vo.setSubjectCode(subject.getSubjectCode());
        vo.setSubjectName(subject.getSubjectName());
        vo.setSubjectLevel(subject.getSubjectLevel());
        vo.setParentCode(subject.getParentCode());
        vo.setSubjectType(subject.getSubjectType());
        vo.setBalanceDirection(subject.getBalanceDirection());
        vo.setStatus(subject.getStatus());
        vo.setAuxiliaryDimension(subject.getAuxiliaryDimension());
        vo.setDescription(subject.getDescription());
        vo.setSortOrder(subject.getSortOrder());
        vo.setCategory(subject.getCategory());
        vo.setSystemType(subject.getSystemType());
        vo.setCreateTime(subject.getCreateTime());
        vo.setUpdateTime(subject.getUpdateTime());
        return vo;
    }

    private List<SubjectVO> buildSubjectTree(List<AccAccountSubject> subjects) {
        Map<String, SubjectVO> subjectMap = subjects.stream()
                .map(this::convertToVO)
                .collect(Collectors.toMap(SubjectVO::getSubjectCode, vo -> vo));

        List<SubjectVO> rootSubjects = new ArrayList<>();
        for (SubjectVO vo : subjectMap.values()) {
            if (vo.getParentCode() == null || vo.getParentCode().isEmpty()) {
                rootSubjects.add(vo);
            } else {
                SubjectVO parent = subjectMap.get(vo.getParentCode());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }
        return rootSubjects;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return null;
        }
    }

    private Integer parseSubjectType(String typeStr) {
        switch (typeStr) {
            case "资产": case "1": return 1;
            case "负债": case "2": return 2;
            case "所有者权益": case "3": return 3;
            case "成本": case "4": return 4;
            case "损益": case "5": return 5;
            default: return 1;
        }
    }

    private Integer parseBalanceDirection(String dirStr) {
        switch (dirStr) {
            case "借": case "借方": case "1": return 1;
            case "贷": case "贷方": case "2": return 2;
            default: return 1;
        }
    }

    private Integer calculateLevel(String subjectCode) {
        if (subjectCode == null || subjectCode.isEmpty()) return 1;
        if (subjectCode.length() <= 4) return 1;
        if (subjectCode.length() <= 6) return 2;
        if (subjectCode.length() <= 8) return 3;
        if (subjectCode.length() <= 10) return 4;
        return 5;
    }
}
