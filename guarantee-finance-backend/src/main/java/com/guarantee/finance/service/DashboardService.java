package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.DashboardStats;
import com.guarantee.finance.entity.IncomeExpenseData;
import com.guarantee.finance.entity.BusinessTypeData;

import java.util.List;
import java.util.Map;

public interface DashboardService extends IService<DashboardStats> {

    /**
     * 获取仪表盘统计数据
     * @return 统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取收支趋势数据
     * @return 收支趋势数据列表
     */
    List<IncomeExpenseData> getIncomeExpenseData();

    /**
     * 获取业务类型分布数据
     * @return 业务类型分布数据列表
     */
    List<BusinessTypeData> getBusinessTypeData();
}
