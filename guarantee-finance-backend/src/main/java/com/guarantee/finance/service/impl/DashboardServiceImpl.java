package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.DashboardStats;
import com.guarantee.finance.entity.IncomeExpenseData;
import com.guarantee.finance.entity.BusinessTypeData;
import com.guarantee.finance.mapper.DashboardStatsMapper;
import com.guarantee.finance.mapper.IncomeExpenseDataMapper;
import com.guarantee.finance.mapper.BusinessTypeDataMapper;
import com.guarantee.finance.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl extends ServiceImpl<DashboardStatsMapper, DashboardStats> implements DashboardService {

    @jakarta.annotation.Resource
    private IncomeExpenseDataMapper incomeExpenseDataMapper;

    @jakarta.annotation.Resource
    private BusinessTypeDataMapper businessTypeDataMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        // 模拟数据，实际项目中应该从数据库查询
        Map<String, Object> stats = new HashMap<>();
        
        // 今日收款
        BigDecimal todayIncome = new BigDecimal("125680.00");
        // 今日付款
        BigDecimal todayExpense = new BigDecimal("89320.00");
        // 待审核单据
        int pendingDocuments = 23;
        // 本月对账
        int monthlyReconciliation = 156;
        // 对账完成率
        BigDecimal reconciliationRate = new BigDecimal("98.00");
        
        // 环比变化率
        Map<String, Object> changeRates = new HashMap<>();
        changeRates.put("todayIncome", "+12.5%");
        changeRates.put("todayExpense", "-3.2%");
        changeRates.put("pendingDocuments", "+5");
        changeRates.put("monthlyReconciliation", "98%");
        
        stats.put("todayIncome", todayIncome);
        stats.put("todayExpense", todayExpense);
        stats.put("pendingDocuments", pendingDocuments);
        stats.put("monthlyReconciliation", monthlyReconciliation);
        stats.put("reconciliationRate", reconciliationRate);
        stats.put("changeRates", changeRates);
        
        return stats;
    }

    @Override
    public List<IncomeExpenseData> getIncomeExpenseData() {
        // 模拟数据，实际项目中应该从数据库查询
        List<IncomeExpenseData> dataList = new ArrayList<>();
        
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月"};
        BigDecimal[] incomes = {new BigDecimal("320000"), new BigDecimal("332000"), new BigDecimal("301000"), 
                              new BigDecimal("334000"), new BigDecimal("390000"), new BigDecimal("330000")};
        BigDecimal[] expenses = {new BigDecimal("280000"), new BigDecimal("292000"), new BigDecimal("251000"), 
                               new BigDecimal("294000"), new BigDecimal("330000"), new BigDecimal("280000")};
        
        for (int i = 0; i < months.length; i++) {
            IncomeExpenseData data = new IncomeExpenseData();
            data.setMonth(months[i]);
            data.setIncome(incomes[i]);
            data.setExpense(expenses[i]);
            dataList.add(data);
        }
        
        return dataList;
    }

    @Override
    public List<BusinessTypeData> getBusinessTypeData() {
        // 模拟数据，实际项目中应该从数据库查询
        List<BusinessTypeData> dataList = new ArrayList<>();
        
        String[] types = {"担保业务", "贷款业务", "理财业务", "其他业务"};
        BigDecimal[] values = {new BigDecimal("45"), new BigDecimal("30"), new BigDecimal("20"), new BigDecimal("5")};
        BigDecimal[] percentages = {new BigDecimal("45"), new BigDecimal("30"), new BigDecimal("20"), new BigDecimal("5")};
        
        for (int i = 0; i < types.length; i++) {
            BusinessTypeData data = new BusinessTypeData();
            data.setType(types[i]);
            data.setValue(values[i]);
            data.setPercentage(percentages[i]);
            dataList.add(data);
        }
        
        return dataList;
    }
}
