package com.mashang.common.constants;

/**
 * HTTP 状态码常量池 —— 集中管理所有接口返回的状态码。
 *
 * 设计原则：
 * 1. 只定义项目中实际会用到的状态码，保持精简
 * 2. 状态码含义与 HTTP 标准一致，前端可直接根据 code 做不同处理
 * 3. 与 R.java 中的 SUCCESS / FAIL 静态常量联动
 *
 * 状态码分组速查：
 * - 2xx：成功类（200 OK, 201 Created, 204 No Content）
 * - 3xx：重定向类（301 永久移动, 304 未修改）
 * - 4xx：客户端错误类（400 参数错误, 401 未授权, 403 禁止, 404 未找到）
 * - 5xx：服务端错误类（500 内部错误, 501 未实现）
 * - 6xx：自定义业务码（601 警告提示）
 */
public class HttpStatus {

    // ======================== 2xx 成功 ========================

    /** 操作成功 —— 最常用的成功状态码 */
    public static final int SUCCESS = 200;
    /** 资源已创建 —— POST 请求创建资源成功时使用 */
    public static final int CREATED = 201;
    /** 请求已接受 —— 异步处理场景，服务端已接收但尚未处理完成 */
    public static final int ACCEPTED = 202;
    /** 无内容 —— 删除成功等不需要返回 body 的场景 */
    public static final int NO_CONTENT = 204;

    // ======================== 3xx 重定向 ========================

    /** 永久重定向 —— 资源 URL 已永久变更 */
    public static final int MOVED_PERM = 301;
    /** 查看其他位置 —— 临时重定向 */
    public static final int SEE_OTHER = 303;
    /** 资源未修改 —— 配合 ETag/If-Modified-Since 做缓存校验 */
    public static final int NOT_MODIFIED = 304;

    // ======================== 4xx 客户端错误 ========================

    /** 请求参数错误 —— 校验失败、格式不正确等场景 */
    public static final int BAD_REQUEST = 400;
    /** 未认证 —— 用户未登录或 Token 失效 */
    public static final int UNAUTHORIZED = 401;
    /** 无权限 —— 已登录但无该操作权限（RBAC 鉴权失败） */
    public static final int FORBIDDEN = 403;
    /** 资源不存在 —— 查询/更新/删除不存在的记录 */
    public static final int NOT_FOUND = 404;
    /** 请求方法不支持 —— 用 GET 调 POST 接口等 */
    public static final int BAD_METHOD = 405;
    /** 资源冲突 —— 并发编辑导致的数据版本冲突 */
    public static final int CONFLICT = 409;
    /** 不支持的媒体类型 —— Content-Type 不是 application/json */
    public static final int UNSUPPORTED_TYPE = 415;

    // ======================== 5xx 服务端错误 ========================

    /** 服务器内部错误 —— 统一兜底异常码 */
    public static final int ERROR = 500;
    /** 功能未实现 —— 预留接口尚未开发 */
    public static final int NOT_IMPLEMENTED = 501;

    // ======================== 6xx 自定义业务码 ========================

    /** 业务警告 —— 操作成功但有需要注意的提示信息 */
    public static final int WARN = 601;
}
