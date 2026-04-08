package com.guarantee.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.OrgQueryDTO;
import com.guarantee.finance.entity.SysOrg;
import com.guarantee.finance.mapper.SysOrgMapper;
import com.guarantee.finance.mapper.SysUserMapper;
import com.guarantee.finance.service.SysOrgService;
import com.guarantee.finance.vo.OrgTreeVO;
import com.guarantee.finance.vo.OrgVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @jakarta.annotation.Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<OrgTreeVO> getOrgTree() {
        List<SysOrg> allOrgs = list(new LambdaQueryWrapper<SysOrg>()
                .orderByAsc(SysOrg::getSortOrder)
                .orderByDesc(SysOrg::getCreateTime));

        return buildTree(allOrgs, 0L);
    }

    @Override
    public IPage<OrgVO> queryPage(OrgQueryDTO queryDTO, Page<OrgVO> page) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getOrgName())) {
            wrapper.like(SysOrg::getOrgName, queryDTO.getOrgName());
        }
        if (StrUtil.isNotBlank(queryDTO.getOrgCode())) {
            wrapper.like(SysOrg::getOrgCode, queryDTO.getOrgCode());
        }
        if (queryDTO.getParentId() != null) {
            wrapper.eq(SysOrg::getParentId, queryDTO.getParentId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysOrg::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getLevel() != null) {
            wrapper.eq(SysOrg::getLevel, queryDTO.getLevel());
        }
        wrapper.orderByAsc(SysOrg::getSortOrder);
        wrapper.orderByDesc(SysOrg::getCreateTime);

        IPage<SysOrg> orgPage = page(page.convert(p -> new SysOrg()), wrapper);

        IPage<OrgVO> voPage = new Page<>(orgPage.getCurrent(), orgPage.getSize(), orgPage.getTotal());
        List<OrgVO> voList = convertToVOList(orgPage.getRecords());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public OrgVO getOrgDetail(Long id) {
        SysOrg org = getById(id);
        if (org == null) {
            return null;
        }
        OrgVO vo = new OrgVO();
        BeanUtils.copyProperties(org, vo);

        if (org.getParentId() != null && org.getParentId() > 0) {
            SysOrg parent = getById(org.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getOrgName());
            }
        }

        List<OrgVO> children = getChildren(id);
        vo.setChildren(children);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrg(SysOrg org) {
        boolean codeExists = checkCodeUnique(org.getOrgCode(), null);
        if (codeExists) {
            throw new RuntimeException("机构编码已存在: " + org.getOrgCode());
        }

        if (org.getParentId() == null || org.getParentId() == 0L) {
            org.setLevel(1);
            org.setParentId(0L);
        } else {
            SysOrg parent = getById(org.getParentId());
            if (parent != null) {
                org.setLevel(parent.getLevel() + 1);
            } else {
                throw new RuntimeException("父级机构不存在");
            }
        }

        save(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrg(SysOrg org) {
        SysOrg existing = getById(org.getId());
        if (existing == null) {
            throw new RuntimeException("机构不存在");
        }

        if (!existing.getOrgCode().equals(org.getOrgCode())) {
            boolean codeExists = checkCodeUnique(org.getOrgCode(), org.getId());
            if (codeExists) {
                throw new RuntimeException("机构编码已存在: " + org.getOrgCode());
            }
        }

        if (org.getParentId() != null && !org.getParentId().equals(existing.getParentId())) {
            if (org.getParentId().equals(org.getId())) {
                throw new RuntimeException("不能将自己设为父级机构");
            }

            if (isChildOf(org.getId(), org.getParentId())) {
                throw new RuntimeException("不能将父级机构移动到其子机构下");
            }

            if (org.getParentId() > 0) {
                SysOrg parent = getById(org.getParentId());
                if (parent != null) {
                    org.setLevel(parent.getLevel() + 1);
                }
            } else {
                org.setLevel(1);
            }
        }

        updateById(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrg(Long id) {
        LambdaQueryWrapper<SysOrg> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysOrg::getParentId, id);
        Long childCount = count(childWrapper);
        if (childCount > 0) {
            return false;
        }

        LambdaQueryWrapper<com.guarantee.finance.entity.SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(com.guarantee.finance.entity.SysUser::getOrgId, id);
        Long userCount = sysUserMapper.selectCount(userWrapper);
        if (userCount > 0) {
            return false;
        }

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysOrg org = getById(id);
        if (org == null) {
            throw new RuntimeException("机构不存在");
        }

        org.setStatus(status);
        updateById(org);

        if (status == 0) {
            cascadeDisableChildren(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveNode(Long id, Long targetParentId, Integer sortOrder) {
        SysOrg org = getById(id);
        if (org == null) {
            throw new RuntimeException("机构不存在");
        }

        if (targetParentId.equals(id)) {
            throw new RuntimeException("不能将节点移动到自己下面");
        }

        if (isChildOf(id, targetParentId)) {
            throw new RuntimeException("不能将父节点移动到子节点下");
        }

        org.setParentId(targetParentId);
        org.setSortOrder(sortOrder);

        if (targetParentId > 0) {
            SysOrg parent = getById(targetParentId);
            if (parent != null) {
                org.setLevel(parent.getLevel() + 1);
            }
        } else {
            org.setLevel(1);
        }

        updateById(org);
    }

    @Override
    public List<OrgVO> getChildren(Long parentId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getParentId, parentId);
        wrapper.orderByAsc(SysOrg::getSortOrder);
        List<SysOrg> children = list(wrapper);
        return convertToVOList(children);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importOrgs(MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

        List<Map<String, Object>> dataList = reader.readAll();

        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (int i = 1; i < dataList.size(); i++) {
            try {
                Map<String, Object> row = dataList.get(i);
                String orgName = String.valueOf(row.getOrDefault(0, ""));
                String orgCode = String.valueOf(row.getOrDefault(1, ""));
                String parentCode = String.valueOf(row.getOrDefault(2, ""));
                String leader = String.valueOf(row.getOrDefault(3, ""));
                String phone = String.valueOf(row.getOrDefault(4, ""));
                String address = String.valueOf(row.getOrDefault(5, ""));

                if (StrUtil.isBlank(orgName) || StrUtil.isBlank(orgCode)) {
                    failCount++;
                    failMsg.append(String.format("第%d行: 机构名称和编码不能为空; ", i + 1));
                    continue;
                }

                boolean codeExists = checkCodeUnique(orgCode.trim(), null);
                if (codeExists) {
                    failCount++;
                    failMsg.append(String.format("第%d行: 机构编码%s已存在; ", i + 1, orgCode));
                    continue;
                }

                SysOrg org = new SysOrg();
                org.setOrgName(orgName.trim());
                org.setOrgCode(orgCode.trim());
                org.setLeader(StrUtil.isBlank(leader) ? null : leader.trim());
                org.setPhone(StrUtil.isBlank(phone) ? null : phone.trim());
                org.setAddress(StrUtil.isBlank(address) ? null : address.trim());
                org.setStatus(1);
                org.setSortOrder(0);

                if (StrUtil.isNotBlank(parentCode) && !"null".equals(parentCode)) {
                    LambdaQueryWrapper<SysOrg> parentWrapper = new LambdaQueryWrapper<>();
                    parentWrapper.eq(SysOrg::getOrgCode, parentCode.trim());
                    SysOrg parent = getOne(parentWrapper);
                    if (parent != null) {
                        org.setParentId(parent.getId());
                        org.setLevel(parent.getLevel() + 1);
                    } else {
                        org.setParentId(0L);
                        org.setLevel(1);
                    }
                } else {
                    org.setParentId(0L);
                    org.setLevel(1);
                }

                save(org);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failMsg.append(String.format("第%d行: %s; ", i + 1, e.getMessage()));
            }
        }

        return String.format("导入完成！成功：%d条，失败：%d条。%s",
                successCount, failCount, failMsg.length() > 0 ? "失败原因：" + failMsg : "");
    }

    @Override
    public void exportOrgs(OrgQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getOrgName())) {
            wrapper.like(SysOrg::getOrgName, queryDTO.getOrgName());
        }
        if (StrUtil.isNotBlank(queryDTO.getOrgCode())) {
            wrapper.like(SysOrg::getOrgCode, queryDTO.getOrgCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysOrg::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByAsc(SysOrg::getSortOrder);

        List<SysOrg> list = list(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("机构数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ExcelWriter writer = ExcelUtil.getWriter(true);

        writer.addHeaderAlias("orgName", "机构名称");
        writer.addHeaderAlias("orgCode", "机构编码");
        writer.addHeaderAlias("parentName", "上级机构");
        writer.addHeaderAlias("leader", "负责人");
        writer.addHeaderAlias("phone", "联系电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("statusName", "状态");

        List<Map<String, Object>> rows = list.stream().map(org -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orgName", org.getOrgName());
            row.put("orgCode", org.getOrgCode());

            if (org.getParentId() != null && org.getParentId() > 0) {
                SysOrg parent = getById(org.getParentId());
                row.put("parentName", parent != null ? parent.getOrgName() : "");
            } else {
                row.put("parentName", "");
            }

            row.put("leader", org.getLeader());
            row.put("phone", org.getPhone());
            row.put("address", org.getAddress());
            row.put("statusName", org.getStatus() != null && org.getStatus() == 1 ? "启用" : "禁用");
            return row;
        }).collect(Collectors.toList());

        writer.write(rows, true);
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("机构导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ExcelWriter writer = ExcelUtil.getWriter(true);

        writer.addHeaderAlias("orgName", "机构名称*");
        writer.addHeaderAlias("orgCode", "机构编码*");
        writer.addHeaderAlias("parentCode", "上级机构编码");
        writer.addHeaderAlias("leader", "负责人");
        writer.addHeaderAlias("phone", "联系电话");
        writer.addHeaderAlias("address", "地址");

        List<Map<String, Object>> templateData = new ArrayList<>();
        Map<String, Object> exampleRow = new LinkedHashMap<>();
        exampleRow.put("orgName", "示例：担保集团总公司");
        exampleRow.put("orgCode", "示例：GROUP001");
        exampleRow.put("parentCode", "示例：（留空表示顶级机构）");
        exampleRow.put("leader", "示例：张三");
        exampleRow.put("phone", "示例：13800138000");
        exampleRow.put("address", "示例：北京市朝阳区xxx路xx号");
        templateData.add(exampleRow);

        writer.write(templateData, true);
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }

    @Override
    public boolean checkCodeUnique(String code, Long id) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getOrgCode, code);
        if (id != null) {
            wrapper.ne(SysOrg::getId, id);
        }
        return count(wrapper) > 0;
    }

    private List<OrgTreeVO> buildTree(List<SysOrg> allOrgs, Long parentId) {
        List<OrgTreeVO> tree = new ArrayList<>();

        for (SysOrg org : allOrgs) {
            if ((parentId == 0L && (org.getParentId() == null || org.getParentId() == 0L))
                    || (org.getParentId() != null && org.getParentId().equals(parentId))) {
                OrgTreeVO node = new OrgTreeVO();
                node.setId(org.getId());
                node.setLabel(org.getOrgName());
                node.setOrgCode(org.getOrgCode());
                node.setLevel(org.getLevel());
                node.setStatus(org.getStatus());
                node.setChildren(buildTree(allOrgs, org.getId()));
                tree.add(node);
            }
        }

        return tree;
    }

    private boolean isChildOf(Long parentId, Long childId) {
        if (parentId.equals(childId)) {
            return true;
        }

        SysOrg child = getById(childId);
        if (child == null || child.getParentId() == null || child.getParentId() == 0L) {
            return false;
        }

        return isChildOf(parentId, child.getParentId());
    }

    private void cascadeDisableChildren(Long parentId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getParentId, parentId);
        List<SysOrg> children = list(wrapper);

        for (SysOrg child : children) {
            child.setStatus(0);
            updateById(child);
            cascadeDisableChildren(child.getId());
        }
    }

    private List<OrgVO> convertToVOList(List<SysOrg> orgList) {
        if (CollUtil.isEmpty(orgList)) {
            return new ArrayList<>();
        }

        return orgList.stream().map(org -> {
            OrgVO vo = new OrgVO();
            BeanUtils.copyProperties(org, vo);

            if (org.getParentId() != null && org.getParentId() > 0) {
                SysOrg parent = getById(org.getParentId());
                if (parent != null) {
                    vo.setParentName(parent.getOrgName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
