package com.guarantee.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.RoleCopyDTO;
import com.guarantee.finance.dto.RoleDTO;
import com.guarantee.finance.entity.SysRole;
import com.guarantee.finance.entity.SysRoleMenu;
import com.guarantee.finance.entity.SysUserRole;
import com.guarantee.finance.mapper.SysRoleMapper;
import com.guarantee.finance.mapper.SysRoleMenuMapper;
import com.guarantee.finance.mapper.SysUserRoleMapper;
import com.guarantee.finance.service.SysRoleService;
import com.guarantee.finance.vo.RoleCompareVO;
import com.guarantee.finance.vo.RoleDiffVO;
import com.guarantee.finance.vo.RoleSimpleVO;
import com.guarantee.finance.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @jakarta.annotation.Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @jakarta.annotation.Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<RoleSimpleVO> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSortOrder);

        List<SysRole> roles = list(wrapper);

        return roles.stream().map(role -> {
            RoleSimpleVO vo = new RoleSimpleVO();
            vo.setId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<RoleVO> queryPage(Long current, Long size, String roleName, String roleCode, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (StrUtil.isNotBlank(roleCode)) {
            wrapper.like(SysRole::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);
        wrapper.orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> rolePage = page(new Page<>(current, size), wrapper);

        IPage<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> voList = rolePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            return null;
        }

        RoleVO vo = convertToVO(role);

        // 查询角色关联的菜单ID
        LambdaQueryWrapper<SysRoleMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.eq(SysRoleMenu::getRoleId, id);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(menuWrapper);

        if (CollUtil.isNotEmpty(roleMenus)) {
            List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            vo.setMenuIds(menuIds);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO dto) {
        boolean codeExists = checkCodeUnique(dto.getRoleCode(), null);
        if (codeExists) {
            throw new RuntimeException("角色编码已存在: " + dto.getRoleCode());
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);

        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }

        save(role);

        // 保存菜单权限关联
        if (CollUtil.isNotEmpty(dto.getMenuIds())) {
            saveRoleMenus(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO dto) {
        SysRole existing = getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }

        if (!existing.getRoleCode().equals(dto.getRoleCode())) {
            boolean codeExists = checkCodeUnique(dto.getRoleCode(), dto.getId());
            if (codeExists) {
                throw new RuntimeException("角色编码已存在: " + dto.getRoleCode());
            }
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        updateById(role);

        // 更新菜单权限关联
        deleteRoleMenus(dto.getId());
        if (CollUtil.isNotEmpty(dto.getMenuIds())) {
            saveRoleMenus(dto.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            return false;
        }

        // 检查是否为内置角色（ADMIN等）
        String[] builtInRoles = {"ADMIN", "USER"};
        for (String builtIn : builtInRoles) {
            if (builtIn.equals(role.getRoleCode())) {
                throw new RuntimeException("内置角色不允许删除");
            }
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<SysUserRole> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUserRole::getRoleId, id);
        Long userCount = sysUserRoleMapper.selectCount(userWrapper);
        if (userCount > 0) {
            throw new RuntimeException("该角色已分配给用户，无法删除");
        }

        // 删除角色菜单关联
        deleteRoleMenus(id);

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO copyRole(RoleCopyDTO dto) {
        SysRole sourceRole = getById(dto.getSourceRoleId());
        if (sourceRole == null) {
            throw new RuntimeException("源角色不存在");
        }

        // 检查新编码唯一性
        boolean codeExists = checkCodeUnique(dto.getNewRoleCode(), null);
        if (codeExists) {
            throw new RuntimeException("角色编码已存在: " + dto.getNewRoleCode());
        }

        // 创建新角色
        SysRole newRole = new SysRole();
        newRole.setRoleName(dto.getNewRoleName());
        newRole.setRoleCode(dto.getNewRoleCode());
        newRole.setDescription(sourceRole.getDescription() + "（复制自：" + sourceRole.getRoleName() + "）");
        newRole.setStatus(sourceRole.getStatus());
        newRole.setSortOrder(sourceRole.getSortOrder());
        save(newRole);

        // 复制菜单权限
        LambdaQueryWrapper<SysRoleMenu> sourceMenuWrapper = new LambdaQueryWrapper<>();
        sourceMenuWrapper.eq(SysRoleMenu::getRoleId, dto.getSourceRoleId());
        List<SysRoleMenu> sourceMenus = sysRoleMenuMapper.selectList(sourceMenuWrapper);

        if (CollUtil.isNotEmpty(sourceMenus)) {
            List<Long> menuIds = sourceMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            saveRoleMenus(newRole.getId(), menuIds);
        }

        return getRoleDetail(newRole.getId());
    }

    @Override
    public RoleCompareVO compareRoles(Long sourceRoleId, Long targetRoleId) {
        RoleVO sourceRole = getRoleDetail(sourceRoleId);
        RoleVO targetRole = getRoleDetail(targetRoleId);

        if (sourceRole == null || targetRole == null) {
            throw new RuntimeException("角色不存在");
        }

        RoleCompareVO compareVO = new RoleCompareVO();
        compareVO.setSourceRole(sourceRole);
        compareVO.setTargetRole(targetRole);

        List<RoleDiffVO> differences = new ArrayList<>();

        // 比较基本信息
        compareField(differences, "角色名称", "roleName", sourceRole.getRoleName(), targetRole.getRoleName());
        compareField(differences, "角色编码", "roleCode", sourceRole.getRoleCode(), targetRole.getRoleCode());
        compareField(differences, "描述", "description", sourceRole.getDescription(), targetRole.getDescription());
        compareField(differences, "状态", "status",
                sourceRole.getStatus() != null ? String.valueOf(sourceRole.getStatus()) : null,
                targetRole.getStatus() != null ? String.valueOf(targetRole.getStatus()) : null);

        // 比较菜单权限
        Set<Long> sourceMenuIds = new HashSet<>(sourceRole.getMenuIds() != null ? sourceRole.getMenuIds() : Collections.emptyList());
        Set<Long> targetMenuIds = new HashSet<>(targetRole.getMenuIds() != null ? targetRole.getMenuIds() : Collections.emptyList());

        Set<Long> onlyInSource = new HashSet<>(sourceMenuIds);
        onlyInSource.removeAll(targetMenuIds);

        Set<Long> onlyInTarget = new HashSet<>(targetMenuIds);
        onlyInTarget.removeAll(sourceMenuIds);

        if (!onlyInSource.isEmpty() || !onlyInTarget.isEmpty()) {
            RoleDiffVO menuDiff = new RoleDiffVO();
            menuDiff.setField("menuIds");
            menuDiff.setFieldName("菜单权限");
            menuDiff.setSourceValue(onlyInSource.isEmpty() ? "-" : onlyInSource.toString());
            menuDiff.setTargetValue(onlyInTarget.isEmpty() ? "-" : onlyInTarget.toString());
            menuDiff.setDiffType("different");
            differences.add(menuDiff);
        }

        compareVO.setDifferences(differences);
        return compareVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysRole role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        role.setStatus(status);
        updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPresetRoles() {
        // 定义预设角色
        Map<String, String[]> presetRoles = new LinkedHashMap<>();
        presetRoles.put("系统管理员", new String[]{"ADMIN", "拥有系统所有权限，可管理所有模块"});
        presetRoles.put("财务主管", new String[]{"FINANCE_MANAGER", "负责财务管理、审批等工作"});
        presetRoles.put("会计", new String[]{"ACCOUNTANT", "负责凭证录入、账务处理等工作"});
        presetRoles.put("出纳", new String[]{"CASHIER", "负责收付款、资金管理等工作"});
        presetRoles.put("业务员", new String[]{"SALESMAN", "负责业务办理、客户对接等工作"});
        presetRoles.put("审批人", new String[]{"APPROVER", "负责单据审批工作"});
        presetRoles.put("普通用户", new String[]{"NORMAL_USER", "普通业务操作人员"});

        int sortOrder = 0;
        for (Map.Entry<String, String[]> entry : presetRoles.entrySet()) {
            String roleName = entry.getKey();
            String[] value = entry.getValue();
            String roleCode = value[0];
            String description = value[1];

            // 检查是否已存在
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysRole::getRoleCode, roleCode);
            long count = count(wrapper);
            if (count > 0) {
                continue; // 已存在则跳过
            }

            SysRole role = new SysRole();
            role.setRoleName(roleName);
            role.setRoleCode(roleCode);
            role.setDescription(description);
            role.setStatus(1);
            role.setSortOrder(sortOrder++);
            save(role);
        }
    }

    @Override
    public boolean checkCodeUnique(String code, Long id) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, code);
        if (id != null) {
            wrapper.ne(SysRole::getId, id);
        }
        return count(wrapper) > 0;
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
    }

    private void deleteRoleMenus(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(wrapper);
    }

    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    private void compareField(List<RoleDiffVO> diffs, String fieldName, String field,
                               Object sourceValue, Object targetValue) {
        String srcStr = sourceValue != null ? String.valueOf(sourceValue) : "";
        String tgtStr = targetValue != null ? String.valueOf(targetValue) : "";

        if (!srcStr.equals(tgtStr)) {
            RoleDiffVO diff = new RoleDiffVO();
            diff.setField(field);
            diff.setFieldName(fieldName);
            diff.setSourceValue(srcStr.isEmpty() ? "-" : srcStr);
            diff.setTargetValue(tgtStr.isEmpty() ? "-" : tgtStr);
            diff.setDiffType("different");
            diffs.add(diff);
        }
    }
}
