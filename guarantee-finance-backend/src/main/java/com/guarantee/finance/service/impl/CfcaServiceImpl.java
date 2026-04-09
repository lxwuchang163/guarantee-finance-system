package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.dto.CfcaSignDTO;
import com.guarantee.finance.entity.CfcaCertificate;
import com.guarantee.finance.mapper.CfcaCertificateMapper;
import com.guarantee.finance.service.CfcaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CfcaServiceImpl implements CfcaService {

    private static final Logger log = LoggerFactory.getLogger(CfcaServiceImpl.class);

    @Resource
    private CfcaCertificateMapper certificateMapper;

    @Override
    public List<Map<String, Object>> listCertificates() {
        List<CfcaCertificate> certs = certificateMapper.selectList(new LambdaQueryWrapper<>());
        List<Map<String, Object>> result = new ArrayList<>();
        for (CfcaCertificate cert : certs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cert.getId());
            map.put("certType", cert.getCertType());
            map.put("certName", cert.getCertName());
            map.put("ownerName", cert.getOwnerName());
            map.put("validFrom", cert.getValidFrom());
            map.put("validTo", cert.getValidTo());
            map.put("status", cert.getStatus());
            map.put("statusText", getCertStatusText(cert.getStatus()));
            // 计算剩余天数
            if (cert.getValidTo() != null) {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), cert.getValidTo());
                map.put("daysLeft", daysLeft);
                map.put("isExpiringSoon", daysLeft <= 30 && daysLeft > 0);
                map.put("isExpired", daysLeft <= 0);
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public boolean signPaymentData(CfcaSignDTO dto) {
        log.info("开始CFCA签名，付款单：{}，金额：{}", dto.getPaymentNo(), dto.getAmount());

        try {
            // 检查金额对应的签名级别
            int requiredLevel = getRequiredSignLevel(dto.getAmount());
            if (dto.getSignLevel() == null) dto.setSignLevel(requiredLevel);

            // 验证证书有效性
            LambdaQueryWrapper<CfcaCertificate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CfcaCertificate::getStatus, 1); // 正常状态
            wrapper.eq(CfcaCertificate::getCertType, "OPERATOR"); // 操作员证书
            long validCertCount = certificateMapper.selectCount(wrapper);
            if (validCertCount < dto.getSignLevel()) {
                throw new RuntimeException("可用操作员证书数量不足，需要" + dto.getSignLevel() + "个，当前" + validCertCount + "个");
            }

            // 模拟CFCA签名过程
            String signData = buildSignData(dto);
            log.info("待签名数据：{}", signData);

            // TODO: 调用实际CFCA SDK进行签名
            // String signature = cfcaClient.sign(signData, certAlias, password);
            log.info("CFCA签名完成，付款单：{}", dto.getPaymentNo());
            return true;
        } catch (Exception e) {
            log.error("CFCA签名失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifySignature(String data, String signature) {
        log.info("验证CFCA签名...");
        // TODO: 调用CFCA验签接口
        return true; // 模拟返回
    }

    @Override
    public Map<String, Object> checkCertificateExpiry() {
        Map<String, Object> result = new HashMap<>();
        List<CfcaCertificate> allCerts = certificateMapper.selectList(new LambdaQueryWrapper<>());

        int total = allCerts.size();
        int normal = 0;
        int expiringSoon = 0; // 30天内到期
        int expired = 0;

        for (CfcaCertificate cert : allCerts) {
            switch (cert.getStatus()) {
                case 1: normal++;
                    if (cert.getValidTo() != null) {
                        long days = ChronoUnit.DAYS.between(LocalDate.now(), cert.getValidTo());
                        if (days <= 30 && days > 0) expiringSoon++;
                    }
                    break;
                case 0: expired++; break;
                default: break;
            }
        }

        result.put("totalCertificates", total);
        result.put("normal", normal);
        result.put("expiringSoon", expiringSoon);
        result.put("expired", expired);
        result.put("alertNeeded", expiringSoon > 0 || expired > 0);
        return result;
    }

    @Override
    public void refreshExpiryAlert() {
        // 更新即将过期的证书状态
        LambdaQueryWrapper<CfcaCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CfcaCertificate::getStatus, 1);
        List<CfcaCertificate> normalCerts = certificateMapper.selectList(wrapper);

        for (CfcaCertificate cert : normalCerts) {
            if (cert.getValidTo() != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), cert.getValidTo());
                if (days <= 7 && days > 0) {
                    cert.setStatus(3); // 待更新
                    certificateMapper.updateById(cert);
                    log.warn("证书[{}]即将过期（{}天），已标记为待更新", cert.getCertName(), days);
                } else if (days <= 0) {
                    cert.setStatus(0); // 已过期
                    certificateMapper.updateById(cert);
                    log.warn("证书[{}]已过期", cert.getCertName());
                }
            }
        }
    }

    private int getRequiredSignLevel(java.math.BigDecimal amount) {
        if (amount.compareTo(new java.math.BigDecimal("50000")) <= 0) return 1;
        if (amount.compareTo(new java.math.BigDecimal("500000")) <= 0) return 2;
        return 3;
    }

    private String buildSignData(CfcaSignDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("paymentNo=").append(dto.getPaymentNo()).append("|");
        sb.append("amount=").append(dto.getAmount()).append("|");
        sb.append("payeeName=").append(dto.getPayeeName()).append("|");
        sb.append("payeeAccountNo=").append(dto.getPayeeAccountNo()).append("|");
        sb.append("timestamp=").append(System.currentTimeMillis());
        return sb.toString();
    }

    private String getCertStatusText(Integer status) {
        switch (status) {
            case 0: return "已过期";
            case 1: return "正常";
            case 2: return "已吊销";
            case 3: return "待更新";
            default: return "未知";
        }
    }
}
