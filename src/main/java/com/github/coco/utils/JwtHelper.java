package com.github.coco.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Yan
 */
public class JwtHelper {
    /**
     * token 过期时间
     */
    private static final long EXPIRE_TIME = 30 * DateHelper.MINUTE;

    /**
     * token 私钥
     */
    private static final String TOKEN_SECRET = "coco";

    /**
     * 生成签名
     * @param username
     * @return
     */
    public static String sign(String username) {
        // 过期时间
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 私钥及加密算法
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        // 设置头信息
        HashMap<String, Object> header = new HashMap<>(2);
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        // 附带username和userID生成签名
        return JWT.create()
                  .withHeader(header)
                  .withClaim("username", username)
                  .withExpiresAt(date)
                  .sign(algorithm);
    }

    /**
     * 校验
     * @param token
     * @return
     */
    public static boolean verity(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
            return true;
        } catch (IllegalArgumentException | JWTVerificationException e) {
            return false;
        }
    }

    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("loginName").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}
