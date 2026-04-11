package com.guarantee.finance.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.SysSmsCode;
import com.guarantee.finance.mapper.SysSmsCodeMapper;
import com.guarantee.finance.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Resource
    private SysSmsCodeMapper sysSmsCodeMapper;

    // 阿里云短信服务配置
    @Value("${sms.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${sms.aliyun.sign-name}")
    private String signName;

    @Value("${sms.aliyun.template-code}")
    private String templateCode;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendSmsCode(String phone) {
        // 检查1分钟内是否已发送过验证码
        LambdaQueryWrapper<SysSmsCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSmsCode::getPhone, phone)
                .eq(SysSmsCode::getStatus, 0)
                .ge(SysSmsCode::getCreateTime, LocalDateTime.now().minusMinutes(1));
        if (sysSmsCodeMapper.selectCount(wrapper) > 0) {
            return false; // 1分钟内已发送过
        }

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 存储验证码
        SysSmsCode smsCode = new SysSmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(5)); // 5分钟过期
        smsCode.setStatus(0);
        smsCode.setCreateTime(LocalDateTime.now());
        smsCode.setUpdateTime(LocalDateTime.now());

        sysSmsCodeMapper.insert(smsCode);

        // 调用阿里云短信服务发送验证码
        try {
            // 初始化阿里云客户端
            log.info("开始发送短信，手机号：{}", phone);
            log.info("阿里云配置 - AccessKeyId: {}, SignName: {}, TemplateCode: {}", 
                    accessKeyId.substring(0, 4) + "****", signName, templateCode);
            
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient client = new DefaultAcsClient(profile);

            // 构建请求
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");

            // 发送请求
            log.info("发送短信请求：{}，手机号：{}", request.getTemplateCode(), phone);
            SendSmsResponse response = client.getAcsResponse(request);
            log.info("发送短信结果: Code={}, Message={}, RequestId={}", 
                    response.getCode(), response.getMessage(), response.getRequestId());
            
            if ("OK".equals(response.getCode())) {
                log.info("短信发送成功，手机号：{}", phone);
                return true;
            } else {
                log.error("发送短信失败: {}，手机号：{}", response.getMessage(), phone);
                return false;
            }
        } catch (Exception e) {
            log.error("发送短信异常，手机号：{}", phone, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifySmsCode(String phone, String code) {
        // 查询未使用且未过期的验证码
        LambdaQueryWrapper<SysSmsCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSmsCode::getPhone, phone)
                .eq(SysSmsCode::getCode, code)
                .eq(SysSmsCode::getStatus, 0)
                .ge(SysSmsCode::getExpireTime, LocalDateTime.now());

        SysSmsCode smsCode = sysSmsCodeMapper.selectOne(wrapper);
        if (smsCode == null) {
            return false;
        }

        // 标记验证码为已使用
        smsCode.setStatus(1);
        smsCode.setUpdateTime(LocalDateTime.now());
        sysSmsCodeMapper.updateById(smsCode);

        return true;
    }
}