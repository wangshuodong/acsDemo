package com.cmiot.rms.common.utils.secret;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author xukai
 * Date 2016/2/1
 */
public class Decript {

    public static String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args){
      System.out.println(SHA1("123456"));//7c4a8d09ca3762af61e59520943dc26494f8941b
    }

}
