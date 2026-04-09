package com.guarantee.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.UserDTO;
import com.guarantee.finance.dto.UserQueryDTO;
import com.guarantee.finance.dto.LoginDTO;
import com.guarantee.finance.vo.LoginVO;
import com.guarantee.finance.entity.SysOrg;
import com.guarantee.finance.entity.SysRole;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.entity.SysUserRole;
import com.guarantee.finance.mapper.SysOrgMapper;
import com.guarantee.finance.mapper.SysRoleMapper;
import com.guarantee.finance.mapper.SysUserMapper;
import com.guarantee.finance.mapper.SysUserRoleMapper;
import com.guarantee.finance.security.JwtUtils;
import com.guarantee.finance.service.SysUserService;
import com.guarantee.finance.vo.OnlineUserVO;
import com.guarantee.finance.vo.RoleSimpleVO;
import com.guarantee.finance.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @jakarta.annotation.Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @jakarta.annotation.Resource
    private SysRoleMapper sysRoleMapper;

    @jakarta.annotation.Resource
    private SysOrgMapper sysOrgMapper;

    @jakarta.annotation.Resource
    private RedisTemplate<String, Object> redisTemplate;

    @jakarta.annotation.Resource
    private JwtUtils jwtUtils;

    private static final String ONLINE_USER_KEY = "online:user:";
    private boolean redisEnabled = false;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = getOne(wrapper);

        // 2. 用户不存在
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 用户被禁用
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 4. 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword()) && !loginDTO.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 5. 生成JWT token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 6. 将token存入Redis（可选）
        if (redisEnabled && redisTemplate != null) {
            try {
                String redisKey = com.guarantee.finance.common.Constants.TOKEN_KEY + user.getId();
                redisTemplate.opsForValue().set(redisKey, token, 1, TimeUnit.HOURS);

                // 7. 记录在线用户信息
                String onlineKey = ONLINE_USER_KEY + token;
                Map<String, Object> onlineInfo = new HashMap<>();
                onlineInfo.put("userId", user.getId());
                onlineInfo.put("username", user.getUsername());
                onlineInfo.put("nickname", user.getNickname());
                onlineInfo.put("loginTime", LocalDateTime.now().toString());
                redisTemplate.opsForValue().set(onlineKey, onlineInfo, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                // Redis操作失败，不影响登录
                System.err.println("Redis操作失败: " + e.getMessage());
            }
        }

        // 8. 返回登录信息
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public void logout(Long userId) {
        // 1. 删除Redis中的token（可选）
        if (redisEnabled && redisTemplate != null) {
            try {
                String redisKey = com.guarantee.finance.common.Constants.TOKEN_KEY + userId;
                String token = (String) redisTemplate.opsForValue().get(redisKey);
                if (token != null) {
                    redisTemplate.delete(redisKey);
                    // 删除在线用户信息
                    String onlineKey = ONLINE_USER_KEY + token;
                    redisTemplate.delete(onlineKey);
                }
            } catch (Exception e) {
                // Redis操作失败，不影响登出
                System.err.println("Redis操作失败: " + e.getMessage());
            }
        }
    }

    @Override
    public IPage<UserVO> queryPage(UserQueryDTO queryDTO, Page<UserVO> page) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getUsername())) {
            wrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        if (StrUtil.isNotBlank(queryDTO.getNickname())) {
            wrapper.like(SysUser::getNickname, queryDTO.getNickname());
        }
        if (StrUtil.isNotBlank(queryDTO.getPhone())) {
            wrapper.like(SysUser::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getOrgId() != null) {
            wrapper.eq(SysUser::getOrgId, queryDTO.getOrgId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getSex() != null) {
            wrapper.eq(SysUser::getSex, queryDTO.getSex());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> userPage = page(page.convert(p -> new SysUser()), wrapper);
        List<UserVO> voList = convertToVOList(userPage.getRecords());

        IPage<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public UserVO getUserDetail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            return null;
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 查询机构名称
        if (user.getOrgId() != null && user.getOrgId() > 0) {
            SysOrg org = sysOrgMapper.selectById(user.getOrgId());
            if (org != null) {
                vo.setOrgName(org.getOrgName());
            }
        }

        // 查询角色列表
        LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysUserRole::getUserId, id);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(roleWrapper);

        if (CollUtil.isNotEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
            roleQuery.in(SysRole::getId, roleIds);
            List<SysRole> roles = sysRoleMapper.selectList(roleQuery);

            List<RoleSimpleVO> roleVOs = roles.stream().map(role -> {
                RoleSimpleVO rvo = new RoleSimpleVO();
                rvo.setId(role.getId());
                rvo.setRoleName(role.getRoleName());
                rvo.setRoleCode(role.getRoleCode());
                return rvo;
            }).collect(Collectors.toList());
            vo.setRoles(roleVOs);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO dto) {
        boolean usernameUnique = checkUsernameUnique(dto.getUsername(), null);
        if (!usernameUnique) {
            throw new RuntimeException("用户名已存在: " + dto.getUsername());
        }

        if (StrUtil.isNotBlank(dto.getPhone())) {
            boolean phoneUnique = checkPhoneUnique(dto.getPhone(), null);
            if (!phoneUnique) {
                throw new RuntimeException("手机号已存在: " + dto.getPhone());
            }
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        // 密码加密
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw("123456"));
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }

        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        save(user);

        // 保存角色关联
        if (CollUtil.isNotEmpty(dto.getRoleIds())) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO dto) {
        SysUser existing = getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!existing.getUsername().equals(dto.getUsername())) {
            boolean usernameUnique = checkUsernameUnique(dto.getUsername(), dto.getId());
            if (!usernameUnique) {
                throw new RuntimeException("用户名已存在: " + dto.getUsername());
            }
        }

        if (StrUtil.isNotBlank(dto.getPhone()) && !dto.getPhone().equals(existing.getPhone())) {
            boolean phoneUnique = checkPhoneUnique(dto.getPhone(), dto.getId());
            if (!phoneUnique) {
                throw new RuntimeException("手机号已存在: " + dto.getPhone());
            }
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);

        // 如果密码不为空，则更新密码
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }

        updateById(user);

        // 更新角色关联
        deleteUserRoles(dto.getId());
        if (CollUtil.isNotEmpty(dto.getRoleIds())) {
            saveUserRoles(dto.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 删除用户角色关联
        deleteUserRoles(id);
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String password) {
        SysUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(BCrypt.hashpw(password));
        updateById(user);
    }

    @Override
    public void unlockUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(1);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importUsers(MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

        List<Map<String, Object>> dataList = reader.readAll();

        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (int i = 1; i < dataList.size(); i++) {
            try {
                Map<String, Object> row = dataList.get(i);
                String username = String.valueOf(row.getOrDefault(0, ""));
                String nickname = String.valueOf(row.getOrDefault(1, ""));
                String phone = String.valueOf(row.getOrDefault(2, ""));
                String email = String.valueOf(row.getOrDefault(3, ""));
                String orgCode = String.valueOf(row.getOrDefault(4, ""));

                if (StrUtil.isBlank(username)) {
                    failCount++;
                    failMsg.append(String.format("第%d行: 用户名不能为空; ", i + 1));
                    continue;
                }

                boolean usernameUnique = checkUsernameUnique(username.trim(), null);
                if (!usernameUnique) {
                    failCount++;
                    failMsg.append(String.format("第%d行: 用户名%s已存在; ", i + 1, username));
                    continue;
                }

                SysUser user = new SysUser();
                user.setUsername(username.trim());
                user.setNickname(StrUtil.isBlank(nickname) ? username.trim() : nickname.trim());
                user.setPhone(StrUtil.isBlank(phone) ? null : phone.trim());
                user.setEmail(StrUtil.isBlank(email) ? null : email.trim());
                user.setPassword(BCrypt.hashpw("123456"));
                user.setStatus(1);
                user.setSex(0);

                // 根据机构编码查找机构ID
                if (StrUtil.isNotBlank(orgCode) && !"null".equals(orgCode)) {
                    LambdaQueryWrapper<SysOrg> orgWrapper = new LambdaQueryWrapper<>();
                    orgWrapper.eq(SysOrg::getOrgCode, orgCode.trim());
                    SysOrg org = sysOrgMapper.selectOne(orgWrapper);
                    if (org != null) {
                        user.setOrgId(org.getId());
                    }
                }

                save(user);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failMsg.append(String.format("第%d行: %s; ", i + 1, e.getMessage()));
            }
        }

        return String.format("导入完成！成功：%d条，失败：%d条。%s",
                successCount, failCount, failMsg.length() > 0 ? "失败原因：" + failMsg : "");
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        ExcelWriter writer = ExcelUtil.getWriter(true);

        writer.addHeaderAlias("username", "用户名*");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("phone", "手机号");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("orgCode", "所属机构编码");

        List<Map<String, Object>> templateData = new ArrayList<>();
        Map<String, Object> exampleRow = new LinkedHashMap<>();
        exampleRow.put("username", "示例：zhangsan");
        exampleRow.put("nickname", "示例：张三");
        exampleRow.put("phone", "示例：13800138000");
        exampleRow.put("email", "示例：zhangsan@example.com");
        exampleRow.put("orgCode", "示例：（留空或不填）");
        templateData.add(exampleRow);

        writer.write(templateData, true);
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }

    @Override
    public List<OnlineUserVO> getOnlineUsers() {
        List<OnlineUserVO> onlineUsers = new ArrayList<>();

        if (redisEnabled && redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys(ONLINE_USER_KEY + "*");
                if (CollUtil.isEmpty(keys)) {
                    return onlineUsers;
                }

                for (String key : keys) {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userInfo = (Map<String, Object>) value;
                        OnlineUserVO vo = new OnlineUserVO();
                        vo.setToken(key.replace(ONLINE_USER_KEY, ""));
                        vo.setUserId(((Number) userInfo.getOrDefault("userId", 0)).longValue());
                        vo.setUsername((String) userInfo.getOrDefault("username", ""));
                        vo.setNickname((String) userInfo.getOrDefault("nickname", ""));
                        vo.setIpaddr((String) userInfo.getOrDefault("ipaddr", ""));
                        vo.setLoginLocation((String) userInfo.getOrDefault("loginLocation", ""));
                        vo.setBrowser((String) userInfo.getOrDefault("browser", ""));
                        vo.setOs((String) userInfo.getOrDefault("os", ""));
                        vo.setLoginTime(LocalDateTime.now()); // 实际应从Redis中存储的时间读取
                        onlineUsers.add(vo);
                    }
                }
            } catch (Exception e) {
                System.err.println("获取在线用户失败: " + e.getMessage());
            }
        }

        return onlineUsers;
    }

    @Override
    public void forceLogout(String token) {
        if (redisEnabled && redisTemplate != null) {
            try {
                String key = ONLINE_USER_KEY + token;
                Boolean result = redisTemplate.delete(key);
                if (result == null || !result) {
                    throw new RuntimeException("强制下线失败，token可能已过期");
                }
            } catch (Exception e) {
                // Redis操作失败
                throw new RuntimeException("强制下线失败: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Redis未启用，无法强制下线");
        }
    }

    @Override
    public boolean checkUsernameUnique(String username, Long id) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (id != null) {
            wrapper.ne(SysUser::getId, id);
        }
        return count(wrapper) == 0;
    }

    @Override
    public boolean checkPhoneUnique(String phone, Long id) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        if (id != null) {
            wrapper.ne(SysUser::getId, id);
        }
        return count(wrapper) == 0;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private void deleteUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);
    }

    private List<UserVO> convertToVOList(List<SysUser> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }

        return userList.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);

            // 查询机构名称
            if (user.getOrgId() != null && user.getOrgId() > 0) {
                SysOrg org = sysOrgMapper.selectById(user.getOrgId());
                if (org != null) {
                    vo.setOrgName(org.getOrgName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
