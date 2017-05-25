package com.kapp.kinflow.business.http;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.kapp.kinflow.business.http.https.SSLSocketFactoryUtil;
import com.kapp.kinflow.business.http.log.LoggerInterceptor;
import com.kapp.kinflow.business.http.persistentcookiejar.ClearableCookieJar;
import com.kapp.kinflow.business.http.persistentcookiejar.PersistentCookieJar;
import com.kapp.kinflow.business.http.persistentcookiejar.cache.SetCookieCache;
import com.kapp.kinflow.business.http.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * description：okhttp简易封装辅助类
 * <br>author：caowugao
 * <br>time： 2017/04/24 10:53
 */

public class OKHttpUtil {

    private final static int TIMEOUT_SECONDS_CONNECT = 10;
    private final static int TIMEOUT_SECONDS_READ = 10;
    private final static int TIMEOUT_SECONDS_WRITE = 60;

    //    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)//设置读取超时时间
//            .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)//设置写的超时时间
//            .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)//设置连接超时时间
//            .build();
    private static OkHttpClient client = null;
    private static final String TAG = OKHttpUtil.class.getSimpleName();

    //MediaType
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final MediaType MEDIA_TYPE_GIF = MediaType.parse("image/gif");
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml; charset=utf-8");

    private static SparseArray<Call> taskMap = new SparseArray<Call>();//用于存放请求的任务
    private static SparseArray<OnRequestListener> listenerMap = new SparseArray<OnRequestListener>();//用于存放请求的监听器

    private static final boolean DEBUG = true;

    //以下是结果返回类型定义
    public static final int RESPONSE_TYPE_STRING = 1;
    public static final int RESPONSE_TYPE_BYTE = 2;
    public static final int RESPONSE_TYPE_INPUTSTREAM = 3;
    public static final int RESPONSE_TYPE_READER = 4;

    //以下是handle用到的
    private static final int MSG_SUCCESS = 1;
    private static final int MSG_FAILURE = -1;

    /**
     * 放在Application中初始化,每次请求都要加请求头Cache-Control: max-stale=3600，服务端设置响应头Cache-Control: max-age=9600
     *
     * @param context
     * @param applictionName
     * @param certificates   自签名证书
     */
    public static void init(Context context, String applictionName, InputStream... certificates) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File(context.getCacheDir().getPath() + File.separator + applictionName);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        Cache cache = new Cache(cacheDirectory, cacheSize);
        ClearableCookieJar cookieJar1 = new PersistentCookieJar(new SetCookieCache(),
                new SharedPrefsCookiePersistor(context.getApplicationContext()));
        //        CookieJarImpl cookieJar1 = new CookieJarImpl(new MemoryCookieStore());

        if (null != certificates) {
            SSLSocketFactory sslSocketFactory = SSLSocketFactoryUtil.getSSLSocketFactory(certificates);
            client = new OkHttpClient.Builder()
                    .readTimeout(TIMEOUT_SECONDS_READ, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(TIMEOUT_SECONDS_WRITE, TimeUnit.SECONDS)//设置写的超时时间
                    .connectTimeout(TIMEOUT_SECONDS_CONNECT, TimeUnit.SECONDS)//设置连接超时时间
                    .cache(cache)
                    .addInterceptor(new LoggerInterceptor("TAG")).cookieJar(cookieJar1)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).sslSocketFactory(sslSocketFactory).build();
        } else {
            client = new OkHttpClient.Builder()
                    .readTimeout(TIMEOUT_SECONDS_READ, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(TIMEOUT_SECONDS_WRITE, TimeUnit.SECONDS)//设置写的超时时间
                    .connectTimeout(TIMEOUT_SECONDS_CONNECT, TimeUnit.SECONDS)//设置连接超时时间
                    .cache(cache)
                    .addInterceptor(new LoggerInterceptor(TAG)).cookieJar(cookieJar1)
                    .build();
        }

        HandlerThread workThread = new HandlerThread("IntentService[" + TAG + "]");
        workThread.start();
        workHandler = new WorkHandler(workThread.getLooper());

        mainHandler = new MainHandler(Looper.getMainLooper());
    }

