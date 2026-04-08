package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.ConversionTemplateDTO;
import com.guarantee.finance.service.AccConversionTemplateService;
import com.guarantee.finance.vo.ConversionTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/template")
@RequiredArgsConstructor
public class ConversionTemplateController {

    private final AccConversionTemplateService accConversionTemplateService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<ConversionTemplateVO>> queryTemplates(
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<ConversionTemplateVO> result = accConversionTemplateService.queryTemplates(templateCode, templateName, businessType, status, pagination);
        return R.ok(result);
    }

    @GetMapping("/detail/{id}")
    public R<ConversionTemplateVO> getTemplateDetail(@PathVariable Long id) {
        ConversionTemplateVO template = accConversionTemplateService.getTemplateDetail(id);
        return R.ok(template);
    }

    @PostMapping
    public R<Long> createTemplate(@RequestBody ConversionTemplateDTO dto) {
        Long id = accConversionTemplateService.createTemplate(dto);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateTemplate(@PathVariable Long id, @RequestBody ConversionTemplateDTO dto) {
        accConversionTemplateService.updateTemplate(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteTemplate(@PathVariable Long id) {
        accConversionTemplateService.deleteTemplate(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        accConversionTemplateService.changeStatus(id, status);
        return R.ok();
    }

    @GetMapping("/enabled")
    public R<List<ConversionTemplateVO>> getEnabledTemplates(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String orgCode) {
        List<ConversionTemplateVO> templates = accConversionTemplateService.getEnabledTemplates(businessType, orgCode);
        return R.ok(templates);
    }

    @GetMapping("/check-code")
    public R<Boolean> checkTemplateCode(@RequestParam String templateCode, @RequestParam(required = false) Long id) {
        boolean isUnique = accConversionTemplateService.checkTemplateCodeUnique(templateCode, id);
        return R.ok(isUnique);
    }
}
