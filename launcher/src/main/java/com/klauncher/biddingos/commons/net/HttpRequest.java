package com.klauncher.biddingos.commons.net;


import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求相关信息
 */
public class HttpRequest {
    private static final String TAG = "HttpRequest";
    protected static HashMap<String, Object> headers = new HashMap<>();
    protected String reqBody;
    private Method method = Method.POST;
    private String url;
    /**
     * 请求超时时间
     */
    private int timeout = Setting.HTTP_CONNECT_TIMEOUT;
    public HttpRequest(String url)
    {
        if(bIsHeaderEmpty())
            HttpRequest.initHeaders();

        this.method = Method.GET;
        this.url = url;
    }

    public HttpRequest(Method method, String url, Map<String, String> params)
    {
        if(bIsHeaderEmpty())
            HttpRequest.initHeaders();

        this.method = method;

        String strParam = "";
        try {
            if(null!=params && !params.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(entry.getKey()).append('=');
                    sb.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                    sb.append('&');
                }
                sb.deleteCharAt(sb.length() - 1);
                strParam = sb.toString();
            }

        }catch (UnsupportedEncodingException e) {
            LogUtils.e(TAG, e.toString());
        }

        switch (method) {
            case GET:
                url += "?" + strParam;
                break;
            case POST:
                this.reqBody = strParam;
                break;
        }

        this.url = url;
    }

    public HttpRequest(Method method, String url, String reqBody)
    {
        if(bIsHeaderEmpty())
            HttpRequest.initHeaders();

        this.method = method;
        this.url = url;
        this.reqBody = reqBody;
    }

    /**
     * 初始化HTTP HEADER数组信息
     */
    public static synchronized void initHeaders() {
        if(headers.isEmpty()) {
//            headers.put("Content-Encoding", "gzip");
            headers.put("User-Agent", HttpUtils.getUserAgent());
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Method getMethod() {
        return this.method;
    }

    public String getUrl() { return this.url; }

    public String getReqBody() { return this.reqBody; }

    public boolean bIsHeaderEmpty() {
        return headers.isEmpty();
    }

    public Object getHeaderValue(String key) {
        return headers.get(key);
    }

    /**
     * 获取Post加密后的内容
     */
    public String getAESreqBody() {
        return AESUtils.encode(Setting.SECRET, this.reqBody);
    }


    public static enum Method {
        POST, GET
    }


}
