package com.mashang.common.handler;

import com.mashang.common.common.R;
import com.mashang.common.constants.HttpStatus;
import com.mashang.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 —— 统一拦截所有 Controller 层抛出的异常。
 *
 * 拦截顺序（按 ExceptionHandler 方法定义顺序，Spring 按精确匹配优先）：
 * 1. ServiceException        → 业务异常，返回对应 code + 自定义 message
 * 2. MethodArgumentNotValid  → @RequestBody + @Validated 校验失败，返回 400 + 字段级错误
 * 3. BindException           → @ModelAttribute + @Validated 校验失败，返回 400 + 字段级错误
 * 4. ConstraintViolation     → 方法参数直接校验失败，返回 400 + 错误信息
 * 5. Exception               → 兜底异常，返回 500 + "服务器内部错误"
 *
 * 设计原则：
 * - 业务异常直接透传 code 和 message，错误信息对用户友好
 * - 参数校验失败返回 400 + 字段级明细，方便前端定位具体哪个字段出错
 * - 未知异常打印完整堆栈，方便运维排查问题
 *
 * 注意：
 * - 本类通过 CommonAutoConfiguration 导入，所有引入 common-service 的服务自动生效
 * - 不要在子服务中重复定义 @RestControllerAdvice，否则会覆盖本处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ======================== 业务异常 ========================

    /**
     * 处理业务异常（ServiceException）。
     * Service 层抛出 ServiceException 时，直接将其 code 和 message 转换为 R 返回。
     * 日志级别为 ERROR，因为业务异常通常需要关注（如重复注册、越权操作）。
     *
     * @param e 业务异常
     * @return R 包含异常的状态码和消息
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // ======================== 参数校验失败（@RequestBody） ========================

    /**
     * 处理 @RequestBody + @Validated 校验失败。
     * 当请求体 JSON 反序列化后的 DTO 字段校验不通过时抛出此异常。
     *
     * 示例：LoginUserQuery.username 标注了 @NotBlank，但前端传了空字符串
     * → 异常被捕获 → 返回 { code: 400, msg: "账号不能为空" }
     *
     * 多个字段校验失败时，用逗号拼接所有错误消息。
     * 日志级别为 WARN（非服务端错误，只是客户端参数问题）。
     *
     * @param e 方法参数校验异常
     * @return R 状态码 400，msg 为字段校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        return R.fail(HttpStatus.BAD_REQUEST, msg);
    }

    // ======================== 参数校验失败（@ModelAttribute / 表单绑定） ========================

    /**
     * 处理 @ModelAttribute + @Validated 校验失败。
     * 适用于表单提交（application/x-www-form-urlencoded）或 GET 请求参数绑定到对象。
     * 与 MethodArgumentNotValidException 类似，只是触发场景不同。
     *
     * @param e 参数绑定异常
     * @return R 状态码 400，msg 为字段校验错误信息
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", msg);
        return R.fail(HttpStatus.BAD_REQUEST, msg);
    }

    // ======================== 参数校验失败（方法参数直接校验） ========================

    /**
     * 处理方法参数直接校验失败（如 @RequestParam + @Min/@Max）。
     * 与上面两个不同，这个异常发生在方法参数不是对象时，
     * 直接对参数使用约束注解（如 @Positive、@Size 等）。
     *
     * 触发条件：Controller 类上标注了 @Validated，且方法参数有约束注解
     *
     * @param e 约束违反异常
     * @return R 状态码 400，msg 为校验失败信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        return R.fail(HttpStatus.BAD_REQUEST, msg);
    }

    // ======================== 兜底异常 ========================

    /**
     * 处理所有未被上述处理器捕获的异常。
     * 这是最后一道防线，防止异常直接暴露给前端（如 NullPointerException、SQL 异常等）。
     *
     * 重要：
     * - 日志级别为 ERROR，并打印完整堆栈，方便运维排查
     * - 返回给前端的消息是通用提示，不包含敏感信息（如数据库密码、SQL 语句）
     * - 生产环境建议接入告警系统，对此类未知异常做实时通知
     *
     * @param e 未知异常
     * @return R 状态码 500，msg 为通用错误提示
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(HttpStatus.ERROR, "服务器内部错误");
    }
}
