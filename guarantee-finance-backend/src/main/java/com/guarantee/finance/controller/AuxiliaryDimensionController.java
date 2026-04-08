package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.AuxiliaryDimensionDTO;
import com.guarantee.finance.service.AccAuxiliaryDimensionService;
import com.guarantee.finance.vo.AuxiliaryDimensionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/auxiliary")
@RequiredArgsConstructor
public class AuxiliaryDimensionController {

    private final AccAuxiliaryDimensionService accAuxiliaryDimensionService;

    @GetMapping("/page")
    public R<com.baomidou.mybatisplus.core.metadata.IPage<AuxiliaryDimensionVO>> queryDimensions(
            @RequestParam(required = false) String dimensionCode,
            @RequestParam(required = false) String dimensionName,
            @RequestParam(required = false) String dimensionType,
            @RequestParam(required = false) Integer status,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Page<?> pagination = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<AuxiliaryDimensionVO> result = accAuxiliaryDimensionService.queryDimensions(dimensionCode, dimensionName, dimensionType, status, pagination);
        return R.ok(result);
    }

    @GetMapping("/detail/{id}")
    public R<AuxiliaryDimensionVO> getDimensionDetail(@PathVariable Long id) {
        AuxiliaryDimensionVO dimension = accAuxiliaryDimensionService.getDimensionDetail(id);
        return R.ok(dimension);
    }

    @PostMapping
    public R<Long> createDimension(@RequestBody AuxiliaryDimensionDTO dto) {
        Long id = accAuxiliaryDimensionService.createDimension(dto);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateDimension(@PathVariable Long id, @RequestBody AuxiliaryDimensionDTO dto) {
        accAuxiliaryDimensionService.updateDimension(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteDimension(@PathVariable Long id) {
        accAuxiliaryDimensionService.deleteDimension(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        accAuxiliaryDimensionService.changeStatus(id, status);
        return R.ok();
    }

    @GetMapping("/tree")
    public R<List<AuxiliaryDimensionVO>> getDimensionTree(@RequestParam(required = false) String dimensionType) {
        List<AuxiliaryDimensionVO> tree = accAuxiliaryDimensionService.getDimensionTree(dimensionType);
        return R.ok(tree);
    }

    @GetMapping("/enabled")
    public R<List<AuxiliaryDimensionVO>> getEnabledDimensions(@RequestParam(required = false) String dimensionType) {
        List<AuxiliaryDimensionVO> dimensions = accAuxiliaryDimensionService.getEnabledDimensions(dimensionType);
        return R.ok(dimensions);
    }

    @GetMapping("/check-code")
    public R<Boolean> checkDimensionCode(@RequestParam String dimensionCode, @RequestParam(required = false) Long id) {
        boolean isUnique = accAuxiliaryDimensionService.checkDimensionCodeUnique(dimensionCode, id);
        return R.ok(isUnique);
    }
}
