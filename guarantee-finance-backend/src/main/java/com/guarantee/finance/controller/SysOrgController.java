package com.guarantee.finance.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.OrgDTO;
import com.guarantee.finance.dto.OrgQueryDTO;
import com.guarantee.finance.entity.SysOrg;
import com.guarantee.finance.service.SysOrgService;
import com.guarantee.finance.vo.OrgTreeVO;
import com.guarantee.finance.vo.OrgVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "机构管理")
@RestController
@RequestMapping("/system/org")
public class SysOrgController {

    @Autowired
    private SysOrgService sysOrgService;

    @Operation(summary = "分页查询机构列表")
    @GetMapping("/page")
    public R<PageResult<OrgVO>> page(OrgQueryDTO queryDTO,
                                      @RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size) {
        IPage<OrgVO> page = sysOrgService.queryPage(queryDTO, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "获取机构树")
    @GetMapping("/tree")
    public R<List<OrgTreeVO>> getTree() {
        List<OrgTreeVO> tree = sysOrgService.getOrgTree();
        return R.ok(tree);
    }

    @Operation(summary = "获取机构详情")
    @GetMapping("/{id}")
    public R<OrgVO> detail(@PathVariable Long id) {
        OrgVO vo = sysOrgService.getOrgDetail(id);
        return R.ok(vo);
    }

    @Operation(summary = "新增机构")
    @PostMapping
    public R<Void> add(@Valid @RequestBody OrgDTO dto) {
        SysOrg org = new SysOrg();
        BeanUtils.copyProperties(dto, org);
        if (org.getParentId() == null) {
            org.setParentId(0L);
        }
        if (org.getStatus() == null) {
            org.setStatus(1);
        }
        if (org.getSortOrder() == null) {
            org.setSortOrder(0);
        }
        sysOrgService.createOrg(org);
        return R.ok();
    }

    @Operation(summary = "修改机构")
    @PutMapping
    public R<Void> update(@Valid @RequestBody OrgDTO dto) {
        if (dto.getId() == null) {
            return R.fail("机构ID不能为空");
        }
        SysOrg org = new SysOrg();
        BeanUtils.copyProperties(dto, org);
        sysOrgService.updateOrg(org);
        return R.ok();
    }

    @Operation(summary = "删除机构")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = sysOrgService.deleteOrg(id);
        if (!success) {
            return R.fail("该机构下存在子机构或关联用户，不允许删除");
        }
        return R.ok();
    }

    @Operation(summary = "更新机构状态")
    @PutMapping("/status/{id}")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysOrgService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "移动机构节点（拖拽排序）")
    @PutMapping("/move")
    public R<Void> moveNode(@RequestParam Long id,
                            @RequestParam Long targetParentId,
                            @RequestParam(defaultValue = "0") Integer sortOrder) {
        sysOrgService.moveNode(id, targetParentId, sortOrder);
        return R.ok();
    }

    @Operation(summary = "获取子机构列表")
    @GetMapping("/children/{parentId}")
    public R<List<OrgVO>> getChildren(@PathVariable Long parentId) {
        List<OrgVO> children = sysOrgService.getChildren(parentId);
        return R.ok(children);
    }

    @Operation(summary = "导入机构数据")
    @PostMapping("/import")
    public R<String> importOrgs(@RequestParam("file") MultipartFile file) throws IOException {
        String result = sysOrgService.importOrgs(file);
        return R.ok(result);
    }

    @Operation(summary = "导出机构数据")
    @GetMapping("/export")
    public void exportOrgs(OrgQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        sysOrgService.exportOrgs(queryDTO, response);
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        sysOrgService.downloadTemplate(response);
    }

    @Operation(summary = "检查机构编码唯一性")
    @GetMapping("/checkCode")
    public R<Boolean> checkCode(@RequestParam String code, @RequestParam(required = false) Long id) {
        boolean exists = sysOrgService.checkCodeUnique(code, id);
        return R.ok(!exists);
    }
}
