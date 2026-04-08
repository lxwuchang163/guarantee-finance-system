package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.entity.BizCustomer;
import com.guarantee.finance.mapper.BizCustomerMapper;
import com.guarantee.finance.service.BizCustomerService;
import com.guarantee.finance.vo.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizCustomerServiceImpl extends ServiceImpl<BizCustomerMapper, BizCustomer> implements BizCustomerService {

    @Override
    public IPage<CustomerVO> queryPage(String keyword, Integer customerType, Integer status, Page<?> page) {
        LambdaQueryWrapper<BizCustomer> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(BizCustomer::getCustomerName, keyword)
                    .or().like(BizCustomer::getCustomerCode, keyword)
                    .or().like(BizCustomer::getContactPhone, keyword));
        }
        if (customerType != null) {
            wrapper.eq(BizCustomer::getCustomerType, customerType);
        }
        if (status != null) {
            wrapper.eq(BizCustomer::getStatus, status);
        }
        wrapper.orderByDesc(BizCustomer::getCreateTime);

        IPage<BizCustomer> customerPage = page(page.convert(p -> new BizCustomer()), wrapper);
        IPage<CustomerVO> voPage = new Page<>(customerPage.getCurrent(), customerPage.getSize(), customerPage.getTotal());
        List<CustomerVO> voList = customerPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CustomerVO getDetail(Long id) {
        BizCustomer customer = getById(id);
        return customer != null ? convertToVO(customer) : null;
    }

    @Override
    public void syncAll() {
        // 模拟全量同步：从业务系统拉取全部客户数据
        // 实际项目中这里会调用业务系统的API接口
        log.info("开始执行客户信息全量同步...");

        int successCount = 0;
        int failCount = 0;

        try {
            // 模拟同步逻辑 - 遍历并保存/更新客户
            List<BizCustomer> allCustomers = list(new LambdaQueryWrapper<>());

            for (BizCustomer customer : allCustomers) {
                try {
                    // 校验必填字段
                    validateCustomer(customer);

                    // 根据编码判断是新增还是更新
                    LambdaQueryWrapper<BizCustomer> existWrapper = new LambdaQueryWrapper<>();
                    existWrapper.eq(BizCustomer::getCustomerCode, customer.getCustomerCode());
                    BizCustomer existing = getOne(existWrapper);

                    if (existing == null) {
                        save(customer);
                    } else {
                        customer.setId(existing.getId());
                        updateById(customer);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("同步客户[{}]失败: {}", customer.getCustomerCode(), e.getMessage());
                }
            }

            log.info("客户信息全量同步完成！成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("客户信息全量同步异常", e);
            throw new RuntimeException("全量同步失败: " + e.getMessage());
        }
    }

    @Override
    public void syncIncremental(String lastSyncTime) {
        // 增量同步：基于时间戳获取变更数据
        log.info("开始执行客户信息增量同步，上次同步时间: {}...", lastSyncTime);

        LambdaQueryWrapper<BizCustomer> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(lastSyncTime)) {
            wrapper.ge(BizCustomer::getUpdateTime, lastSyncTime);
        } else {
            wrapper.ge(BizCustomer::getCreateTime, LocalDateTime.now().minusDays(1).toString());
        }

        List<BizCustomer> changedCustomers = list(wrapper);
        log.info("发现{}条变更的客户记录", changedCustomers.size());

        for (BizCustomer customer : changedCustomers) {
            try {
                validateCustomer(customer);
                saveOrUpdate(customer);
            } catch (Exception e) {
                log.error("增量同步客户[{}]失败: {}", customer.getCustomerCode(), e.getMessage());
            }
        }

        log.info("客户信息增量同步完成");
    }

    @Override
    public boolean checkCodeUnique(String code, Long id) {
        LambdaQueryWrapper<BizCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCustomer::getCustomerCode, code);
        if (id != null) {
            wrapper.ne(BizCustomer::getId, id);
        }
        return count(wrapper) > 0;
    }

    private void validateCustomer(BizCustomer customer) {
        if (StrUtil.isBlank(customer.getCustomerCode())) {
            throw new RuntimeException("客户编码不能为空");
        }
        if (StrUtil.isBlank(customer.getCustomerName())) {
            throw new RuntimeException("客户名称不能为空");
        }
        // 手机号格式校验
        if (StrUtil.isNotBlank(customer.getContactPhone()) && !customer.getContactPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确: " + customer.getContactPhone());
        }
        // 身份证号格式校验（个人）
        if (customer.getCustomerType() != null && customer.getCustomerType() == 1 && StrUtil.isNotBlank(customer.getIdCard())) {
            if (!customer.getIdCard().matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$")) {
                throw new RuntimeException("身份证号格式不正确");
            }
        }
        // 统一社会信用代码格式校验（企业）
        if (customer.getCustomerType() != null && (customer.getCustomerType() == 2 || customer.getCustomerType() == 3)
                && StrUtil.isNotBlank(customer.getCreditCode())) {
            if (!customer.getCreditCode().matches("^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$")) {
                throw new RuntimeException("统一社会信用代码格式不正确");
            }
        }
    }

    private CustomerVO convertToVO(BizCustomer customer) {
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(customer, vo);
        return vo;
    }
}
