package com.simple.base.util;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.alibaba.fastjson.JSONObject;

/**
 * @author <a href ="mailto:yongchao.zhao@happyelements.com">yongchao.zhao</a>
 * 2016年5月18日
 */
public class HttpUtils {
	public static DefaultHttpClient getHttpClient(){
		return new DefaultHttpClient();
	}
	
	
	public static NameValuePair newNameValuePair(String name, String value) {
		return new NameValuePair(name, value);
	}

	/**
	 * 发送Post请求
	 * 
	 * @param url
	 * @param postParam
	 *            post参数
	 * @param queryStringParam
	 *            queryString参数
	 * @return
	 */
	public static JSONObject postJSONReturn(String url, NameValuePair[] queryStringParams, NameValuePair... postParam) {
		String result = post(url, queryStringParams, postParam);
		try {
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			throw new RuntimeException("JSON parse error", e);
		}
	}

	/**
	 * 发送Post请求
	 * 
	 * @param url
	 * @param postParam
	 *            post参数
	 * @return
	 */
	public static JSONObject postJSONReturn(String url, NameValuePair... postParam) {
		String result = post(url, null, postParam);
		try {
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			throw new RuntimeException("JSON parse error", e);
		}
	}

	/**
	 * 发送Post请求
	 * 
	 * @param url
	 * @param postParam
	 *            post参数
	 * @param queryStringParam
	 *            queryString参数
	 * @return
	 */
	public static String post(String url, NameValuePair[] queryStringParams, NameValuePair... postParam) {
		HttpClient httpclient = new HttpClient();
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		PostMethod postMethod = new PostMethod(url);
		postMethod.addParameters(postParam);
		if (null != queryStringParams)
			postMethod.setQueryString(queryStringParams);
		try {
			int code = httpclient.executeMethod(postMethod);
			if (code == 200) {
				return postMethod.getResponseBodyAsString();
			} else {
				throw new RuntimeException("response code is " + code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			postMethod.releaseConnection();
		}
	}

	

	/**
	 * 发送get请求,timeout 0为无限时
	 * @param url
	 * @param timeout
	 * @param queryStringParams
	 * @return
	 */
	public static String get(String url, int timeout, NameValuePair... queryStringParams) {
		HttpClient httpclient = new HttpClient();
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		httpclient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);
		GetMethod getMethod = new GetMethod(url);
		if (null != queryStringParams)
			getMethod.setQueryString(queryStringParams);
		try {
			int code = httpclient.executeMethod(getMethod);
			if (code == 200) {
				return getMethod.getResponseBodyAsString();
			} else {
				throw new RuntimeException("response code is " + code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			getMethod.releaseConnection();
		}
	}
	
	/**
	 * 发送Post请求
	 * 
	 * @param url
	 * @param postParam
	 *            post参数
	 * @param queryStringParam
	 *            queryString参数
	 * @return
	 */
	public static String post(String url,int timeout, NameValuePair[] queryStringParams, NameValuePair... postParam) {
		HttpClient httpclient = new HttpClient();
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		httpclient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, timeout);
		PostMethod postMethod = new PostMethod(url);
		postMethod.addParameters(postParam);
		if (null != queryStringParams)
			postMethod.setQueryString(queryStringParams);
		try {
			int code = httpclient.executeMethod(postMethod);
			if (code == 200) {
				return postMethod.getResponseBodyAsString();
			} else {
				throw new RuntimeException("response code is " + code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			postMethod.releaseConnection();
		}
	}

	
	
	public static HttpResponse post(String url, Map<String, String> headers, byte[] content) throws IOException{
		DefaultHttpClient client = getHttpClient();
		HttpPost postMethod = new HttpPost(url);
		ByteArrayEntity entity = new ByteArrayEntity(content);
		postMethod.setEntity(entity);
		if(headers != null){
			for(Map.Entry<String, String> entry : headers.entrySet()){
				postMethod.setHeader(entry.getKey(), entry.getValue());
			}
		}
		HttpResponse resp = client.execute(postMethod);
		StatusLine status = resp.getStatusLine();
		int code = status.getStatusCode();
		if(code >= 200 && code < 300){
			return resp;
		}else{
			throw new IOException("Response status is " + code + ", not correct");
		}
	}
	
}
