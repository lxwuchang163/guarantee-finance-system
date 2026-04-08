package com.guarantee.finance.controller;

import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.LoginDTO;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.SysUserService;
import com.guarantee.finance.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return R.ok(loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout(@AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.logout(loginUser.getUserId());
        return R.ok();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userinfo")
    public R<SysUser> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        SysUser user = sysUserService.getUserInfo(loginUser.getUserId());
        user.setPassword(null);
        return R.ok(user);
    }
}
