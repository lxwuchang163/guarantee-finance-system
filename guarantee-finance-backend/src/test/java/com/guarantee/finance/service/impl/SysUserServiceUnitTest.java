package com.guarantee.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SysUserServiceUnitTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(new BCryptPasswordEncoder().encode("password123"));
        testUser.setNickname("测试用户");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("根据用户名查找用户-成功场景")
    public void testFindByUsernameSuccess() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        SysUser result = sysUserService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(sysUserMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据用户名查找用户-用户不存在")
    public void testFindByUsernameNotFound() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        SysUser result = sysUserService.findByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("用户状态校验-正常用户可登录")
    public void testValidateUserStatusNormal() {
        assertTrue(sysUserService.validateUserStatus(testUser));
    }

    @Test
    @DisplayName("用户状态校验-禁用用户不可登录")
    public void testValidateUserStatusDisabled() {
        testUser.setStatus(0);
        assertFalse(sysUserService.validateUserStatus(testUser));
    }
}
