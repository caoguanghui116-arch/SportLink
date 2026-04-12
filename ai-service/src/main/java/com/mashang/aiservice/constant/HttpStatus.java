package com.mashang.aiservice.constant;

/**
 * HTTP response status codes.
 */
public class HttpStatus {

    /** Operation successful */
    public static final int SUCCESS = 200;

    /** Resource created */
    public static final int CREATED = 201;

    /** Request accepted */
    public static final int ACCEPTED = 202;

    /** Operation executed successfully but no data returned */
    public static final int NO_CONTENT = 204;

    /** Resource moved permanently */
    public static final int MOVED_PERM = 301;

    /** Redirect */
    public static final int SEE_OTHER = 303;

    /** Resource not modified */
    public static final int NOT_MODIFIED = 304;

    /** Bad request - parameter list error (missing, format mismatch) */
    public static final int BAD_REQUEST = 400;

    /** Unauthorized */
    public static final int UNAUTHORIZED = 401;

    /** Forbidden - access restricted, authorization expired */
    public static final int FORBIDDEN = 403;

    /** Resource or service not found */
    public static final int NOT_FOUND = 404;

    /** HTTP method not allowed */
    public static final int BAD_METHOD = 405;

    /** Resource conflict or locked */
    public static final int CONFLICT = 409;

    /** Unsupported media type */
    public static final int UNSUPPORTED_TYPE = 415;

    /** Internal server error */
    public static final int ERROR = 500;

    /** Not implemented */
    public static final int NOT_IMPLEMENTED = 501;

    /** System warning */
    public static final int WARN = 601;
}
