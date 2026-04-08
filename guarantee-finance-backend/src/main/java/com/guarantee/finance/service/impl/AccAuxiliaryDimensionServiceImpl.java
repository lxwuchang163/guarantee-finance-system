package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.AuxiliaryDimensionDTO;
import com.guarantee.finance.entity.AccAuxiliaryDimension;
import com.guarantee.finance.mapper.AccAuxiliaryDimensionMapper;
import com.guarantee.finance.service.AccAuxiliaryDimensionService;
import com.guarantee.finance.vo.AuxiliaryDimensionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccAuxiliaryDimensionServiceImpl extends ServiceImpl<AccAuxiliaryDimensionMapper, AccAuxiliaryDimension> implements AccAuxiliaryDimensionService {

    private final AccAuxiliaryDimensionMapper accAuxiliaryDimensionMapper;

    @Override
    public IPage<AuxiliaryDimensionVO> queryDimensions(String dimensionCode, String dimensionName, String dimensionType, Integer status, Page<?> page) {
        LambdaQueryWrapper<AccAuxiliaryDimension> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dimensionCode != null, AccAuxiliaryDimension::getDimensionCode, dimensionCode)
                .like(dimensionName != null, AccAuxiliaryDimension::getDimensionName, dimensionName)
                .eq(dimensionType != null, AccAuxiliaryDimension::getDimensionType, dimensionType)
                .eq(status != null, AccAuxiliaryDimension::getStatus, status)
                .orderByAsc(AccAuxiliaryDimension::getSortOrder)
                .orderByAsc(AccAuxiliaryDimension::getDimensionCode);

        IPage<AccAuxiliaryDimension> dimensionPage = accAuxiliaryDimensionMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return dimensionPage.convert(this::convertToVO);
    }

    @Override
    public AuxiliaryDimensionVO getDimensionDetail(Long id) {
        AccAuxiliaryDimension dimension = accAuxiliaryDimensionMapper.selectById(id);
        return convertToVO(dimension);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDimension(AuxiliaryDimensionDTO dto) {
        if (!checkDimensionCodeUnique(dto.getDimensionCode(), null)) {
            throw new RuntimeException("维度编码已存在");
        }

        AccAuxiliaryDimension dimension = new AccAuxiliaryDimension();
        dimension.setDimensionCode(dto.getDimensionCode());
        dimension.setDimensionName(dto.getDimensionName());
        dimension.setDimensionType(dto.getDimensionType());
        dimension.setDescription(dto.getDescription());
        dimension.setStatus(1);
        dimension.setSortOrder(dto.getSortOrder());
        dimension.setParentCode(dto.getParentCode());
        dimension.setDimensionLevel(dto.getDimensionLevel());

        accAuxiliaryDimensionMapper.insert(dimension);
        return dimension.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDimension(Long id, AuxiliaryDimensionDTO dto) {
        AccAuxiliaryDimension dimension = accAuxiliaryDimensionMapper.selectById(id);
        if (dimension == null) {
            throw new RuntimeException("维度不存在");
        }

        if (!dimension.getDimensionCode().equals(dto.getDimensionCode()) && !checkDimensionCodeUnique(dto.getDimensionCode(), id)) {
            throw new RuntimeException("维度编码已存在");
        }

        dimension.setDimensionCode(dto.getDimensionCode());
        dimension.setDimensionName(dto.getDimensionName());
        dimension.setDimensionType(dto.getDimensionType());
        dimension.setDescription(dto.getDescription());
        dimension.setSortOrder(dto.getSortOrder());
        dimension.setParentCode(dto.getParentCode());
        dimension.setDimensionLevel(dto.getDimensionLevel());

        accAuxiliaryDimensionMapper.updateById(dimension);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDimension(Long id) {
        AccAuxiliaryDimension dimension = accAuxiliaryDimensionMapper.selectById(id);
        if (dimension == null) {
            throw new RuntimeException("维度不存在");
        }

        // 检查是否有子维度
        LambdaQueryWrapper<AccAuxiliaryDimension> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(AccAuxiliaryDimension::getParentCode, dimension.getDimensionCode());
        long childCount = accAuxiliaryDimensionMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new RuntimeException("该维度下存在子维度，无法删除");
        }

        accAuxiliaryDimensionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        AccAuxiliaryDimension dimension = accAuxiliaryDimensionMapper.selectById(id);
        if (dimension == null) {
            throw new RuntimeException("维度不存在");
        }

        dimension.setStatus(status);
        accAuxiliaryDimensionMapper.updateById(dimension);
    }

    @Override
    public List<AuxiliaryDimensionVO> getDimensionTree(String dimensionType) {
        LambdaQueryWrapper<AccAuxiliaryDimension> wrapper = new LambdaQueryWrapper<>();
        if (dimensionType != null) {
            wrapper.eq(AccAuxiliaryDimension::getDimensionType, dimensionType);
        }
        List<AccAuxiliaryDimension> dimensions = accAuxiliaryDimensionMapper.selectList(wrapper);
        return buildDimensionTree(dimensions);
    }

    @Override
    public List<AuxiliaryDimensionVO> getEnabledDimensions(String dimensionType) {
        LambdaQueryWrapper<AccAuxiliaryDimension> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccAuxiliaryDimension::getStatus, 1);
        if (dimensionType != null) {
            wrapper.eq(AccAuxiliaryDimension::getDimensionType, dimensionType);
        }
        List<AccAuxiliaryDimension> dimensions = accAuxiliaryDimensionMapper.selectList(wrapper);
        return dimensions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkDimensionCodeUnique(String dimensionCode, Long id) {
        LambdaQueryWrapper<AccAuxiliaryDimension> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccAuxiliaryDimension::getDimensionCode, dimensionCode);
        if (id != null) {
            wrapper.ne(AccAuxiliaryDimension::getId, id);
        }
        return accAuxiliaryDimensionMapper.selectCount(wrapper) == 0;
    }

    private AuxiliaryDimensionVO convertToVO(AccAuxiliaryDimension dimension) {
        if (dimension == null) {
            return null;
        }
        AuxiliaryDimensionVO vo = new AuxiliaryDimensionVO();
        vo.setId(dimension.getId());
        vo.setDimensionCode(dimension.getDimensionCode());
        vo.setDimensionName(dimension.getDimensionName());
        vo.setDimensionType(dimension.getDimensionType());
        vo.setDescription(dimension.getDescription());
        vo.setStatus(dimension.getStatus());
        vo.setSortOrder(dimension.getSortOrder());
        vo.setParentCode(dimension.getParentCode());
        vo.setDimensionLevel(dimension.getDimensionLevel());
        vo.setCreateTime(dimension.getCreateTime());
        vo.setUpdateTime(dimension.getUpdateTime());
        return vo;
    }

    private List<AuxiliaryDimensionVO> buildDimensionTree(List<AccAuxiliaryDimension> dimensions) {
        Map<String, AuxiliaryDimensionVO> dimensionMap = dimensions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toMap(AuxiliaryDimensionVO::getDimensionCode, vo -> vo));

        List<AuxiliaryDimensionVO> rootDimensions = new ArrayList<>();
        for (AuxiliaryDimensionVO vo : dimensionMap.values()) {
            if (vo.getParentCode() == null || vo.getParentCode().isEmpty()) {
                rootDimensions.add(vo);
            } else {
                AuxiliaryDimensionVO parent = dimensionMap.get(vo.getParentCode());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }
        return rootDimensions;
    }
}
