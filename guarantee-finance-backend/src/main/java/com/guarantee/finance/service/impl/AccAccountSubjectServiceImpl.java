package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.SubjectDTO;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.mapper.AccAccountSubjectMapper;
import com.guarantee.finance.service.AccAccountSubjectService;
import com.guarantee.finance.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccAccountSubjectServiceImpl extends ServiceImpl<AccAccountSubjectMapper, AccAccountSubject> implements AccAccountSubjectService {

    private final AccAccountSubjectMapper accAccountSubjectMapper;

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

        // 检查是否有子科目
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
    public void importSubjects(MultipartFile file) {
        // TODO: 实现Excel导入功能
        throw new UnsupportedOperationException("Import functionality not implemented yet");
    }

    @Override
    public Map<String, Object> validateSubjects() {
        // TODO: 实现科目校验功能
        throw new UnsupportedOperationException("Validation functionality not implemented yet");
    }

    private SubjectVO convertToVO(AccAccountSubject subject) {
        if (subject == null) {
            return null;
        }
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
}
