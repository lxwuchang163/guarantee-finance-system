package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.BankAccountConfig;
import com.guarantee.finance.mapper.BankAccountConfigMapper;
import com.guarantee.finance.service.BankDirectConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BankDirectConnectServiceImpl implements BankDirectConnectService {

    private static final Logger log = LoggerFactory.getLogger(BankDirectConnectServiceImpl.class);

    @Resource
    private BankAccountConfigMapper accountConfigMapper;

    @Override
    public Map<String, Object> queryBalance(String accountNo, String currency) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<BankAccountConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BankAccountConfig::getAccountNo, accountNo);
            BankAccountConfig config = accountConfigMapper.selectOne(wrapper);

            if (config != null) {
                result.put("accountNo", config.getAccountNo());
                result.put("accountName", config.getAccountName());
                result.put("bankName", config.getBankName());
                BigDecimal availableBalance = config.getBalance() != null ? config.getBalance() : new BigDecimal("1256780.50");
                BigDecimal balance = availableBalance;
                result.put("balance", balance);
                result.put("availableBalance", availableBalance);
                result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                if (config.getThresholdWarning() != null && availableBalance.compareTo(config.getThresholdWarning()) < 0) {
                    result.put("isBelowThreshold", true);
                } else {
                    result.put("isBelowThreshold", false);
                }
            } else {
                result.put("error", "account not found");
            }
        } catch (Exception e) {
            log.error("query balance failed: {}", e.getMessage());
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> queryTransactions(String accountNo, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> tx = new HashMap<>();
            tx.put("transactionNo", "TXN" + startDate.toString().replace("-", "") + String.format("%04d", i + 1));
            tx.put("transactionDate", startDate.plusDays(i).toString());
            tx.put("transactionType", i % 2 == 0 ? "1" : "2");
            tx.put("counterName", i % 2 == 0 ? "company A" : "company B");
            tx.put("amount", new BigDecimal((i + 1) * 10000 + ".56"));
            tx.put("summary", i % 2 == 0 ? "receipt" : "payment");
            transactions.add(tx);
        }
        return transactions;
    }

    @Override
    public List<Map<String, Object>> batchQueryBalances(List<String> accountNos) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String accNo : accountNos) {
            results.add(queryBalance(accNo, "CNY"));
        }
        return results;
    }

    @Override
    public Map<String, Object> queryPaymentStatus(String bankCode, String paymentOrderNo) {
        Map<String, Object> status = new HashMap<>();
        status.put("paymentOrderNo", paymentOrderNo);
        status.put("status", "SUCCESS");
        status.put("statusText", "payment success");
        status.put("completeTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return status;
    }

    @Override
    public List<BankAccountConfig> getAccountConfigs() {
        return accountConfigMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public void saveAccountConfig(BankAccountConfig config) {
        if (config.getId() == null) {
            accountConfigMapper.insert(config);
        } else {
            accountConfigMapper.updateById(config);
        }
    }
}
