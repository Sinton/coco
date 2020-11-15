package com.github.coco.utils;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Yan
 */
public class EncryptHelper {
    /**
     * 获取MD5值
     *
     * @param input
     * @return
     */
    public static String md5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(input.getBytes(StandardCharsets.UTF_8));
            return new String(new Hex().encode(digest));
        } catch (NoSuchAlgorithmException e) {
            LoggerHelper.fmtError(EncryptHelper.class, e, "");
            return "";
        }
    }
}
