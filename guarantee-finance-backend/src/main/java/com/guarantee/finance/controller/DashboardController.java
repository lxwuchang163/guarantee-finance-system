package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.entity.TodoItem;
import com.guarantee.finance.entity.Notice;
import com.guarantee.finance.entity.IncomeExpenseData;
import com.guarantee.finance.entity.BusinessTypeData;
import com.guarantee.finance.service.DashboardService;
import com.guarantee.finance.service.TodoService;
import com.guarantee.finance.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "仪表盘管理")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TodoService todoService;

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return R.ok(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return R.fail("获取仪表盘统计数据失败");
        }
    }

    @Operation(summary = "获取待办事项列表")
    @GetMapping("/todos")
    public R<List<TodoItem>> getTodoList() {
        try {
            List<TodoItem> todoList = todoService.getTodoList();
            return R.ok(todoList);
        } catch (Exception e) {
            log.error("获取待办事项列表失败", e);
            return R.fail("获取待办事项列表失败");
        }
    }

    @Operation(summary = "处理待办事项")
    @PostMapping("/todos/{id}/process")
    public R<Void> processTodo(@PathVariable Long id) {
        try {
            boolean result = todoService.processTodo(id);
            if (result) {
                return R.ok();
            } else {
                return R.fail("处理待办事项失败");
            }
        } catch (Exception e) {
            log.error("处理待办事项失败", e);
            return R.fail("处理待办事项失败");
        }
    }

    @Operation(summary = "获取待办事项详情")
    @GetMapping("/todos/{id}")
    public R<TodoItem> getTodoDetail(@PathVariable Long id) {
        try {
            TodoItem todo = todoService.getTodoDetail(id);
            if (todo != null) {
                return R.ok(todo);
            } else {
                return R.fail("待办事项不存在");
            }
        } catch (Exception e) {
            log.error("获取待办事项详情失败", e);
            return R.fail("获取待办事项详情失败");
        }
    }

    @Operation(summary = "获取系统公告列表")
    @GetMapping("/notices")
    public R<List<Notice>> getNoticeList() {
        try {
            List<Notice> noticeList = noticeService.getNoticeList();
            return R.ok(noticeList);
        } catch (Exception e) {
            log.error("获取系统公告列表失败", e);
            return R.fail("获取系统公告列表失败");
        }
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/notices/{id}")
    public R<Notice> getNoticeDetail(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getNoticeDetail(id);
            if (notice != null) {
                return R.ok(notice);
            } else {
                return R.fail("公告不存在");
            }
        } catch (Exception e) {
            log.error("获取公告详情失败", e);
            return R.fail("获取公告详情失败");
        }
    }

    @Operation(summary = "获取收支趋势数据")
    @GetMapping("/business/income-expense")
    public R<List<IncomeExpenseData>> getIncomeExpenseData() {
        try {
            List<IncomeExpenseData> dataList = dashboardService.getIncomeExpenseData();
            return R.ok(dataList);
        } catch (Exception e) {
            log.error("获取收支趋势数据失败", e);
            return R.fail("获取收支趋势数据失败");
        }
    }

    @Operation(summary = "获取业务类型分布数据")
    @GetMapping("/business/type-distribution")
    public R<List<BusinessTypeData>> getBusinessTypeData() {
        try {
            List<BusinessTypeData> dataList = dashboardService.getBusinessTypeData();
            return R.ok(dataList);
        } catch (Exception e) {
            log.error("获取业务类型分布数据失败", e);
            return R.fail("获取业务类型分布数据失败");
        }
    }
}
