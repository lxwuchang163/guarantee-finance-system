package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.SubjectDTO;
import com.guarantee.finance.service.AccAccountSubjectService;
import com.guarantee.finance.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final AccAccountSubjectService accAccountSubjectService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<SubjectVO>> querySubjects(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) Integer status,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<SubjectVO> result = accAccountSubjectService.querySubjects(subjectCode, subjectName, status, pagination);
        return R.ok(result);
    }

    @GetMapping("/detail/{id}")
    public R<SubjectVO> getSubjectDetail(@PathVariable Long id) {
        SubjectVO subject = accAccountSubjectService.getSubjectDetail(id);
        return R.ok(subject);
    }

    @PostMapping
    public R<Long> createSubject(@RequestBody SubjectDTO dto) {
        Long id = accAccountSubjectService.createSubject(dto);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateSubject(@PathVariable Long id, @RequestBody SubjectDTO dto) {
        accAccountSubjectService.updateSubject(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteSubject(@PathVariable Long id) {
        accAccountSubjectService.deleteSubject(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        accAccountSubjectService.changeStatus(id, status);
        return R.ok();
    }

    @GetMapping("/tree")
    public R<List<SubjectVO>> getSubjectTree() {
        List<SubjectVO> tree = accAccountSubjectService.getSubjectTree();
        return R.ok(tree);
    }

    @GetMapping("/enabled")
    public R<List<SubjectVO>> getEnabledSubjects() {
        List<SubjectVO> subjects = accAccountSubjectService.getEnabledSubjects();
        return R.ok(subjects);
    }

    @GetMapping("/check-code")
    public R<Boolean> checkSubjectCode(@RequestParam String subjectCode, @RequestParam(required = false) Long id) {
        boolean isUnique = accAccountSubjectService.checkSubjectCodeUnique(subjectCode, id);
        return R.ok(isUnique);
    }

    @PostMapping("/import")
    public R<Void> importSubjects(@RequestParam("file") MultipartFile file) {
        accAccountSubjectService.importSubjects(file);
        return R.ok();
    }

    @PostMapping("/validate")
    public R<Map<String, Object>> validateSubjects() {
        Map<String, Object> result = accAccountSubjectService.validateSubjects();
        return R.ok(result);
    }
}
