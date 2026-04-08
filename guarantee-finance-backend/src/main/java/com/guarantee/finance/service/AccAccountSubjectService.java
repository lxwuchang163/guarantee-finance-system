package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.SubjectDTO;
import com.guarantee.finance.entity.AccAccountSubject;
import com.guarantee.finance.vo.SubjectVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AccAccountSubjectService extends IService<AccAccountSubject> {

    IPage<SubjectVO> querySubjects(String subjectCode, String subjectName, Integer status, Page<?> page);

    SubjectVO getSubjectDetail(Long id);

    Long createSubject(SubjectDTO dto);

    void updateSubject(Long id, SubjectDTO dto);

    void deleteSubject(Long id);

    void changeStatus(Long id, Integer status);

    List<SubjectVO> getSubjectTree();

    List<SubjectVO> getEnabledSubjects();

    boolean checkSubjectCodeUnique(String subjectCode, Long id);

    void importSubjects(MultipartFile file);

    Map<String, Object> validateSubjects();
}
