package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.RoleCopyDTO;
import com.guarantee.finance.dto.RoleDTO;
import com.guarantee.finance.entity.SysRole;
import com.guarantee.finance.vo.RoleCompareVO;
import com.guarantee.finance.vo.RoleSimpleVO;
import com.guarantee.finance.vo.RoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<RoleSimpleVO> getAllRoles();

    IPage<RoleVO> queryPage(Long current, Long size, String roleName, String roleCode, Integer status);

    RoleVO getRoleDetail(Long id);

    void createRole(RoleDTO dto);

    void updateRole(RoleDTO dto);

    boolean deleteRole(Long id);

    RoleVO copyRole(RoleCopyDTO dto);

    RoleCompareVO compareRoles(Long sourceRoleId, Long targetRoleId);

    void updateStatus(Long id, Integer status);

    void initPresetRoles();

    boolean checkCodeUnique(String code, Long id);
}
