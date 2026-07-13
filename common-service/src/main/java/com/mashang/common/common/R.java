package com.mashang.common.common;

import com.mashang.common.constants.HttpStatus;

import java.io.Serializable;

/**
 * 统一响应体 —— 所有 Controller 接口的返回格式。
 *
 * 设计目标：
 * 1. 前后端分离架构下，前端需要统一的数据结构来解析后端响应
 * 2. 通过静态工厂方法限制构造，避免调用方随意 new 对象
 * 3. 提供语义化的 ok/fail 方法，让业务代码更具可读性
 *
 * 使用示例：
 * <pre>
 *   // 成功返回，无数据
 *   return R.ok();
 *
 *   // 成功返回，带数据
 *   return R.ok(userList);
 *
 *   // 失败返回，自定义消息
 *   return R.fail("用户名已存在");
 *
 *   // 根据数据库影响行数自动判断
 *   return R.toResult(mapper.insert(entity));
 * </pre>
 *
 * @param <T> 响应数据的类型
 */
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 成功状态码（200），引用 HttpStatus 常量，统一管理 */
    public static final int SUCCESS = HttpStatus.SUCCESS;
    /** 失败状态码（500），引用 HttpStatus 常量，统一管理 */
    public static final int FAIL = HttpStatus.ERROR;

    /** 状态码，200 表示成功，其它表示失败 */
    private int code;
    /** 提示消息，成功时为 "操作成功"，失败时包含具体错误原因 */
    private String msg;
    /** 响应数据，泛型支持任意类型 */
    private T data;

    // ======================== boolean/int → R 转换 ========================

    /**
     * 根据布尔值构建响应体。
     * 常用于 Service 层返回 boolean 的场景，true → ok，false → fail。
     *
     * @param result 操作结果（true=成功, false=失败）
     * @return R&lt;Void&gt;，不带数据的响应体
     */
    public static R<Void> toResult(boolean result) {
        return result ? R.ok() : R.fail();
    }

    /**
     * 根据数据库影响行数构建响应体。
     * MyBatis / MyBatis-Plus 写操作返回 int 行数，大于 0 表示成功。
     *
     * @param rows 数据库操作影响的行数
     * @return R&lt;Void&gt;，不带数据的响应体
     */
    public static R<Void> toResult(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    // ======================== 成功响应快捷方法 ========================

    /**
     * 成功响应（无数据）。
     * 适用于删除、修改等不需要返回数据的操作。
     */
    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    /**
     * 成功响应（带数据）。
     * 适用于查询列表、详情等需要返回数据的操作。
     *
     * @param data 返回给前端的数据
     */
    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    /**
     * 成功响应（带数据 + 自定义消息）。
     *
     * @param data 返回给前端的数据
     * @param msg  自定义成功提示
     */
    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    // ======================== 失败响应快捷方法 ========================

    /**
     * 失败响应（无数据，默认消息 "操作失败"）。
     */
    public static <T> R<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    /**
     * 失败响应（自定义错误消息）。
     * 适用于业务校验失败场景，如 "用户名已存在"、"无权删除他人动态" 等。
     *
     * @param msg 错误提示消息
     */
    public static <T> R<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    /**
     * 失败响应（自定义状态码 + 消息）。
     * 适用于需要精确 HTTP 状态码的场景，如 400 参数错误、401 未授权等。
     *
     * @param code 业务状态码（来自 HttpStatus 常量）
     * @param msg  错误提示消息
     */
    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    /**
     * 失败响应（带数据）。
     * 适用于失败时需要返回部分数据的场景。
     *
     * @param data 返回的数据
     */
    public static <T> R<T> fail(T data) {
        return restResult(data, FAIL, "操作失败");
    }

    /**
     * 失败响应（带数据 + 自定义消息）。
     */
    public static <T> R<T> fail(T data, String msg) {
        return restResult(data, FAIL, msg);
    }

    /**
     * 失败响应（自定义状态码 + 消息 + 数据）。
     *
     * @param code 业务状态码
     * @param msg  错误提示消息
     * @param data 返回的数据
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        return restResult(data, code, msg);
    }

    // ======================== 内部构造器 ========================

    /**
     * 私有构造器，统一创建 R 实例。
     * 所有工厂方法最终都调用此方法，确保 code/msg/data 三者一起赋值，避免遗漏。
     */
    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    // ======================== 带动态消息的快捷方法 ========================

    /**
     * 根据布尔值构建响应，消息自动拼接 "成功"/"失败" 后缀。
     * 如：R.to(true, "保存") → msg = "保存成功"
     *
     * @param b   操作结果
     * @param msg 操作描述（不含 "成功"/"失败" 后缀）
     */
    public static R<Void> to(boolean b, String msg) {
        return b ? R.ok(null, msg + "成功") : R.fail(msg + "失败");
    }

    /**
     * 根据布尔值构建响应，带数据 + 消息自动拼接后缀。
     *
     * @param b    操作结果
     * @param msg  操作描述
     * @param data 返回数据
     */
    public static <T> R<T> to(boolean b, String msg, T data) {
        return b ? R.ok(data, msg + "成功") : R.fail(msg + "失败");
    }

    // ======================== 状态判断工具 ========================

    /**
     * 判断响应是否失败。
     * 常用于调用远程服务后判断结果。
     */
    public static <T> boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    /**
     * 判断响应是否成功（code == 200）。
     */
    public static <T> boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }

    // ======================== Getter / Setter ========================

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
