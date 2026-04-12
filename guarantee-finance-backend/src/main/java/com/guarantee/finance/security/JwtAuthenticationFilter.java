package com.guarantee.finance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarantee.finance.common.Constants;
import com.guarantee.finance.common.R;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        String requestUri = request.getRequestURI();

        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtils.validateToken(token)) {
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String username = jwtUtils.getUsernameFromToken(token);

                    // 为所有用户添加默认权限
                    java.util.List<String> permissions = new java.util.ArrayList<>();
                    permissions.add("voucher:create");
                    permissions.add("voucher:update");
                    permissions.add("voucher:delete");
                    permissions.add("voucher:submit");
                    permissions.add("voucher:void");
                    permissions.add("voucher:restore");
                    permissions.add("voucher:post");
                    permissions.add("voucher:unpost");
                    permissions.add("voucher:import");
                    permissions.add("voucher:export");
                    permissions.add("subject:view");
                    permissions.add("subject:create");
                    permissions.add("subject:update");
                    permissions.add("subject:delete");

                    LoginUser loginUser = new LoginUser();
                    loginUser.setUserId(userId);
                    loginUser.setUsername(username);
                    loginUser.setStatus(1);
                    loginUser.setPermissions(permissions);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            loginUser, null, loginUser.getAuthorities());

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);

                    log.info("Auth set for user: {}, uri: {}", username, requestUri);
                } else {
                    log.warn("Invalid token for uri: {}", requestUri);
                    sendErrorResponse(response, 401, "Token已过期或无效，请重新登录");
                    return;
                }
            } catch (Exception e) {
                log.error("Token validation error: {}", e.getMessage());
                sendErrorResponse(response, 401, "Token验证异常: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        R<?> result = R.fail(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