    public interface OnRequestListener {
        void onFailure(int requestCode, String msg);

//        /**
//         * @param requestCode
//         * @param fullResponse   返回okhttp原来的response，一定要关闭.response.close();
//         * @param simpleResponse 返回指定类型的数据
//         */
//        void onResponse(int requestCode, Response fullResponse, Object simpleResponse);

        /**
         * @param requestCode
         * @param simpleResponse 返回指定类型的数据
         */
        void onResponse(int requestCode, Object simpleResponse);
    }

    private static WorkHandler workHandler;
    private static MainHandler mainHandler;

    private static class WorkHandler extends Handler {
        public WorkHandler(Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            logDebug("WorkHandler : thread name=" + Thread.currentThread().getName());
            switch (msg.what) {
                case MSG_SUCCESS:
                    int requestCode1 = msg.arg1;
                    OnRequestListener listener1 = listenerMap.get(requestCode1);
                    handleResponseResult(msg, requestCode1, listener1);
                    break;
                case MSG_FAILURE:
                    int requestCode2 = msg.arg1;
                    String error = (String) msg.obj;
//                    OnRequestListener listener2 = listenerMap.get(requestCode2);
//                    if (null != listener2) {
//                        listener2.onFailure(requestCode2, error);
//                    }
                    sendMainMessage(MSG_FAILURE, RESPONSE_TYPE_STRING, requestCode2, error);
                    break;
            }
        }
    }

