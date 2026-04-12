package com.guarantee.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.controller.SysMenuController.DataScopeDTO;
import com.guarantee.finance.entity.SysMenu;
import com.guarantee.finance.entity.SysOrg;
import com.guarantee.finance.entity.SysRoleMenu;
import com.guarantee.finance.mapper.SysMenuMapper;
import com.guarantee.finance.mapper.SysOrgMapper;
import com.guarantee.finance.mapper.SysRoleMenuMapper;
import com.guarantee.finance.service.SysMenuService;
import com.guarantee.finance.vo.DataScopeVO;
import com.guarantee.finance.vo.MenuTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @jakarta.annotation.Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @jakarta.annotation.Resource
    private SysOrgMapper sysOrgMapper;

    @Override
    public List<MenuTreeVO> getMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getStatus, 1);
        wrapper.orderByAsc(SysMenu::getSortOrder);

        List<SysMenu> allMenus = list(wrapper);
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<Long> getRoleMenus(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(wrapper);

        if (CollUtil.isEmpty(roleMenus)) {
            return new ArrayList<>();
        }

        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenuPermissions(Long roleId, List<Long> menuIds) {
        // 先删除原有权限
        LambdaQueryWrapper<SysRoleMenu> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(deleteWrapper);

        // 再添加新权限
        if (CollUtil.isNotEmpty(menuIds)) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
    }

    @Override
    public DataScopeVO getDataScope(Long roleId) {
        DataScopeVO vo = new DataScopeVO();
        vo.setRoleId(roleId);
        // 默认返回本机构及以下
        vo.setDataScope("ORG_AND_CHILD");
        vo.setOrgCodes(new ArrayList<>());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDataScope(DataScopeDTO dto) {
    }

    @Override
    public List<MenuTreeVO> getCurrentUserMenuTree() {
        return getMenuTree();
    }

    @Override
    public List<String> getCurrentUserPermissions() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .isNotNull(SysMenu::getPermission));
        return allMenus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<MenuTreeVO> buildMenuTree(List<SysMenu> allMenus, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();

        for (SysMenu menu : allMenus) {
            if ((parentId == 0L && (menu.getParentId() == null || menu.getParentId() == 0L))
                    || (menu.getParentId() != null && menu.getParentId().equals(parentId))) {
                MenuTreeVO node = new MenuTreeVO();
                BeanUtils.copyProperties(menu, node);
                node.setChildren(buildMenuTree(allMenus, menu.getId()));
                tree.add(node);
            }
        }

        return tree;
    }
}
