package com.mashang.notificationservice.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT工具类（用于解析token获取用户信息）
 */
public class JWTUtil {

    /**
     * 验证token
     */
    public static boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取userId
     */
    public static Long getUserId(String token) {
        try {
            return JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(token).getClaim("userId").asLong();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 从request中获取userId
     */
    public static Long getUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authentication");
            if (token == null || token.isEmpty()) {
                return 0L;
            }
            return JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(token).getClaim("userId").asLong();
        } catch (Exception e) {
            return 0L;
        }
    }

}
