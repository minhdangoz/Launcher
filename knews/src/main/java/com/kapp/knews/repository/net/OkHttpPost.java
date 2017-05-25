package com.kapp.knews.repository.net;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.R;
import com.kapp.knews.helper.content.resource.ValuesHelper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by xixionghui on 2016/11/24.
 */

public class OkHttpPost {


    public static final int EXECUTE_REQUEST_ERROR = -1;


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private OkHttpPostRequestListener mRequestListener;

    OkHttpClient mOkHttpClient = new OkHttpClient();



    public OkHttpPost( OkHttpPostRequestListener requestListener) {
        this.mRequestListener = requestListener;
    }


    public void post(String url, String requestBodyData, int msgWhat) {
        //创建请求体
        RequestBody requestBody = RequestBody.create(JSON, requestBodyData);
        //创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //执行请求获取响应
        Response response = null;
        try {

            response = mOkHttpClient.newCall(request).execute();
            if (null != response) {
                mRequestListener.serverHasResponse(response,msgWhat);
            } else {
                throw new NullPointerException();
            }

        } catch (Exception e) {
            LogUtils.e(ValuesHelper.getString(R.string.execute_request_error));
            mRequestListener.serverNoResponse(e,msgWhat);
        } finally {
            LogUtils.e("=============okHttpPost.finally==============");
            if (null != response)
                response.body().close();

        }
    }

    public interface OkHttpPostRequestListener {

        //message.obj = List<T>
        void serverHasResponse(Response response, int msgWhat);

        //message.arg1 = EXECUTE_REQUEST_ERROR
        void serverNoResponse(Exception e, int msgWhat);
    }
}
