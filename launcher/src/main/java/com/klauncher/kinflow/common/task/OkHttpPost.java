package com.klauncher.kinflow.common.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.klauncher.kinflow.cards.model.sougou.SougouSearchArticle;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.utilities.CollectionsUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by xixionghui on 16/9/23.
 */
public class OkHttpPost {


    /**
     * 制定json传输
     */
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private Handler mHandler;
    private Message mMessage;

    OkHttpClient mOkHttpClient = new OkHttpClient();

    public OkHttpPost (Handler handler,int msgWhat) {
        this.mHandler = handler;
        this.mMessage = MessageFactory.createMessage(msgWhat);
    }

    public String post(String url, String requestBodyData){
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
             parseResponse(response);
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseResponse(Response response) {
        try {
            mMessage.arg1 = AsynchronousGet.CONNECTION_ERROR;
            //服务端有响应
            if (response.isSuccessful()) {//搜狗搜索的服务器有响应了,但是不一定获取数据成功.
                List<SougouSearchArticle> sougouSearchArticleList = parseSouGouSearchNews(new JSONObject(response.body().string()));
                /*
                for (SougouSearchArticle news :
                        sougouSearchArticleList) {
                    Log.w("kinflow", "获取到的搜狗新闻详情:" + news.toString());
                }
                */
                if (CollectionsUtils.collectionIsNull(sougouSearchArticleList)) {
                    mMessage.arg1 = AsynchronousGet.OBTAIN_RESULT_NULL;
                } else {
                    mMessage.arg1 = AsynchronousGet.SUCCESS;
                    mMessage.obj = sougouSearchArticleList;
                }
            }else {
                //服务器端无响应
                mMessage.arg1 = AsynchronousGet.OBTAIN_RESULT_NULL;
            }
            mHandler.sendMessage(mMessage);
        } catch (Exception e) {
            Log.w("kinflow","解析搜狗响应体,出现错误:"+e.getMessage());
        }

    }

    private List<SougouSearchArticle> parseSouGouSearchNews(JSONObject jsonObject) {
        List<SougouSearchArticle> sougouSearchArticleList = new ArrayList<>();
        try {
            int status = jsonObject.optInt("status",0);//默认为出错
            if (status == SougouSearchArticle.RESPONSE_STATUS_ERROR) {
                    Log.w("Kinflow","搜狗搜索服务器有响应,但是状态错误,导致没有数据");
            }
            if (status == SougouSearchArticle.RESPONSE_STATUS_SUCCESS) {//返回数据正常----正式开始解析数据
                JSONArray sougouSearchNewsJsonArray = jsonObject.optJSONArray("result");
                if (null==sougouSearchNewsJsonArray||
                        sougouSearchNewsJsonArray.length()==0) {
                    Log.w("Kinflow","获取到搜狗搜索新闻数据为空");
                }else {
                    for (int i = 0; i < sougouSearchNewsJsonArray.length(); i++) {
                        sougouSearchArticleList.add(new SougouSearchArticle(sougouSearchNewsJsonArray.optJSONObject(i)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sougouSearchArticleList;
    }
}
