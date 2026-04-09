package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.SysMenu;
import com.guarantee.finance.service.SysMenuService;
import com.guarantee.finance.vo.DataScopeVO;
import com.guarantee.finance.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单权限管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @Operation(summary = "获取菜单权限树")
    @GetMapping("/tree")
    public R<List<MenuTreeVO>> getMenuTree() {
        List<MenuTreeVO> tree = sysMenuService.getMenuTree();
        return R.ok(tree);
    }

    @Operation(summary = "获取角色已分配的菜单权限")
    @GetMapping("/role/{roleId}")
    public R<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<Long> menuIds = sysMenuService.getRoleMenus(roleId);
        return R.ok(menuIds);
    }

    @Operation(summary = "为角色分配菜单权限")
    @PostMapping("/assign")
    public R<Void> assignMenuPermissions(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        sysMenuService.assignMenuPermissions(roleId, menuIds);
        return R.ok();
    }

    @Operation(summary = "获取角色数据权限配置")
    @GetMapping("/dataScope/{roleId}")
    public R<DataScopeVO> getDataScope(@PathVariable Long roleId) {
        DataScopeVO dataScope = sysMenuService.getDataScope(roleId);
        return R.ok(dataScope);
    }

    @Operation(summary = "设置数据权限")
    @PutMapping("/dataScope")
    public R<Void> setDataScope(@RequestBody DataScopeDTO dto) {
        sysMenuService.setDataScope(dto);
        return R.ok();
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public R<Long> addMenu(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return R.ok(menu.getId());
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public R<Void> updateMenu(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{menuId}")
    public R<Void> deleteMenu(@PathVariable Long menuId) {
        sysMenuService.removeById(menuId);
        return R.ok();
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{menuId}")
    public R<SysMenu> getMenu(@PathVariable Long menuId) {
        SysMenu menu = sysMenuService.getById(menuId);
        return R.ok(menu);
    }

    // 内部使用DTO
    public static class DataScopeDTO {
        public Long roleId;
        public String dataScope;
        public List<String> orgCodes;
    }
}
