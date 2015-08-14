package com.lody.welike.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 用于计算Hash值的工具类
 *
 * @author Lody
 */
public class HashUtils {

    private HashUtils(){}

    /**
     * 得到传入的Key的MD5值
     *
     * @param key
     * @return
     */
    public static String hashKey(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
