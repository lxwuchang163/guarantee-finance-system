package com.guarantee.finance.service;

import com.guarantee.finance.GuaranteeFinanceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GuaranteeFinanceApplication.class)
@ActiveProfiles("test")
@Transactional
public class ScheduleIntegrationTest {

    @Autowired
    private ScheduleService scheduleService;

    @Test
    @DisplayName("定时任务CRUD完整流程")
    public void testScheduleJobCRUD() {
        var job = new com.guarantee.finance.entity.ScheduleJob();
        job.setJobName("测试定时任务");
        job.setJobGroup("TEST_GROUP");
        job.setBeanName("testBean");
        job.setMethodName("testMethod");
        job.setCronExpression("0 0 2 * * ?");
        job.setStatus("0");

        Long id = scheduleService.createJob(job);
        assertNotNull(id);

        var detail = scheduleService.getJobDetail(id);
        assertThat(detail.getJobName()).isEqualTo("测试定时任务");
        assertThat(detail.getStatus()).isEqualTo("0");

        job.setId(id);
        job.setJobName("修改后的任务名");
        scheduleService.updateJob(job);

        var updated = scheduleService.getJobDetail(id);
        assertThat(updated.getJobName()).isEqualTo("修改后的任务名");

        scheduleService.changeStatus(id, "1");
        var activated = scheduleService.getJobDetail(id);
        assertThat(activated.getStatus()).isEqualTo("1");

        scheduleService.deleteJob(id);
        var deleted = scheduleService.getJobDetail(id);
        assertNull(deleted);
    }

    @Test
    @DisplayName("内置任务初始化和查询")
    public void testBuiltInTasks() {
        scheduleService.initBuiltInTasks();
        List<com.guarantee.finance.entity.ScheduleTaskConfig> tasks = scheduleService.getBuiltInTasks();
        assertThat(tasks).hasSize(10);
        assertThat(tasks.stream().anyMatch(t -> t.getTaskCode().equals("SYNC_CUSTOMER_FULL"))).isTrue();
    }

    @Test
    @DisplayName("调度仪表盘数据统计")
    public void testScheduleDashboard() {
        Map<String, Object> dashboard = scheduleService.getScheduleDashboard();
        assertThat(dashboard).containsKeys("totalJobs", "runningJobs", "pausedJobs", "todaySuccess", "todayFail", "recentLogs");
    }

    @Test
    @DisplayName("执行日志查询")
    public void testScheduleLogs() {
        var logs = scheduleService.queryLogs(null, null, 1, 10);
        assertThat(logs).isNotNull();
    }
}
