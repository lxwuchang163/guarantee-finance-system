package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.ProcessDTO;
import com.guarantee.finance.service.ProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审批流程管理")
@RestController
@RequestMapping("/system/process")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    @Operation(summary = "获取流程定义列表")
    @GetMapping("/definition/list")
    public R<List<?>> getDefinitions() {
        return R.ok(processService.getDefinitions());
    }

    @Operation(summary = "获取流程定义详情（含节点）")
    @GetMapping("/definition/{id}")
    public R<?> getDefinitionDetail(@PathVariable Long id) {
        return R.ok(processService.getDefinitionDetail(id));
    }

    @Operation(summary = "新增流程定义")
    @PostMapping("/definition")
    public R<Void> createDefinition(@RequestBody ProcessDTO dto) {
        processService.createDefinition(dto);
        return R.ok();
    }

    @Operation(summary = "修改流程定义")
    @PutMapping("/definition")
    public R<Void> updateDefinition(@RequestBody ProcessDTO dto) {
        processService.updateDefinition(dto);
        return R.ok();
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/definition/{id}")
    public R<Void> deleteDefinition(@PathVariable Long id) {
        processService.deleteDefinition(id);
        return R.ok();
    }

    @Operation(summary = "发起审批")
    @PostMapping("/instance/start")
    public R<Map<String, Object>> startProcess(@RequestBody Map<String, Object> params) {
        return R.ok(processService.startProcess(params));
    }

    @Operation(summary = "审批通过")
    @PutMapping("/instance/approve/{instanceId}")
    public R<Void> approve(@PathVariable Long instanceId, @RequestParam String opinion) {
        processService.approve(instanceId, opinion, "approve");
        return R.ok();
    }

    @Operation(summary = "驳回")
    @PutMapping("/instance/reject/{instanceId}")
    public R<Void> reject(@PathVariable Long instanceId, @RequestParam String opinion) {
        processService.approve(instanceId, opinion, "reject");
        return R.ok();
    }

    @Operation(summary = "撤回")
    @PutMapping("/instance/cancel/{instanceId}")
    public R<Void> cancel(@PathVariable Long instanceId) {
        processService.cancel(instanceId);
        return R.ok();
    }

    @Operation(summary = "我的审批（待我审批）")
    @GetMapping("/instance/myPending")
    public R<List<?>> getMyPending() {
        return R.ok(processService.getMyPending());
    }

    @Operation(summary = "我发起的")
    @GetMapping("/instance/myInitiated")
    public R<List<?>> getMyInitiated() {
        return R.ok(processService.getMyInitiated());
    }
}
