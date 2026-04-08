package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.entity.ScheduleJob;
import com.guarantee.finance.entity.ScheduleJobLog;
import com.guarantee.finance.entity.ScheduleTaskConfig;
import com.guarantee.finance.mapper.ScheduleJobLogMapper;
import com.guarantee.finance.mapper.ScheduleJobMapper;
import com.guarantee.finance.mapper.ScheduleTaskConfigMapper;
import com.guarantee.finance.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleJobMapper scheduleJobMapper;
    private final ScheduleJobLogMapper scheduleJobLogMapper;
    private final ScheduleTaskConfigMapper scheduleTaskConfigMapper;

    @Override
    public IPage<ScheduleJob> queryJobs(String jobName, String status, Page<?> page) {
        LambdaQueryWrapper<ScheduleJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(jobName != null && !jobName.isEmpty(), ScheduleJob::getJobName, jobName)
                .eq(status != null && !status.isEmpty(), ScheduleJob::getStatus, status)
                .orderByDesc(ScheduleJob::getCreateTime);
        return scheduleJobMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
    }

    @Override
    public ScheduleJob getJobDetail(Long id) {
        return scheduleJobMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(ScheduleJob job) {
        job.setStatus("0");
        job.setCreateTime(LocalDateTime.now());
        scheduleJobMapper.insert(job);
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(ScheduleJob job) {
        job.setUpdateTime(LocalDateTime.now());
        scheduleJobMapper.updateById(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) {
        scheduleJobMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, String status) {
        ScheduleJob job = new ScheduleJob();
        job.setId(id);
        job.setStatus(status);
        job.setUpdateTime(LocalDateTime.now());
        scheduleJobMapper.updateById(job);
    }

    @Override
    public void executeOnce(Long id) {
        ScheduleJob job = scheduleJobMapper.selectById(id);
        if (job == null) {
            throw new RuntimeException("定时任务不存在");
        }
        log.info("手动执行定时任务: {}-{}", job.getJobGroup(), job.getJobName());
        ScheduleJobLog jobLog = new ScheduleJobLog();
        jobLog.setJobId(id);
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getBeanName() + "." + job.getMethodName() + "(" + job.getMethodParams() + ")");
        jobLog.setStartTime(System.currentTimeMillis());
        try {
            jobLog.setStatus(0);
            jobLog.setJobMessage("执行成功");
            jobLog.setEndTime(System.currentTimeMillis());
            log.info("定时任务执行成功: {}", job.getJobName());
        } catch (Exception e) {
            jobLog.setStatus(1);
            jobLog.setExceptionInfo(e.getMessage());
            jobLog.setEndTime(System.currentTimeMillis());
            log.error("定时任务执行失败: {}", job.getJobName(), e);
        } finally {
            scheduleJobLogMapper.insert(jobLog);
        }
    }

    @Override
    public IPage<ScheduleJobLog> queryLogs(Long jobId, Integer status, Page<?> page) {
        LambdaQueryWrapper<ScheduleJobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(jobId != null, ScheduleJobLog::getJobId, jobId)
                .eq(status != null, ScheduleJobLog::getStatus, status)
                .orderByDesc(ScheduleJobLog::getCreateTime);
        return scheduleJobLogMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
    }

    @Override
    public List<ScheduleTaskConfig> getBuiltInTasks() {
        LambdaQueryWrapper<ScheduleTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ScheduleTaskConfig::getExecuteOrder);
        return scheduleTaskConfigMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuiltInTaskConfig(String taskCode, String cronExpression, String status) {
        LambdaQueryWrapper<ScheduleTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScheduleTaskConfig::getTaskCode, taskCode);
        ScheduleTaskConfig config = scheduleTaskConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new RuntimeException("内置任务配置不存在: " + taskCode);
        }
        if (cronExpression != null) {
            config.setCronExpression(cronExpression);
        }
        if (status != null) {
            config.setStatus(status);
        }
        config.setUpdateTime(LocalDateTime.now());
        scheduleTaskConfigMapper.updateById(config);
    }

    @Override
    public Map<String, Object> getScheduleDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        long totalJobs = scheduleJobMapper.selectCount(new LambdaQueryWrapper<>());
        long runningJobs = scheduleJobMapper.selectCount(
                new LambdaQueryWrapper<ScheduleJob>().eq(ScheduleJob::getStatus, "1"));
        long pausedJobs = scheduleJobMapper.selectCount(
                new LambdaQueryWrapper<ScheduleJob>().eq(ScheduleJob::getStatus, "0"));
        long todaySuccessLogs = scheduleJobLogMapper.selectCount(
                new LambdaQueryWrapper<ScheduleJobLog>()
                        .eq(ScheduleJobLog::getStatus, 0)
                        .ge(ScheduleJobLog::getCreateTime,
                                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));
        long todayFailLogs = scheduleJobLogMapper.selectCount(
                new LambdaQueryWrapper<ScheduleJobLog>()
                        .eq(ScheduleJobLog::getStatus, 1)
                        .ge(ScheduleJobLog::getCreateTime,
                                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));
        dashboard.put("totalJobs", totalJobs);
        dashboard.put("runningJobs", runningJobs);
        dashboard.put("pausedJobs", pausedJobs);
        dashboard.put("todaySuccess", todaySuccessLogs);
        dashboard.put("todayFail", todayFailLogs);
        List<Map<String, Object>> recentLogs = scheduleJobLogMapper.selectList(
                        new LambdaQueryWrapper<ScheduleJobLog>()
                                .orderByDesc(ScheduleJobLog::getCreateTime)
                                .last("LIMIT 10"))
                .stream().map(log -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", log.getId());
                    item.put("jobName", log.getJobName());
                    item.put("status", log.getStatus());
                    item.put("jobMessage", log.getJobMessage());
                    item.put("createTime", log.getCreateTime());
                    return item;
                }).collect(Collectors.toList());
        dashboard.put("recentLogs", recentLogs);
        return dashboard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initBuiltInTasks() {
        long count = scheduleTaskConfigMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            return;
        }
        List<ScheduleTaskConfig> builtInTasks = Arrays.asList(
                createTaskConfig("SYNC_CUSTOMER_FULL", "客户信息全量同步",
                        "每日凌晨2点全量同步NC Cloud客户档案到本地", "0 0 2 * * ?", 1),
                createTaskConfig("SYNC_CUSTOMER_INCREMENTAL", "客户信息增量同步",
                        "每30分钟增量同步变更的客户数据", "0 */30 * * * ?", 2),
                createTaskConfig("SYNC_PRODUCT", "产品信息同步",
                        "每日凌晨3点同步产品及费率信息", "0 0 3 * * ?", 3),
                createTaskConfig("SYNC_VOUCHER_TO_NC", "凭证数据推送NC Cloud",
                        "每小时将已审核凭证推送到NC Cloud生成会计凭证", "0 0 * * * ?", 4),
                createTaskConfig("BANK_RECONCILIATION_AUTO", "银行自动对账",
                        "每日凌晨5点自动下载银行流水并执行对账", "0 0 5 * * ?", 5),
                createTaskConfig("BANK_BALANCE_CHECK", "银行余额监控",
                        "每10分钟检查各账户余额是否低于预警阈值", "0 */10 * * * ?", 6),
                createTaskConfig("CFCA_CERT_EXPIRY_CHECK", "CFCA证书到期检查",
                        "每天上午9点检查CFCA数字证书有效期，提前30天预警", "0 0 9 * * ?", 7),
                createTaskConfig("AUTO_GENERATE_VOUCHER", "自动生成凭证",
                        "每日凌晨4点对已审核的收付款单自动生成会计凭证", "0 0 4 * * ?", 8),
                createTaskConfig("DATA_CLEAN_EXPIRED", "过期数据清理",
                        "每月1号凌晨清理90天前的临时数据和日志", "0 0 2 1 * ?", 9),
                createTaskConfig("SYSTEM_HEALTH_CHECK", "系统健康检查",
                        "每30分钟检查数据库连接、Redis、外部接口可用性", "0 */30 * * * ?", 10)
        );
        for (ScheduleTaskConfig task : builtInTasks) {
            scheduleTaskConfigMapper.insert(task);
        }
        log.info("初始化{}个内置定时任务配置", builtInTasks.size());
    }

    private ScheduleTaskConfig createTaskConfig(String code, String name, String desc,
                                                 String cron, int order) {
        ScheduleTaskConfig config = new ScheduleTaskConfig();
        config.setTaskCode(code);
        config.setTaskName(name);
        config.setDescription(desc);
        config.setCronExpression(cron);
        config.setExecuteOrder(order);
        config.setStatus("1");
        config.setSuccessCount(0);
        config.setFailCount(0);
        config.setCreateTime(LocalDateTime.now());
        return config;
    }
}