    private static class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            logDebug("MainHandler : thread name=" + Thread.currentThread().getName());

//            Message mainMsg = mainHandler.obtainMessage();
//            mainMsg.what = resultTag;
//            mainMsg.arg1 = resultType;
//            mainMsg.arg2 = requestCode;
//            mainMsg.obj = result;
//            mainHandler.sendMessage(mainMsg);
            int resultType = msg.arg1;
            int requestCode = msg.arg2;
            Object result = msg.obj;
            OnRequestListener listener = listenerMap.get(requestCode);
            if (null != listener) {
                if (MSG_SUCCESS == msg.what) {
                    listener.onResponse(requestCode, result);
                    listenerMap.remove(requestCode);
                } else if (MSG_FAILURE == msg.what) {
                    listener.onFailure(requestCode, null == result ? null : result.toString());
                    listenerMap.remove(requestCode);
                }
            }
        }

    }

    private static class BigStringRequestBody extends RequestBody {

        private String bigString;

        public BigStringRequestBody(String bigString) {
            this.bigString = bigString;
        }

        /**
         * Returns the Content-Type header for this body.
         */
        @Override
        public MediaType contentType() {
            return MEDIA_TYPE_MARKDOWN;
        }

        /**
         * Writes the content of this request to {@code out}.
         *
         * @param sink
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.writeUtf8(bigString);
        }
    }


    private OKHttpUtil() {
    }

    private static void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    private static void logError(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }
//
//    /**
//     * 处理响应成功的结果
//     *
//     * @param msg
//     * @param requestCode
//     * @param listener
//     */
//    private static void handleResponseResult(Message msg, int requestCode, OnRequestListener listener) {
//        if (null != listener) {
//            Response response = (Response) msg.obj;
//            int responseType = msg.arg2;
//            switch (responseType) {
//                case RESPONSE_TYPE_STRING://string
//                    try {
//                        String result = response.body().string();
//                        listener.onResponse(requestCode, response, result);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        listener.onFailure(requestCode, e.getMessage());
//                    }
//                    break;
//                case RESPONSE_TYPE_BYTE://byte
//                    try {
//                        byte[] bytes = response.body().bytes();
//                        listener.onResponse(requestCode, response, bytes);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        listener.onFailure(requestCode, e.getMessage());
//                    }
//                    break;
//                case RESPONSE_TYPE_INPUTSTREAM://inputstream
//                    InputStream is = response.body().byteStream();
//                    listener.onResponse(requestCode, response, is);
//                    break;
//                case RESPONSE_TYPE_READER://reader
//                    Reader reader = response.body().charStream();
//                    listener.onResponse(requestCode, response, reader);
//                    break;
//            }
//        }
//    }

    /**
     * 处理响应成功的结果
     *
     * @param msg
     * @param requestCode
     * @param listener
     */
    private static void handleResponseResult(Message msg, int requestCode, OnRequestListener listener) {
        if (null != listener) {
            Response response = (Response) msg.obj;
            int responseType = msg.arg2;
            switch (responseType) {
                case RESPONSE_TYPE_STRING://string
                    try {
                        String result = response.body().string();
//                        listener.onResponse(requestCode, response, result);
                        sendMainMessage(MSG_SUCCESS, responseType, requestCode, result);

                    } catch (IOException e) {
                        e.printStackTrace();
//                        listener.onFailure(requestCode, e.getMessage());
                        sendMainMessage(MSG_FAILURE, responseType, requestCode, e.getMessage());
                    }
                    break;
                case RESPONSE_TYPE_BYTE://byte
                    try {
                        byte[] bytes = response.body().bytes();
//                        listener.onResponse(requestCode, response, bytes);
                        sendMainMessage(MSG_SUCCESS, responseType, requestCode, bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        listener.onFailure(requestCode, e.getMessage());
                        sendMainMessage(MSG_FAILURE, responseType, requestCode, e.getMessage());
                    }
                    break;
                case RESPONSE_TYPE_INPUTSTREAM://inputstream
                    InputStream is = response.body().byteStream();
//                    listener.onResponse(requestCode, response, is);
                    sendMainMessage(MSG_SUCCESS, responseType, requestCode, is);
                    break;
                case RESPONSE_TYPE_READER://reader
                    Reader reader = response.body().charStream();
//                    listener.onResponse(requestCode, response, reader);
                    sendMainMessage(MSG_SUCCESS, responseType, requestCode, reader);
                    break;
            }
            response.close();
        }
    }

    private static void sendMainMessage(int resultTag, int resultType, int requestCode, Object result) {
        Message mainMsg = mainHandler.obtainMessage();
        mainMsg.what = resultTag;
        mainMsg.arg1 = resultType;
        mainMsg.arg2 = requestCode;
        mainMsg.obj = result;
        mainHandler.sendMessage(mainMsg);
    }

    private static String parseResponseType(int responseType) {
        switch (responseType) {
            case RESPONSE_TYPE_STRING:
                return "String";
            case RESPONSE_TYPE_BYTE:
                return "Byte";
            case RESPONSE_TYPE_INPUTSTREAM:
                return "InputStream";
            case RESPONSE_TYPE_READER:
                return "Reader";
        }
        return "unknown responseType!!!";
    }


    /**
     * 以get方式请求数据，以字符串的形式返回结果
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param requestCode 用于标记是哪次的请求，后面可用来取消请求
     * @param listener
     */
    public static void get(String url, Map<String, String> headers, int requestCode, OnRequestListener
            listener) {
        get(url, headers, requestCode, RESPONSE_TYPE_STRING, listener);
    }

    /**
     * 以get方式请求数据，返回结果由responseType决定
     *
     * @param url
     * @param headers      请求头，不用就传null
     * @param requestCode
     * @param responseType
     * @param listener
     */
    public static void get(String url, Map<String, String> headers, int requestCode, int responseType, OnRequestListener
            listener) {

        logDebug("get(...) 请求得url=" + url + " ,requestCode=" + requestCode + " ,responseType=" + parseResponseType
                (responseType));

        Request.Builder requestBuilder = new Request.Builder().url(url);
        addHeaders(headers, requestBuilder);
        Request request = requestBuilder.build();
        startRequest(request, requestCode, responseType, listener);
    }

    /**
     * 添加请求头
     *
     * @param headers
     * @param requestBuilder
     */
    private static void addHeaders(Map<String, String> headers, Request.Builder requestBuilder) {
        if (null != headers && !headers.isEmpty()) {
            Set<Map.Entry<String, String>> set = headers.entrySet();
            for (Map.Entry<String, String> item : set) {
                requestBuilder.addHeader(item.getKey(), item
                        .getValue());
            }
        }
    }

    /**
     * 开始请求
     *
     * @param request
     * @param requestCode
     * @param responseType
     * @param listener
     */
    private static void startRequest(Request request, final int requestCode, final int responseType,
                                     OnRequestListener listener) {
        Call call = client.newCall(request);
        taskMap.put(requestCode, call);
        if (null != listener) {
            listenerMap.put(requestCode, listener);
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                taskMap.remove(requestCode);
                logError("startRequest(...) onFailure ===>" + e.getMessage());
                Message message = workHandler.obtainMessage();
                message.what = MSG_FAILURE;
                message.arg1 = requestCode;
                message.obj = e.getMessage();
                message.sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                taskMap.remove(requestCode);
                logDebug("startRequest(...) onResponse ....");
                if (response.isSuccessful()) {
                    Message message = workHandler.obtainMessage();
                    message.what = MSG_SUCCESS;
                    message.arg1 = requestCode;
                    message.arg2 = responseType;
                    message.obj = response;
                    message.sendToTarget();
                } else {
                    String error = "响应成功，但未知原因response not successful";
                    logDebug("startRequest(...) onResponse " + error);
                    Message message = workHandler.obtainMessage();
                    message.what = MSG_FAILURE;
                    message.arg1 = requestCode;
                    message.obj = error;
                    message.sendToTarget();
                }
            }
        });
    }

    /**
     * 以post方式提交markdown文档的字符串数据(小于1M)，以字符串的形式返回结果。提交文档数据不建议用该方法
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param markdown
     * @param requestCode
     * @param listener
     */
    public static void postBySmallString(String url, Map<String, String> headers, String markdown, int
            requestCode, OnRequestListener listener) {

        logDebug("postBySmallString(...) 请求得url=" + url + ",请求头headers=" + (null == headers ? null : headers.toString
                ()) + "" +
                " ,markdown=" + markdown + " ,requestCode=" + requestCode + " ," +
                "responseType=" + parseResponseType
                (RESPONSE_TYPE_STRING));

        RequestBody body = RequestBody.create(MEDIA_TYPE_MARKDOWN, markdown);
        packagePostRequest(url, headers, requestCode, RESPONSE_TYPE_STRING, body, listener);
    }

    /**
     * 以post方式提交markdown文档的字符串数据，以字符串的形式返回结果。提交文档数据专用
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param markdown
     * @param requestCode
     * @param listener
     */
    public static void postByBigString(String url, Map<String, String> headers, String markdown, int
            requestCode, OnRequestListener listener) {
        logDebug("postByBigString(...) 请求得url=" + url + ",请求头headers=" + (null == headers ? null : headers.toString()
        ) + "" +
                " ,markdown=" + markdown + " ,requestCode=" + requestCode + " ," +
                "responseType=" + parseResponseType
                (RESPONSE_TYPE_STRING));

        RequestBody body = new BigStringRequestBody(markdown);
        packagePostRequest(url, headers, requestCode, RESPONSE_TYPE_STRING, body, listener);
    }

    /**
     * 以post方式提交单个markdown文件，以字符串的形式返回结果。
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param markdown
     * @param requestCode
     * @param listener
     */
    public static void postMarkdownFile(String url, Map<String, String> headers, File markdown, int
            requestCode, OnRequestListener listener) {
        logDebug("postMarkdownFile(...) 请求得url=" + url + ",请求头headers=" + (null == headers ? null : headers.toString
                ()) + "" +
                " ,文件File=" + (null == markdown ? null : markdown.getPath()) + " ,requestCode=" + requestCode + " ," +
                "responseType=" + parseResponseType
                (RESPONSE_TYPE_STRING));

        RequestBody body = RequestBody.create(MEDIA_TYPE_MARKDOWN, markdown);
        packagePostRequest(url, headers, requestCode, RESPONSE_TYPE_STRING, body, listener);
    }
