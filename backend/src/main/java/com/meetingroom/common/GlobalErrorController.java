package com.meetingroom.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一错误兜底：/error 返回统一 Result 结构（B-14），
 * 避免前端拿到 Spring 默认错误页导致 .data 取值失败。
 */
@Slf4j
@RestController
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public Result<?> error(HttpServletRequest request, HttpServletResponse response) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status == null) {
            status = 500;
        }
        response.setStatus(status);
        if (status == HttpStatus.NOT_FOUND.value()) {
            return Result.error(404, "接口不存在");
        }
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            return Result.error(401, "未登录或登录已失效，请重新登录");
        }
        if (status == HttpStatus.FORBIDDEN.value()) {
            return Result.error(403, "无权限访问");
        }
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
