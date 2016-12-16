package com.cmiot.acs.common;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZJL on 2016/3/8.
 */
public class SignDigestUtil {
    public static final String BLANK = " ";//空格符
    public static final String SEPARATOR = ":";//分隔符
    public static final String realm = "IOT-ACS";


    /**
     * 组装 WWW-Authenticate
     *
     * @return
     */
    public static final String newWwwAuthenticate(String nonce) {
        StringBuilder autBuilder = new StringBuilder("Digest")
                .append(BLANK).append("realm=").append("\"").append(realm).append("\",")
                .append(BLANK).append("nonce=").append("\"").append(nonce).append("\",")
                .append(BLANK).append("qop=").append("\"").append("auth").append("\"");
        return autBuilder.toString();
    }


    /**
     * Digest 认证
     *
     * @param map
     * @return
     */
    public static boolean digestSign(Map<String, String> map) {
        String nc = map.get("nc"); //请求计数
        String uri = map.get("uri");//请求地址
        String qop = map.get("qop");  //保护质量
        String realm = map.get("realm");//认证域
        String nonce = map.get("nonce");//服务器密码随机数
        String cnonce = map.get("cnonce"); //客户端密码随机数
        String method = map.get("method");//请求方式
        String response = map.get("response");//加密后的MD5
        String password = map.get("password");//密码
        String username = map.get("username");//用户名
        String ha1 = DigestUtils.md5Hex(username + SEPARATOR + realm + SEPARATOR + password);
        String ha2 = DigestUtils.md5Hex(method + SEPARATOR + uri);
        String hResponse = null;
        if ("auth".equals(qop) || "auth-int".equals(qop)) {
            hResponse = ha1 + SEPARATOR + nonce + SEPARATOR + nc + SEPARATOR + cnonce + SEPARATOR + qop + SEPARATOR + ha2;
        } else {
            hResponse = ha1 + SEPARATOR + nonce + SEPARATOR + ha2;
        }
        String md5vResponse = DigestUtils.md5Hex(hResponse);
        return response.equals(md5vResponse);
    }


    /**
     * 将Authenticate转换为Map
     *
     * @param authenticate
     * @return
     */
    public static final Map<String, String> authenticateToMap(String authenticate) {
        Map<String, String> authenticateMap = new HashMap<>();
        authenticate = authenticate.replaceAll("\"", "");
        authenticate = authenticate.replaceAll("Digest", "");
        String[] authenticateArray = authenticate.split(",");
        for (String s : authenticateArray) {
            int index = s.indexOf("=");
            String key = s.substring(0, index).trim();
            String value = s.substring(index + 1, s.length());
            authenticateMap.put(key, value);
        }
        return authenticateMap;
    }


}
