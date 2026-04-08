package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.entity.ScheduleJob;
import com.guarantee.finance.entity.ScheduleJobLog;
import com.guarantee.finance.entity.ScheduleTaskConfig;

import java.util.List;
import java.util.Map;

public interface ScheduleService {

    IPage<ScheduleJob> queryJobs(String jobName, String status, Page<?> page);

    ScheduleJob getJobDetail(Long id);

    Long createJob(ScheduleJob job);

    void updateJob(ScheduleJob job);

    void deleteJob(Long id);

    void changeStatus(Long id, String status);

    void executeOnce(Long id);

    IPage<ScheduleJobLog> queryLogs(Long jobId, Integer status, Page<?> page);

    List<ScheduleTaskConfig> getBuiltInTasks();

    void updateBuiltInTaskConfig(String taskCode, String cronExpression, String status);

    Map<String, Object> getScheduleDashboard();

    void initBuiltInTasks();
}
