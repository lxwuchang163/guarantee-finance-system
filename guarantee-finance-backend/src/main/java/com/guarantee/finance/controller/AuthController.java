package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.LoginDTO;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.security.JwtUtils;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.SysUserService;
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

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

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
}
