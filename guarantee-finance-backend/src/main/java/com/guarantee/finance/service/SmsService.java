package com.guarantee.finance.service;

public interface SmsService {

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 发送结果
     */
    boolean sendSmsCode(String phone);

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifySmsCode(String phone, String code);
}