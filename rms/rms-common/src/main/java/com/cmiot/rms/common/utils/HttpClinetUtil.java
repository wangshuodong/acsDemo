package com.cmiot.rms.common.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpClinetUtil {

	
	
	 public static Map<String, Object> post(String url, String json) {
	        Map<String, Object> responseMap = new HashMap<>();
	        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
	        DefaultHttpClient client = new org.apache.http.impl.client.DefaultHttpClient();
	        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
	        HttpPost httpPost = new HttpPost(url);
	        try {
	            StringEntity s = new StringEntity(json);
	            s.setContentEncoding("UTF-8");
	            s.setContentType("application/json");
	            httpPost.setEntity(s);
	            HttpResponse httpResponse = client.execute(httpPost);
	            HttpEntity entity = httpResponse.getEntity();
	            responseMap.put("statusCode", httpResponse.getStatusLine().getStatusCode());
	            if (entity != null) {
	                responseMap.put("contentEncoding", entity.getContentEncoding());
	                responseMap.put("responseContent", EntityUtils.toString(entity));
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return responseMap;
	    }
}
