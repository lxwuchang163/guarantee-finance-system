package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.ScheduleJob;
import com.guarantee.finance.entity.ScheduleJobLog;
import com.guarantee.finance.entity.ScheduleTaskConfig;
import com.guarantee.finance.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "获取调度中心仪表盘")
    @GetMapping("/dashboard")
    public R<Map<String, Object>> getDashboard() {
        return R.ok(scheduleService.getScheduleDashboard());
    }

    @Operation(summary = "分页查询定时任务列表")
    @GetMapping("/jobs")
    public R<IPage<ScheduleJob>> listJobs2(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<?> page = Page.of(current, size);
        return R.ok(scheduleService.queryJobs(jobName, status, page));
    }

    @Operation(summary = "分页查询定时任务列表")
    @GetMapping("/job/list")
    public R<IPage<ScheduleJob>> listJobs(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<?> page = Page.of(current, size);
        return R.ok(scheduleService.queryJobs(jobName, status, page));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/job/{id}")
    public R<ScheduleJob> getJobDetail(@PathVariable Long id) {
        return R.ok(scheduleService.getJobDetail(id));
    }

    @Operation(summary = "创建定时任务")
    @PostMapping("/job")
    public R<Long> createJob(@RequestBody ScheduleJob job) {
        Long id = scheduleService.createJob(job);
        return R.ok(id);
    }

    @Operation(summary = "更新定时任务")
    @PutMapping("/job")
    public R<Void> updateJob(@RequestBody ScheduleJob job) {
        scheduleService.updateJob(job);
        return R.ok();
    }

    @Operation(summary = "删除定时任务")
    @DeleteMapping("/job/{id}")
    public R<Void> deleteJob(@PathVariable Long id) {
        scheduleService.deleteJob(id);
        return R.ok();
    }

    @Operation(summary = "修改任务状态-暂停/恢复")
    @PutMapping("/job/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        scheduleService.changeStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "立即执行一次")
    @PostMapping("/job/{id}/execute")
    public R<Void> executeOnce(@PathVariable Long id) {
        scheduleService.executeOnce(id);
        return R.ok();
    }

    @Operation(summary = "查询执行日志列表")
    @GetMapping("/log/list")
    public R<IPage<ScheduleJobLog>> listLogs(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<?> page = Page.of(current, size);
        return R.ok(scheduleService.queryLogs(jobId, status, page));
    }

    @Operation(summary = "获取内置任务配置列表")
    @GetMapping("/built-in/tasks")
    public R<List<ScheduleTaskConfig>> getBuiltInTasks() {
        return R.ok(scheduleService.getBuiltInTasks());
    }

    @Operation(summary = "更新内置任务配置")
    @PutMapping("/built-in/task/{taskCode}/config")
    public R<Void> updateBuiltInTaskConfig(
            @PathVariable String taskCode,
            @RequestParam(required = false) String cronExpression,
            @RequestParam(required = false) String status) {
        scheduleService.updateBuiltInTaskConfig(taskCode, cronExpression, status);
        return R.ok();
    }

    @Operation(summary = "初始化内置任务")
    @PostMapping("/built-in/init")
    public R<Void> initBuiltInTasks() {
        scheduleService.initBuiltInTasks();
        return R.ok();
    }
}
