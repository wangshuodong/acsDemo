package com.cmiot.acs.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Digest认证客服端
 * Created by zjial on 2016/4/27.
 */
public class AbHttpUtil {
    /**
     * GIT请求
     *
     * @param url
     * @param userName
     * @param password
     * @throws IOException
     */
    public static CloseableHttpResponse get(String url, String userName, String password) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
        try {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setCredentialsProvider(credentialsProvider);
            localContext.setRequestConfig(requestConfig);
            CloseableHttpResponse httpResponse = httpclient.execute(new HttpGet(url), localContext);
            return httpResponse;
        } finally {
            httpclient.close();
        }
    }


    /**
     * 提交POST
     *
     * @param url
     * @param stringEntity
     * @return
     * @throws Exception
     */
    private static CloseableHttpResponse post(String url, StringEntity stringEntity) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();
        try {
            stringEntity.setContentEncoding("UTF-8");
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            return httpResponse;
        } finally {
            httpClient.close();
        }
    }


    /**
     * 发送 JSON
     *
     * @param url
     * @param jsonObject
     * @return
     * @throws Exception
     */
    public static CloseableHttpResponse sendJson(String url, JSONObject jsonObject) throws Exception {
        StringEntity stringEntity = new StringEntity(jsonObject.toJSONString());
        stringEntity.setContentType("application/json");
        return post(url, stringEntity);
    }

}
