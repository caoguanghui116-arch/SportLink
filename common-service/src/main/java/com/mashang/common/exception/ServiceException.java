package com.mashang.common.exception;

/**
 * 业务异常 —— 用于在 Service 层抛出可预见的业务错误。
 *
 * 与 RuntimeException 的区别：
 * - RuntimeException：未经检查的运行时异常，通常表示代码 bug
 * - ServiceException：预期内的业务异常（如 "用户名已存在"、"库存不足"），
 *   由 GlobalExceptionHandler 统一捕获并转换为 R.fail(code, msg) 返回给前端
 *
 * 使用示例：
 * <pre>
 *   if (userMapper.selectOne(wrapper) != null) {
 *       throw new ServiceException(HttpStatus.BAD_REQUEST, "用户名已存在");
 *   }
 * </pre>
 *
 * 注意：
 * - 不要用 ServiceException 替代参数校验（优先使用 @Valid/@Validated）
 * - 不要吞掉 ServiceException 的 message，它最终会展示给用户
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 业务错误码，对应 HttpStatus 中的常量值 */
    private final int code;
    /** 错误描述，直接展示给前端用户，需用中文写清楚 */
    private final String message;

    /**
     * 构造业务异常（默认状态码 500）。
     * 当不确定具体错误类型时使用，建议优先使用带状态码的构造器。
     *
     * @param message 错误消息
     */
    public ServiceException(String message) {
        this(500, message);
    }

    /**
     * 构造业务异常（指定状态码）。
     *
     * @param code    业务状态码，建议使用 HttpStatus 常量（如 HttpStatus.BAD_REQUEST）
     * @param message 错误消息，直接展示给用户
     */
    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /** 获取业务错误码 */
    public int getCode() { return code; }

    /** 获取错误描述 */
    @Override
    public String getMessage() { return message; }
}
