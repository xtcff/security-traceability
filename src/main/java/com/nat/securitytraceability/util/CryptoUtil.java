package com.nat.securitytraceability.util;

import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 加密工具
 *
 * @author hhf
 *
 */
@Slf4j
public class CryptoUtil {

    /**
     * SHA256散列方法
     * @param str str
     * @return String
     */
    public static String SHA256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            log.info("getSHA256 is error: [{}]", e.getMessage());
        }
        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                builder.append("0");
            }
            builder.append(temp);
        }
        return builder.toString();
    }
}
