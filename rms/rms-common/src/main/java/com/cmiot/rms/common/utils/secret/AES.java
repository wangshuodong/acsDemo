package com.cmiot.rms.common.utils.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 对称加密工具
 * 加密算法采用AES128、ECB模式、PKCS5Padding填充，外层再base64加密
 */
public class AES {

    private static final String MD5 = "MD5";
    private static final String AES = "AES";
    private static final String AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";
    private static final String ENCODING = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(com.cmiot.rms.common.utils.secret.AES.class);

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return 生成Base64编码的字符串
     */
    public static String encrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);      // 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(password));  // 初始化
            return Base64.encode(cipher.doFinal(content.getBytes(ENCODING))); // 加密后Base64
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return 解密内容
     */
    public static String decrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);       // 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(password));   // 初始化
            return new String(cipher.doFinal(Base64.decode(content))); // 解密
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 生成加密密钥（通过对密钥的MD5摘要获得）
     *
     * @param password 加密密钥
     * @return 加密密钥
     * @throws NoSuchAlgorithmException
     */
    private static SecretKeySpec getSecretKeySpec(String password) throws NoSuchAlgorithmException {

        //生成加密密钥（通过对特定数据的MD5摘要获得）
        MessageDigest digest = MessageDigest.getInstance(MD5);
        return new SecretKeySpec(digest.digest(password.getBytes()), AES);
    }
}
