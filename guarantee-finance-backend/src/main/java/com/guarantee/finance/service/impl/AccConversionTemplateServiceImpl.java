package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.ConversionTemplateDTO;
import com.guarantee.finance.entity.AccConversionTemplate;
import com.guarantee.finance.mapper.AccConversionTemplateMapper;
import com.guarantee.finance.service.AccConversionTemplateService;
import com.guarantee.finance.vo.ConversionTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccConversionTemplateServiceImpl extends ServiceImpl<AccConversionTemplateMapper, AccConversionTemplate> implements AccConversionTemplateService {

    private final AccConversionTemplateMapper accConversionTemplateMapper;

    @Override
    public IPage<ConversionTemplateVO> queryTemplates(String templateCode, String templateName, String businessType, Integer status, Page<?> page) {
        LambdaQueryWrapper<AccConversionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(templateCode != null, AccConversionTemplate::getTemplateCode, templateCode)
                .like(templateName != null, AccConversionTemplate::getTemplateName, templateName)
                .eq(businessType != null, AccConversionTemplate::getBusinessType, businessType)
                .eq(status != null, AccConversionTemplate::getStatus, status)
                .orderByAsc(AccConversionTemplate::getSortOrder)
                .orderByAsc(AccConversionTemplate::getTemplateCode);

        IPage<AccConversionTemplate> templatePage = accConversionTemplateMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        return templatePage.convert(this::convertToVO);
    }

    @Override
    public ConversionTemplateVO getTemplateDetail(Long id) {
        AccConversionTemplate template = accConversionTemplateMapper.selectById(id);
        return convertToVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(ConversionTemplateDTO dto) {
        if (!checkTemplateCodeUnique(dto.getTemplateCode(), null)) {
            throw new RuntimeException("模板编码已存在");
        }

        AccConversionTemplate template = new AccConversionTemplate();
        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setBusinessType(dto.getBusinessType());
        template.setOrgCode(dto.getOrgCode());
        template.setDescription(dto.getDescription());
        template.setTemplateContent(dto.getTemplateContent());
        template.setStatus(1);
        template.setSortOrder(dto.getSortOrder());

        accConversionTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(Long id, ConversionTemplateDTO dto) {
        AccConversionTemplate template = accConversionTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        if (!template.getTemplateCode().equals(dto.getTemplateCode()) && !checkTemplateCodeUnique(dto.getTemplateCode(), id)) {
            throw new RuntimeException("模板编码已存在");
        }

        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setBusinessType(dto.getBusinessType());
        template.setOrgCode(dto.getOrgCode());
        template.setDescription(dto.getDescription());
        template.setTemplateContent(dto.getTemplateContent());
        template.setSortOrder(dto.getSortOrder());

        accConversionTemplateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        AccConversionTemplate template = accConversionTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        accConversionTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        AccConversionTemplate template = accConversionTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        template.setStatus(status);
        accConversionTemplateMapper.updateById(template);
    }

    @Override
    public List<ConversionTemplateVO> getEnabledTemplates(String businessType, String orgCode) {
        LambdaQueryWrapper<AccConversionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccConversionTemplate::getStatus, 1);
        if (businessType != null) {
            wrapper.eq(AccConversionTemplate::getBusinessType, businessType);
        }
        if (orgCode != null) {
            wrapper.eq(AccConversionTemplate::getOrgCode, orgCode);
        }
        List<AccConversionTemplate> templates = accConversionTemplateMapper.selectList(wrapper);
        return templates.stream().map(this::convertToVO).toList();
    }

    @Override
    public boolean checkTemplateCodeUnique(String templateCode, Long id) {
        LambdaQueryWrapper<AccConversionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccConversionTemplate::getTemplateCode, templateCode);
        if (id != null) {
            wrapper.ne(AccConversionTemplate::getId, id);
        }
        return accConversionTemplateMapper.selectCount(wrapper) == 0;
    }

    private ConversionTemplateVO convertToVO(AccConversionTemplate template) {
        if (template == null) {
            return null;
        }
        ConversionTemplateVO vo = new ConversionTemplateVO();
        vo.setId(template.getId());
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setBusinessType(template.getBusinessType());
        vo.setBusinessTypeText(getBusinessTypeText(template.getBusinessType()));
        vo.setOrgCode(template.getOrgCode());
        vo.setDescription(template.getDescription());
        vo.setTemplateContent(template.getTemplateContent());
        vo.setStatus(template.getStatus());
        vo.setStatusText(template.getStatus() == 1 ? "启用" : "停用");
        vo.setSortOrder(template.getSortOrder());
        vo.setCreateTime(template.getCreateTime());
        vo.setUpdateTime(template.getUpdateTime());
        return vo;
    }

    private String getBusinessTypeText(String businessType) {
        switch (businessType) {
            case "receipt": return "收款";
            case "payment": return "付款";
            case "refund": return "退款";
            case "compensation": return "代偿";
            default: return "未知";
        }
    }
}
