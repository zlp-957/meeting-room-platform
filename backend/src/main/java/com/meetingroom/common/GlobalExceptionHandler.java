package com.meetingroom.common;

/**
 * 全局异常处理器
 */
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.error(401, "未登录或登录已失效，请重新登录");
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRole(NotRoleException e) {
        return Result.error(403, "无权限访问");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValid(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }

    /** 缺失参数 / 类型不匹配 / 请求体不可读 → 统一 400（B-14） */
    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public Result<?> handleBadRequest(Exception e) {
        return Result.error(400, "请求参数不正确");
    }

    /** 404：接口不存在时返回统一 Result 结构，而非 Spring 默认错误页（B-14） */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNotFound(NoHandlerFoundException e) {
        return Result.error(404, "接口不存在");
    }

    /** 数据库唯一约束冲突 → 友好提示（具体业务场景在 Service 内自行捕获并给出准确文案） */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<?> handleDuplicateKey(org.springframework.dao.DuplicateKeyException e) {
        return Result.error("保存失败：填写内容与已有数据重复，请检查后重试");
    }

    /**
     * 未知异常统一脱敏：不向前端泄露 SQL/堆栈细节，仅记录后台日志（B-04）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
