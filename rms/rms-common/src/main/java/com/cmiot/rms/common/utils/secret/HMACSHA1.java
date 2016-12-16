package com.cmiot.rms.common.utils.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据签名工具
 * 使用HmacSHA1生成消息摘要，外层再用base64加密
 */
public class HMACSHA1 {

    private static Logger logger = LoggerFactory.getLogger(HMACSHA1.class);

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    /**
     * 生成签名数据
     *
     * @param data 待加密的数据
     * @param key  加密使用的key
     * @return 生成Base64编码的字符串
     * @throws Exception
     */
    public static String getSignature(String data, String key) {

        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(ENCODING), MAC_NAME);
            Mac mac = Mac.getInstance(MAC_NAME);
            mac.init(signingKey);
            return Base64.encode(mac.doFinal(data.getBytes(ENCODING)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
