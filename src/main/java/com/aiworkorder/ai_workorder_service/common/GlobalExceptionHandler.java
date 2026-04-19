package com.aiworkorder.ai_workorder_service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // 处理参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringJoiner sj = new StringJoiner("；");
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            sj.add(fieldError.getField() + "：" + fieldError.getDefaultMessage());
        });
        log.error("参数校验异常：{}", sj.toString());
        return Result.fail(400, sj.toString());
    }

    // 处理约束违反异常
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        StringJoiner sj = new StringJoiner("；");
        e.getConstraintViolations().forEach(violation -> {
            sj.add(violation.getPropertyPath() + "：" + violation.getMessage());
        });
        return Result.fail(400, sj.toString());
    }

    // 处理请求方法不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return Result.fail(405, "不支持的请求方法: " + e.getMethod());
    }

    // 处理404
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return Result.fail(404, "接口不存在: " + e.getRequestURL());
    }

    // 处理JSON解析错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        return Result.fail(400, "请求参数格式错误或JSON解析失败");
    }

    // 处理权限不足
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(403, "权限不足");
    }

    // 处理认证失败
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        return Result.fail(401, "认证失败: " + e.getMessage());
    }

    // 处理必填参数缺失
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必填参数: " + e.getParameterName());
    }

    // 处理其他所有异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器繁忙，请稍后重试");
    }
}