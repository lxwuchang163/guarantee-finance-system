package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.SubjectBalanceDTO;
import com.guarantee.finance.service.AccSubjectBalanceService;
import com.guarantee.finance.vo.SubjectBalanceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounting/balance")
@RequiredArgsConstructor
public class SubjectBalanceController {

    private final AccSubjectBalanceService accSubjectBalanceService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<SubjectBalanceVO>> queryBalances(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String period,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<SubjectBalanceVO> result = accSubjectBalanceService.queryBalances(subjectCode, period, pagination);
        return R.ok(result);
    }

    @GetMapping("/detail/{id}")
    public R<SubjectBalanceVO> getBalanceDetail(@PathVariable Long id) {
        SubjectBalanceVO balance = accSubjectBalanceService.getBalanceDetail(id);
        return R.ok(balance);
    }

    @PostMapping
    public R<Long> createBalance(@RequestBody SubjectBalanceDTO dto) {
        Long id = accSubjectBalanceService.createBalance(dto);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateBalance(@PathVariable Long id, @RequestBody SubjectBalanceDTO dto) {
        accSubjectBalanceService.updateBalance(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteBalance(@PathVariable Long id) {
        accSubjectBalanceService.deleteBalance(id);
        return R.ok();
    }

    @PostMapping("/import")
    public R<Void> importBalances(@RequestParam("file") MultipartFile file) {
        accSubjectBalanceService.importBalances(file);
        return R.ok();
    }

    @PostMapping("/validate")
    public R<Map<String, Object>> validateBalances(@RequestParam String period) {
        Map<String, Object> result = accSubjectBalanceService.validateBalances(period);
        return R.ok(result);
    }

    @PostMapping("/confirm")
    public R<Void> confirmBalances(@RequestParam String period) {
        accSubjectBalanceService.confirmBalances(period);
        return R.ok();
    }

    @GetMapping("/period")
    public R<List<SubjectBalanceVO>> getPeriodBalances(@RequestParam String period) {
        List<SubjectBalanceVO> balances = accSubjectBalanceService.getPeriodBalances(period);
        return R.ok(balances);
    }
}
