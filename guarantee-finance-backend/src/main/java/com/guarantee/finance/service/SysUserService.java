package com.guarantee.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.dto.LoginDTO;
import com.guarantee.finance.dto.UserDTO;
import com.guarantee.finance.dto.UserQueryDTO;
import com.guarantee.finance.entity.SysUser;
import com.guarantee.finance.vo.LoginVO;
import com.guarantee.finance.vo.OnlineUserVO;
import com.guarantee.finance.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginDTO loginDTO);

    void logout(Long userId);

    IPage<UserVO> queryPage(UserQueryDTO queryDTO, Page<UserVO> page);

    UserVO getUserDetail(Long id);

    void createUser(UserDTO dto);

    void updateUser(UserDTO dto);

    boolean deleteUser(Long id);

    void resetPassword(Long id, String password);

    void unlockUser(Long id);

    void updateStatus(Long id, Integer status);

    String importUsers(MultipartFile file) throws IOException;

    void downloadTemplate(HttpServletResponse response) throws IOException;

    List<OnlineUserVO> getOnlineUsers();

    void forceLogout(String token);

    boolean checkUsernameUnique(String username, Long id);

    boolean checkPhoneUnique(String phone, Long id);
}
