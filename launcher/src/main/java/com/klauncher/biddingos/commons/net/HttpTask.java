package com.klauncher.biddingos.commons.net;


import com.klauncher.biddingos.commons.task.Task;
import com.klauncher.biddingos.commons.task.TaskCallback;

import java.util.Map;

/**
 * 后台HTTP任务
 */
public class HttpTask extends Task<HttpRequest, HttpResponse> {
    private static final String TAG = "HttpTask";

    public HttpTask(String url, TaskCallback<HttpRequest, HttpResponse> callback) {
        super(new HttpRequest(url), callback);
    }

    public HttpTask(HttpRequest.Method method, String url, Map<String, String> params, TaskCallback<HttpRequest, HttpResponse> callback) {
        super(new HttpRequest(method, url, params), callback);
    }

    public HttpTask(HttpRequest.Method method, String url, String reqBody, TaskCallback<HttpRequest, HttpResponse> callback) {
        super(new HttpRequest(method, url, reqBody), callback);
    }

    @Override
    public HttpResponse execute(HttpRequest input) throws Exception {

       try{
           return HttpUtils.execute(input);
       }catch (Exception e) {
           throw e;
       }
    }

}
