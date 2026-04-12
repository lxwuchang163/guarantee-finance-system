package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.*;
import com.guarantee.finance.mapper.*;
import com.guarantee.finance.service.TodoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoServiceImpl extends ServiceImpl<TodoItemMapper, TodoItem> implements TodoService {

    @Resource
    private FinReceiptMapper finReceiptMapper;

    @Resource
    private FinPaymentMapper finPaymentMapper;

    @Resource
    private BankReconciliationMapper bankReconciliationMapper;

    @Resource
    private BankAccountConfigMapper bankAccountConfigMapper;

    @Resource
    private AccVoucherMapper accVoucherMapper;

    @Override
    public List<TodoItem> getTodoList() {
        List<TodoItem> todoList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<FinReceipt> pendingReceipts = finReceiptMapper.selectList(
                new LambdaQueryWrapper<FinReceipt>().eq(FinReceipt::getDeleted, 0).in(FinReceipt::getStatus, 0, 1)
                        .orderByDesc(FinReceipt::getCreateTime).last("LIMIT 5"));
        for (FinReceipt r : pendingReceipts) {
            TodoItem item = new TodoItem();
            item.setId(r.getId());
            item.setTitle("收款单 " + r.getReceiptNo() + (r.getStatus() == 0 ? " 待提交" : " 待审核"));
            item.setType("收款");
            item.setPriority(r.getStatus() == 1 ? "high" : "medium");
            item.setStatus("pending");
            item.setDescription("收款单 " + r.getReceiptNo() + "，金额：" + r.getAmount() + "，客户：" + r.getCustomerName());
            item.setDisplayTime(r.getCreateTime() != null ? r.getCreateTime().format(fmt) : "");
            todoList.add(item);
        }

        List<FinPayment> pendingPayments = finPaymentMapper.selectList(
                new LambdaQueryWrapper<FinPayment>().eq(FinPayment::getDeleted, 0).in(FinPayment::getStatus, 0, 1)
                        .orderByDesc(FinPayment::getCreateTime).last("LIMIT 5"));
        for (FinPayment p : pendingPayments) {
            TodoItem item = new TodoItem();
            item.setId(p.getId());
            item.setTitle("付款单 " + p.getPaymentNo() + (p.getStatus() == 0 ? " 待提交" : " 待审核"));
            item.setType("付款");
            item.setPriority(p.getStatus() == 1 ? "high" : "medium");
            item.setStatus("pending");
            item.setDescription("付款单 " + p.getPaymentNo() + "，金额：" + p.getAmount() + "，客户：" + p.getCustomerName());
            item.setDisplayTime(p.getCreateTime() != null ? p.getCreateTime().format(fmt) : "");
            todoList.add(item);
        }

        List<BankReconciliation> pendingRecon = bankReconciliationMapper.selectList(
                new LambdaQueryWrapper<BankReconciliation>().eq(BankReconciliation::getDeleted, 0).eq(BankReconciliation::getStatus, 0)
                        .orderByDesc(BankReconciliation::getCreateTime).last("LIMIT 5"));
        for (BankReconciliation b : pendingRecon) {
            TodoItem item = new TodoItem();
            item.setId(b.getId());
            item.setTitle("对账单 " + b.getAccountNo() + " 待对账");
            item.setType("对账");
            item.setPriority("medium");
            item.setStatus("pending");
            item.setDescription("账户 " + b.getAccountNo() + " 对账日期：" + b.getReconciliationDate());
            item.setDisplayTime(b.getCreateTime() != null ? b.getCreateTime().format(fmt) : "");
            todoList.add(item);
        }

        List<AccVoucher> pendingVouchers = accVoucherMapper.selectList(
                new LambdaQueryWrapper<AccVoucher>().eq(AccVoucher::getDeleted, 0).eq(AccVoucher::getStatus, 1)
                        .orderByDesc(AccVoucher::getCreateTime).last("LIMIT 5"));
        for (AccVoucher v : pendingVouchers) {
            TodoItem item = new TodoItem();
            item.setId(v.getId());
            item.setTitle("凭证 " + v.getVoucherNo() + " 待审核");
            item.setType("凭证");
            item.setPriority("high");
            item.setStatus("pending");
            item.setDescription("凭证 " + v.getVoucherNo() + "，日期：" + v.getVoucherDate());
            item.setDisplayTime(v.getCreateTime() != null ? v.getCreateTime().format(fmt) : "");
            todoList.add(item);
        }

        List<BankAccountConfig> bankConfigs = bankAccountConfigMapper.selectList(
                new LambdaQueryWrapper<BankAccountConfig>().eq(BankAccountConfig::getDeleted, 0).eq(BankAccountConfig::getApiStatus, 0)
                        .orderByDesc(BankAccountConfig::getCreateTime).last("LIMIT 5"));
        for (BankAccountConfig bc : bankConfigs) {
            TodoItem item = new TodoItem();
            item.setId(bc.getId());
            item.setTitle("银企直连 " + bc.getAccountNo() + " 待连接");
            item.setType("银企直连");
            item.setPriority("low");
            item.setStatus("pending");
            item.setDescription("账户 " + bc.getAccountNo() + "（" + bc.getAccountName() + "）银企直连未连接");
            item.setDisplayTime(bc.getCreateTime() != null ? bc.getCreateTime().format(fmt) : "");
            todoList.add(item);
        }

        todoList.sort((a, b) -> {
            int pa = "high".equals(a.getPriority()) ? 0 : "medium".equals(a.getPriority()) ? 1 : 2;
            int pb = "high".equals(b.getPriority()) ? 0 : "medium".equals(b.getPriority()) ? 1 : 2;
            return pa - pb;
        });

        return todoList;
    }

    @Override
    public boolean processTodo(Long id) {
        TodoItem todo = getById(id);
        if (todo != null) {
            todo.setStatus("done");
            todo.setUpdateTime(LocalDateTime.now());
            return updateById(todo);
        }
        return false;
    }

    @Override
    public TodoItem getTodoDetail(Long id) {
        return getById(id);
    }
}
