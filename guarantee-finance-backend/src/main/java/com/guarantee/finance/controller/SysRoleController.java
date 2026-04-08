package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.RoleCopyDTO;
import com.guarantee.finance.dto.RoleDTO;
import com.guarantee.finance.service.SysRoleService;
import com.guarantee.finance.vo.RoleCompareVO;
import com.guarantee.finance.vo.RoleSimpleVO;
import com.guarantee.finance.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Operation(summary = "获取所有角色列表（下拉选项用）")
    @GetMapping("/list")
    public R<List<RoleSimpleVO>> listAll() {
        List<RoleSimpleVO> list = sysRoleService.getAllRoles();
        return R.ok(list);
    }

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public R<PageResult<RoleVO>> page(@RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size,
                                      @RequestParam(required = false) String roleName,
                                      @RequestParam(required = false) String roleCode,
                                      @RequestParam(required = false) Integer status) {
        IPage<RoleVO> page = sysRoleService.queryPage(current, size, roleName, roleCode, status);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "获取角色详情（含权限）")
    @GetMapping("/{id}")
    public R<RoleVO> detail(@PathVariable Long id) {
        RoleVO vo = sysRoleService.getRoleDetail(id);
        return R.ok(vo);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleDTO dto) {
        sysRoleService.createRole(dto);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public R<Void> update(@Valid @RequestBody RoleDTO dto) {
        if (dto.getId() == null) {
            return R.fail("角色ID不能为空");
        }
        sysRoleService.updateRole(dto);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = sysRoleService.deleteRole(id);
        if (!success) {
            return R.fail("删除失败，该角色可能为内置角色或已分配给用户");
        }
        return R.ok();
    }

    @Operation(summary = "复制角色")
    @PostMapping("/copy")
    public R<RoleVO> copyRole(@Valid @RequestBody RoleCopyDTO dto) {
        RoleVO newRole = sysRoleService.copyRole(dto);
        return R.ok(newRole);
    }

    @Operation(summary = "对比两个角色")
    @GetMapping("/compare")
    public R<RoleCompareVO> compareRoles(@RequestParam Long sourceRoleId,
                                         @RequestParam Long targetRoleId) {
        RoleCompareVO compareVO = sysRoleService.compareRoles(sourceRoleId, targetRoleId);
        return R.ok(compareVO);
    }

    @Operation(summary = "更新角色状态")
    @PutMapping("/status/{id}")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysRoleService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "初始化预设角色")
    @PostMapping("/initPreset")
    public R<Void> initPresetRoles() {
        sysRoleService.initPresetRoles();
        return R.ok();
    }

    @Operation(summary = "检查角色编码唯一性")
    @GetMapping("/checkCode")
    public R<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long id) {
        boolean exists = sysRoleService.checkCodeUnique(code, id);
        return R.ok(!exists);
    }
}
