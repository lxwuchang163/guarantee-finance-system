package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.OrgQueryDTO;
import com.guarantee.finance.entity.SysOrg;
import com.guarantee.finance.vo.OrgTreeVO;
import com.guarantee.finance.vo.OrgVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SysOrgService extends IService<SysOrg> {

    List<OrgTreeVO> getOrgTree();

    IPage<OrgVO> queryPage(OrgQueryDTO queryDTO, Page<OrgVO> page);

    OrgVO getOrgDetail(Long id);

    void createOrg(SysOrg org);

    void updateOrg(SysOrg org);

    boolean deleteOrg(Long id);

    void updateStatus(Long id, Integer status);

    void moveNode(Long id, Long targetParentId, Integer sortOrder);

    List<OrgVO> getChildren(Long parentId);

    String importOrgs(MultipartFile file) throws IOException;

    void exportOrgs(OrgQueryDTO queryDTO, HttpServletResponse response) throws IOException;

    void downloadTemplate(HttpServletResponse response) throws IOException;

    boolean checkCodeUnique(String code, Long id);
}