//#
//    public static void postFiles(String url, Map<String, String> headers, Map<MediaType, File> fileMap, int
//            requestCode, OnRequestListener listener) {
//
//        logDebug("postFiles(...) 请求得url=" + url + ",请求头headers=" + (null == headers ? null : headers.toString()) +
// "" +
//                " ,多文件Files=" + (null == fileMap ? null : fileMap.toString()) + " ,requestCode=" + requestCode + "
// ," +
//                "responseType=" + parseResponseType
//                (RESPONSE_TYPE_STRING));
//
//        MultipartBody.Builder multiBuilder=new MultipartBody.Builder().setType(MultipartBody.FORM);
//
//
//        RequestBody body =multiBuilder.build();
//        packagePostRequest(url, headers, requestCode, RESPONSE_TYPE_STRING, body, listener);
//    }

    /**
     * 组装post方式的请求
     *
     * @param url
     * @param headers
     * @param requestCode
     * @param responseType
     * @param body
     * @param listener
     */
    private static void packagePostRequest(String url, Map<String, String> headers, int requestCode, int
            responseType, RequestBody body, OnRequestListener listener) {
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        addHeaders(headers, requestBuilder);
        Request request = requestBuilder.build();
        startRequest(request, requestCode, responseType, listener);
    }

    /**
     * post方式提交json数据，以字符串的形式返回结果
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param jsonParam
     * @param requestCode
     * @param listener
     */
    public static void postByJson(String url, Map<String, String> headers, String jsonParam, final int requestCode,
                                  OnRequestListener listener) {
        postByJson(url, headers, jsonParam, requestCode, RESPONSE_TYPE_STRING, listener);
    }


    /**
     * post方式提交json数据，返回结果由responseType决定
     *
     * @param url
     * @param headers      请求头，不用就传null
     * @param jsonParam
     * @param requestCode
     * @param responseType
     * @param listener
     */
    public static void postByJson(String url, Map<String, String> headers, String jsonParam, int requestCode, int
            responseType,
                                  OnRequestListener listener) {
        logDebug("postByJson(...) 请求得url=" + url + ",请求头headers=" + (null == headers ? null : headers.toString()) + "" +
                " ,jsonParam=" + jsonParam + " ,requestCode=" + requestCode + " ," +
                "responseType=" + parseResponseType
                (responseType));

        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonParam);
        packagePostRequest(url, headers, requestCode, responseType, body, listener);
    }

    /**
     * post方式提交键值对数据，以字符串的形式返回结果
     *
     * @param url
     * @param headers     请求头，不用就传null
     * @param params
     * @param requestCode
     * @param listener
     */
    public static void post(String url, Map<String, String> headers, Map<String, String> params, int requestCode,
                            OnRequestListener listener) {
        post(url, headers, params, requestCode, RESPONSE_TYPE_STRING, listener);
    }

    /**
     * 以post方式提交键值对，返回结果由responseType决定
     *
     * @param url
     * @param headers      请求头，不用就传null
     * @param params
     * @param requestCode
     * @param responseType
     * @param listener
     */
    public static void post(String url, Map<String, String> headers, Map<String, String> params, int requestCode, int
            responseType,
                            OnRequestListener listener) {

        logDebug("post(...) 请求得url=" + url + " ,请求头headers=" + (null == headers ? null : headers.toString()) +
                "" +
                " ,jsonParam=" + params.toString() + " ,requestCode=" +
                requestCode + " ," +
                "responseType=" + parseResponseType
                (responseType));

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if (null != params && !params.isEmpty()) {
            Set<Map.Entry<String, String>> set = params.entrySet();
            for (Map.Entry<String, String> item : set) {
                bodyBuilder.add(item.getKey(), item.getValue());
            }
        }
        RequestBody body = bodyBuilder.build();
        packagePostRequest(url, headers, requestCode, responseType, body, listener);

    }


    public static void cancel(int requestCode) {
        Call call = taskMap.get(requestCode);
        if (null != call) {
            try {
                call.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            taskMap.remove(requestCode);
            listenerMap.remove(requestCode);
        }
    }

    public static void release() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                workHandler.getLooper().quitSafely();
                mainHandler.getLooper().quitSafely();
            } else {
                workHandler.getLooper().quit();
                mainHandler.getLooper().quit();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


