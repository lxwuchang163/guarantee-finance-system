package com.guarantee.finance.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.entity.SysWechatUser;
import com.guarantee.finance.mapper.SysUserMapper;
import com.guarantee.finance.mapper.SysWechatUserMapper;
import com.guarantee.finance.service.WechatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WechatServiceImpl implements WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatServiceImpl.class);

    @Resource
    private SysWechatUserMapper sysWechatUserMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    // 微信登录配置
    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    @Value("${wechat.redirect-uri}")
    private String redirectUri;

    // 二维码状态存储
    private final Map<String, Map<String, Object>> qrCodeStatusMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> generateQrCode() {
        // 生成随机状态参数
        String state = RandomUtil.randomString(32);

        // 构建微信授权URL
        try {
            String authUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                    "?appid=" + appId +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=snsapi_login" +
                    "&state=" + state +
                    "#wechat_redirect";

            // 存储二维码状态
            Map<String, Object> status = new HashMap<>();
            status.put("createdTime", LocalDateTime.now());
            status.put("scanned", false);
            status.put("userId", null);
            qrCodeStatusMap.put(state, status);

            Map<String, String> result = new HashMap<>();
            result.put("ticket", state);
            result.put("qrCodeUrl", authUrl);
            return result;
        } catch (Exception e) {
            log.error("生成微信二维码失败", e);
            Map<String, String> result = new HashMap<>();
            result.put("ticket", "");
            result.put("qrCodeUrl", "");
            return result;
        }
    }

    @Override
    public Map<String, Object> getQrCodeStatus(String ticket) {
        Map<String, Object> status = qrCodeStatusMap.get(ticket);
        Map<String, Object> result = new HashMap<>();

        if (status == null) {
            result.put("status", "expired");
            result.put("message", "二维码已过期");
        } else {
            if ((Boolean) status.get("scanned")) {
                result.put("status", "scanned");
                result.put("message", "二维码已扫描");
                result.put("userId", status.get("userId"));
            } else {
                // 检查是否过期（5分钟）
                LocalDateTime createdTime = (LocalDateTime) status.get("createdTime");
                if (createdTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
                    result.put("status", "expired");
                    result.put("message", "二维码已过期");
                    qrCodeStatusMap.remove(ticket);
                } else {
                    result.put("status", "waiting");
                    result.put("message", "等待扫描");
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processWechatLogin(String code) {
        try {
            // 1. 通过 code 获取 access_token
            String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=" + appId +
                    "&secret=" + appSecret +
                    "&code=" + code +
                    "&grant_type=authorization_code";

            // 发送 HTTP 请求获取 token
            String tokenResponse = HttpRequest.get(tokenUrl).execute().body();
            log.info("获取微信token响应: {}", tokenResponse);
            cn.hutool.json.JSONObject tokenJson = JSONUtil.parseObj(tokenResponse);

            String accessToken = tokenJson.getStr("access_token");
            String openid = tokenJson.getStr("openid");
            String unionid = tokenJson.getStr("unionid");

            if (openid == null) {
                log.error("获取微信openid失败: {}", tokenJson.getStr("errmsg"));
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "微信登录失败");
                return result;
            }

            // 2. 获取用户信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=" + accessToken +
                    "&openid=" + openid +
                    "&lang=zh_CN";

            String userInfoResponse = HttpRequest.get(userInfoUrl).execute().body();
            log.info("获取微信用户信息响应: {}", userInfoResponse);
            cn.hutool.json.JSONObject userInfoJson = JSONUtil.parseObj(userInfoResponse);

            String nickname = userInfoJson.getStr("nickname");
            String avatar = userInfoJson.getStr("headimgurl");
            int sex = userInfoJson.getInt("sex");

            if (nickname == null) {
                log.error("获取微信用户信息失败: {}", userInfoJson.getStr("errmsg"));
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "微信登录失败");
                return result;
            }

            // 3. 查找是否已绑定用户
            LambdaQueryWrapper<SysWechatUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysWechatUser::getOpenid, openid);
            SysWechatUser wechatUser = sysWechatUserMapper.selectOne(wrapper);

            Long userId = null;
            if (wechatUser != null) {
                userId = wechatUser.getUserId();
            } else {
                // 创建新用户
                SysUser user = new SysUser();
                user.setUsername("wechat_" + RandomUtil.randomString(10));
                user.setPassword("123456"); // 默认密码
                user.setNickname(nickname);
                user.setAvatar(avatar);
                user.setSex(sex);
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());

                sysUserMapper.insert(user);
                userId = user.getId();

                // 绑定微信用户
                wechatUser = new SysWechatUser();
                wechatUser.setOpenid(openid);
                wechatUser.setUnionid(unionid);
                wechatUser.setNickname(nickname);
                wechatUser.setAvatar(avatar);
                wechatUser.setSex(sex);
                wechatUser.setUserId(userId);
                wechatUser.setCreateTime(LocalDateTime.now());
                wechatUser.setUpdateTime(LocalDateTime.now());

                sysWechatUserMapper.insert(wechatUser);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("nickname", nickname);
            result.put("avatar", avatar);

            return result;
        } catch (Exception e) {
            log.error("微信登录处理失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "微信登录失败");
            return result;
        }
    }
}