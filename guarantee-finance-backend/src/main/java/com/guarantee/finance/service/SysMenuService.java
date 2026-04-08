package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.SysMenu;
import com.guarantee.finance.controller.SysMenuController.DataScopeDTO;
import com.guarantee.finance.vo.DataScopeVO;
import com.guarantee.finance.vo.MenuTreeVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<MenuTreeVO> getMenuTree();

    List<Long> getRoleMenus(Long roleId);

    void assignMenuPermissions(Long roleId, List<Long> menuIds);

    DataScopeVO getDataScope(Long roleId);

    void setDataScope(DataScopeDTO dto);
}
