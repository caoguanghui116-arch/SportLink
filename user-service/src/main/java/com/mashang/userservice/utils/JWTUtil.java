package com.mashang.userservice.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mashang.userservice.domain.entity.SysUser;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;

//生成 & 解析 Token（工具类）
public class JWTUtil {

    //生成token
    public static String createToken(SysUser user){

        //获取日历对象实例
        Calendar calendar = Calendar.getInstance();

        //在当前的时间基础上加四个月，用于设置token过期时间
        calendar.add(Calendar.MONTH,4);

        //创建JWT
        String token = JWT.create().withClaim("userId", user.getUserId())
                .withClaim("username", user.getUsername())
                .withExpiresAt(calendar.getTime())//设置过期时间
                .sign(Algorithm.HMAC256("chiwhvbsugiw"));
        return token;

    }

    //验证token
    public static boolean verifyToken(String token){
        try {
            //验证token,验证不通过则报错 verify(token)验证token的方法 .build()构建一个验证方法
            JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static Long getUserId(String token) {
        try {
            // 获取id，没有id则会报错
            return JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(token).getClaim("userId").asLong();
        } catch (Exception e) {
            // 如果报错就返回null表示没有找到对应的用户
            return 0L;
        }
    }
    //从token中获取有效信息
    public static Long getUserId(HttpServletRequest request) {
        try {

            String token = request.getHeader("Authorization");

            // 获取id，没有id则会报错
            return JWT.require(Algorithm.HMAC256("chiwhvbsugiw")).build().verify(request.getHeader(token)).getClaim("userId").asLong();
        } catch (Exception e) {
            // 如果报错就返回null表示没有找到对应的用户
            return 0L;
        }
    }

    //获取Id
    public static Long getUserId() {
        return   ((SysUser) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()).getUserId();

    }


    //Feign 传递 token（你很可能会用到）
//
//    @Configuration
//    public class FeignConfig implements RequestInterceptor {
//
//        @Override
//        public void apply(RequestTemplate template) {
//
//            ServletRequestAttributes attributes =
//                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//
//            if (attributes != null) {
//                HttpServletRequest request = attributes.getRequest();
//                String token = request.getHeader("Authorization");
//
//                template.header("Authorization", token);
//            }
//        }
//    }

}
