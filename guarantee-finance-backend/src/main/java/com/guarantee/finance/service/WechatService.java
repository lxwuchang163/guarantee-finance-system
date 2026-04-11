package com.guarantee.finance.service;

import java.util.Map;

public interface WechatService {

    /**
     * 生成微信登录二维码
     * @return 包含二维码URL和ticket的Map
     */
    Map<String, String> generateQrCode();

    /**
     * 获取二维码扫描状态
     * @param ticket 二维码票据
     * @return 状态信息
     */
    Map<String, Object> getQrCodeStatus(String ticket);

    /**
     * 处理微信登录回调
     * @param code 微信授权码
     * @return 登录结果
     */
    Map<String, Object> processWechatLogin(String code);
}