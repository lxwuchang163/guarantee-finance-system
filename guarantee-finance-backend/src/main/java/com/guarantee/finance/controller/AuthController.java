package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.LoginDTO;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.security.JwtUtils;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.SmsService;
import com.guarantee.finance.service.SysUserService;
import com.guarantee.finance.service.WechatService;
import com.guarantee.finance.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SmsService smsService;

    @Autowired
    private WechatService wechatService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return R.ok(loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            sysUserService.logout(userId);
        }
        return R.ok();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userinfo")
    public R<SysUser> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getCurrentUserId();

        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtils.validateToken(token)) {
                    userId = jwtUtils.getUserIdFromToken(token);
                }
            } catch (Exception ignored) {}
        }

        if (userId == null) {
            return R.fail(401, "未登录");
        }
        SysUser user = sysUserService.getUserInfo(userId);
        user.setPassword(null);
        return R.ok(user);
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sendSms")
    public R<Void> sendSms(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            return R.fail("手机号不能为空");
        }
        boolean result = smsService.sendSmsCode(phone);
        if (result) {
            return R.ok();
        } else {
            return R.fail("验证码发送失败，请稍后重试");
        }
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/loginBySms")
    public R<LoginVO> loginBySms(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        
        if (phone == null || phone.isEmpty() || code == null || code.isEmpty()) {
            return R.fail("手机号和验证码不能为空");
        }
        
        // 验证短信验证码
        boolean valid = smsService.verifySmsCode(phone, code);
        if (!valid) {
            return R.fail("验证码错误或已过期");
        }

        // 根据手机号获取用户
        SysUser user = sysUserService.getUserByPhone(phone);
        if (user == null) {
            return R.fail("用户不存在");
        }

        // 生成JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 返回登录信息
        LoginVO loginVO = new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());

        return R.ok(loginVO);
    }

    @Operation(summary = "获取微信登录二维码")
    @GetMapping("/wechat/qrCode")
    public R<Map<String, String>> getWechatQrCode() {
        Map<String, String> result = wechatService.generateQrCode();
        return R.ok(result);
    }

    @Operation(summary = "检查微信二维码扫描状态")
    @GetMapping("/wechat/checkStatus")
    public R<Map<String, Object>> checkWechatLoginStatus(@RequestParam String ticket) {
        Map<String, Object> status = wechatService.getQrCodeStatus(ticket);
        return R.ok(status);
    }

    @Operation(summary = "微信登录回调")
    @GetMapping("/wechat/callback")
    public R<Map<String, Object>> wechatCallback(@RequestParam String code) {
        Map<String, Object> result = wechatService.processWechatLogin(code);
        return R.ok(result);
    }
}
