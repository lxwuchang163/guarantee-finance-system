package com.guarantee.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.common.PageResult;
import com.guarantee.finance.common.R;
import com.guarantee.finance.dto.UserDTO;
import com.guarantee.finance.dto.UserQueryDTO;
import com.guarantee.finance.service.SysUserService;
import com.guarantee.finance.vo.OnlineUserVO;
import com.guarantee.finance.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public R<PageResult<UserVO>> page(UserQueryDTO queryDTO,
                                       @RequestParam(defaultValue = "1") Long current,
                                       @RequestParam(defaultValue = "10") Long size) {
        IPage<UserVO> page = sysUserService.queryPage(queryDTO, new Page<>(current, size));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public R<UserVO> detail(@PathVariable Long id) {
        UserVO vo = sysUserService.getUserDetail(id);
        return R.ok(vo);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public R<Void> add(@Valid @RequestBody UserDTO dto) {
        sysUserService.createUser(dto);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public R<Void> update(@Valid @RequestBody UserDTO dto) {
        if (dto.getId() == null) {
            return R.fail("用户ID不能为空");
        }
        sysUserService.updateUser(dto);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = sysUserService.deleteUser(id);
        if (!success) {
            return R.fail("删除失败，该用户可能不存在");
        }
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/resetPwd/{id}")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam(defaultValue = "123456") String password) {
        sysUserService.resetPassword(id, password);
        return R.ok();
    }

    @Operation(summary = "解锁用户")
    @PutMapping("/unlock/{id}")
    public R<Void> unlockUser(@PathVariable Long id) {
        sysUserService.unlockUser(id);
        return R.ok();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/status/{id}")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysUserService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "批量导入用户")
    @PostMapping("/import")
    public R<String> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        String result = sysUserService.importUsers(file);
        return R.ok(result);
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) throws IOException {
        sysUserService.downloadTemplate(response);
    }

    @Operation(summary = "获取在线用户列表")
    @GetMapping("/online")
    public R<List<OnlineUserVO>> getOnlineUsers() {
        List<OnlineUserVO> users = sysUserService.getOnlineUsers();
        return R.ok(users);
    }

    @Operation(summary = "强制下线")
    @DeleteMapping("/online/{token}")
    public R<Void> forceLogout(@PathVariable String token) {
        sysUserService.forceLogout(token);
        return R.ok();
    }

    @Operation(summary = "检查用户名唯一性")
    @GetMapping("/checkUsername")
    public R<Boolean> checkUsernameUnique(@RequestParam String username,
                                          @RequestParam(required = false) Long id) {
        boolean exists = sysUserService.checkUsernameUnique(username, id);
        return R.ok(!exists);
    }

    @Operation(summary = "检查手机号唯一性")
    @GetMapping("/checkPhone")
    public R<Boolean> checkPhoneUnique(@RequestParam String phone,
                                       @RequestParam(required = false) Long id) {
        if (phone == null || phone.trim().isEmpty()) {
            return R.ok(true);
        }
        boolean exists = sysUserService.checkPhoneUnique(phone, id);
        return R.ok(!exists);
    }
}
