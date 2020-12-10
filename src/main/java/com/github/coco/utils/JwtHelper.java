package com.github.coco.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Yan
 */
public class JwtHelper {
    /**
     * token 过期时间
     */
    private static final long EXPIRE_TIME = 30 * DateHelper.HOUR;

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
        // 颁发者
        String issued = "Coco";
        // 颁发时间
        Date issuedTime = new Date(System.currentTimeMillis());
        // 过期时间
        Date expireTime = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 私钥及加密算法
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        // 设置头信息
        HashMap<String, Object> header = new HashMap<>(2);
        header.put(PublicClaims.TYPE, "JWT");
        header.put(PublicClaims.ALGORITHM, "HS256");
        // 附带username生成签名
        return JWT.create()
                  .withHeader(header)
                  .withClaim("username", username)
                  .withIssuer(issued)
                  .withIssuedAt(issuedTime)
                  .withExpiresAt(expireTime)
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
            JWT.require(algorithm).withClaim("username", "admin").build().verify(token);
            return true;
        } catch (IllegalArgumentException | JWTVerificationException e) {
            return false;
        }
    }

    public static String getUsername(String token) {
        try {
            return JWT.decode(token).getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}
