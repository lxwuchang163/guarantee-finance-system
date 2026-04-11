package com.guarantee.finance.config;

import com.guarantee.finance.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return R.fail(403, "没有访问权限，请登录后重试");
    }

    @ExceptionHandler(AuthenticationException.class)
    public R<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return R.fail(401, "认证失败: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常 [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return R.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "参数绑定失败";
        return R.fail(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        String exClass = e.getClass().getSimpleName();
        String msg = e.getMessage();
        if (msg != null && msg.length() > 200) {
            msg = msg.substring(0, 200) + "...";
        }
        log.error("系统异常 [{}]: {}", exClass, msg, e);
        return R.fail(500, "系统内部错误 [" + exClass + "]: " + msg);
    }
}
