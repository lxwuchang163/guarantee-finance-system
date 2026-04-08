package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.AuxiliaryDimensionDTO;
import com.guarantee.finance.entity.AccAuxiliaryDimension;
import com.guarantee.finance.vo.AuxiliaryDimensionVO;

import java.util.List;

public interface AccAuxiliaryDimensionService extends IService<AccAuxiliaryDimension> {

    IPage<AuxiliaryDimensionVO> queryDimensions(String dimensionCode, String dimensionName, String dimensionType, Integer status, Page<?> page);

    AuxiliaryDimensionVO getDimensionDetail(Long id);

    Long createDimension(AuxiliaryDimensionDTO dto);

    void updateDimension(Long id, AuxiliaryDimensionDTO dto);

    void deleteDimension(Long id);

    void changeStatus(Long id, Integer status);

    List<AuxiliaryDimensionVO> getDimensionTree(String dimensionType);

    List<AuxiliaryDimensionVO> getEnabledDimensions(String dimensionType);

    boolean checkDimensionCodeUnique(String dimensionCode, Long id);
}
