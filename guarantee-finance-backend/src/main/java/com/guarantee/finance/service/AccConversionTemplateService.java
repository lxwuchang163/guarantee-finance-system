package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.ConversionTemplateDTO;
import com.guarantee.finance.entity.AccConversionTemplate;
import com.guarantee.finance.vo.ConversionTemplateVO;

import java.util.List;

public interface AccConversionTemplateService extends IService<AccConversionTemplate> {

    IPage<ConversionTemplateVO> queryTemplates(String templateCode, String templateName, String businessType, Integer status, Page<?> page);

    ConversionTemplateVO getTemplateDetail(Long id);

    Long createTemplate(ConversionTemplateDTO dto);

    void updateTemplate(Long id, ConversionTemplateDTO dto);

    void deleteTemplate(Long id);

    void changeStatus(Long id, Integer status);

    List<ConversionTemplateVO> getEnabledTemplates(String businessType, String orgCode);

    boolean checkTemplateCodeUnique(String templateCode, Long id);
}
