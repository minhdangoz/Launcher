package com.klauncher.kinflow.common.task;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xixionghui on 2016/3/31.
 */
public class YiDianTask {

    //    private final OkHttpClient client = new OkHttpClient();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
    private YiDianQuestCallBack mCallBack;
    private Context mContext;
    private int mOurDefineChannelId;
    private Handler mHandler;
    Handler.Callback handleCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.arg1) {
                case YiDianQuestCallBack.ERROR_CONNECT:
                    mCallBack.onError(YiDianQuestCallBack.ERROR_CONNECT);
                    break;
                case YiDianQuestCallBack.ERROR_RESPONSE:
                    mCallBack.onError(YiDianQuestCallBack.ERROR_CONNECT);
                    break;
                case YiDianQuestCallBack.ERROR_DATA_NULL:
                    mCallBack.onError(YiDianQuestCallBack.ERROR_CONNECT);
                    break;
                case YiDianQuestCallBack.ERROR_PARSE:
                    mCallBack.onError(YiDianQuestCallBack.ERROR_CONNECT);
                    break;
                case YiDianQuestCallBack.SUCCESS:
                    switch (msg.what) {
                        case MessageFactory.MESSAGE_WHAT_TIMESTAMP:
                            String timestamp = (String) msg.obj;
                            String requestUrl = getRequestUrl(timestamp);
                            getYdTask(requestUrl);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN:
                            int offset = CommonShareData.getInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, 0) + 5;
                            CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, offset);
                            mCallBack.onSuccess((List<YiDianModel>) msg.obj);
                            break;
                    }
                    break;
            }
            return true;
        }
    };

    public String getRequestUrl(String timestamp) {
//        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT_DEBUG);
        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT_RELEASE);
//        /*
        if (null == timestamp)
            timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
//        int timestamp = (int)((new Date().getTime())/1000);
        String nonce = CommonUtils.getInstance().getRandomString(5);//生成5个随机字符串
        //String appkey,String nonce,String timestamp
        String secretkey = CommonUtils.getSecretkey(Const.YIDIAN_APPKEY, nonce, timestamp);
        stringBuilder.append("?appid=").append(Const.YIDIAN_APPID);
        stringBuilder.append("&secretkey=").append(secretkey);
        stringBuilder.append("&timestamp=").append(timestamp);
        stringBuilder.append("&nonce=").append(nonce);
//        */
        //获取偏移量,如果没有获取到偏移量则使用默认偏移量0
        int mOffSet = CommonShareData.getInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, 0);
        stringBuilder.append("&channel_id=").append(CardIdMap.getYiDianChannelId(mOurDefineChannelId));//channelId
        stringBuilder.append("&offset=").append(String.valueOf(mOffSet));//偏移量
        stringBuilder.append("&count=").append(String.valueOf(5));//count 为5 不变
        return stringBuilder.toString();
    }

    /**
     * @param context
     * @param ourDefineChannelId 不同的channelID,用于控制offset
     * @param callBack
     */
    public YiDianTask(Context context, int ourDefineChannelId, YiDianQuestCallBack callBack) {
        this.mContext = context;
        this.mOurDefineChannelId = ourDefineChannelId;
        this.mCallBack = callBack;
        mHandler = new Handler(handleCallback);
    }

    public void run() {
        Request request = new Request.Builder()
                .url(Const.URL_TIMESTAMP)
                .build();
        final Message msgTimestamp = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_TIMESTAMP);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //网络连接错误
                msgTimestamp.arg1 = YiDianQuestCallBack.ERROR_CONNECT;
                mHandler.sendMessage(msgTimestamp);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {//响应失败
                    msgTimestamp.arg1 = YiDianQuestCallBack.ERROR_RESPONSE;
                    response.body().close();
                    return;
                }

                String responseBodyStr = null;
                try {
                    responseBodyStr = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                    msgTimestamp.arg1 = YiDianQuestCallBack.ERROR_RESPONSE;
                    mHandler.sendMessage(msgTimestamp);
                    log("时间戳响应失败,发生IOException");
                    response.body().close();
                }
                if (!TextUtils.isEmpty(responseBodyStr))
                    parseTimeStamp(msgTimestamp, responseBodyStr);
                response.body().close();
            }
        });
    }

    private void parseTimeStamp(Message msgTimestamp, String responseBody) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(responseBody);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            int time = (int) (jsonObject.getLong("time") / 1000);
            msgTimestamp.arg1 = YiDianQuestCallBack.SUCCESS;
            msgTimestamp.obj = String.valueOf(time);
        } catch (JSONException e) {
            e.printStackTrace();
            msgTimestamp.arg1 = YiDianQuestCallBack.ERROR_PARSE;
        } finally {
            mHandler.sendMessage(msgTimestamp);
        }
    }

    public void getYdTask(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        final Message msgYiDian = MessageFactory.createMessage(MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //网络连接错误
                msgYiDian.arg1 = YiDianQuestCallBack.ERROR_CONNECT;
                mHandler.sendMessage(msgYiDian);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {//响应失败
                    msgYiDian.arg1 = YiDianQuestCallBack.ERROR_RESPONSE;
                    response.body().close();
                    return;
                }

                String responseBodyStr = null;
                try {
                    responseBodyStr = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                    msgYiDian.arg1 = YiDianQuestCallBack.ERROR_RESPONSE;
                    mHandler.sendMessage(msgYiDian);
                    log("单独获取一点咨询响应失败,发生IOException");
                }
                if (!TextUtils.isEmpty(responseBodyStr))
                    parseYiDian(msgYiDian, responseBodyStr);
                response.body().close();
            }
        });
    }

    private void parseYiDian(Message msgYiDian, String responseBody) {
//        handler.sendMessage(msg);
        List<YiDianModel> yiDianModelList = new ArrayList<>();
        try {
            JSONObject allYiDianObject = new JSONObject(responseBody);
            int code = allYiDianObject.getInt("code");
            if (code != 0) {//获取失败
                msgYiDian.arg1 = YiDianQuestCallBack.ERROR_DATA_NULL;
            } else {//获取成功
                JSONArray jsonArrayYiDianModel = allYiDianObject.getJSONArray("result");
                int length = jsonArrayYiDianModel.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonYidianMoedel = jsonArrayYiDianModel.getJSONObject(i);
                    String title = jsonYidianMoedel.getString("title");
                    String docid = jsonYidianMoedel.getString("docid");
                    String url = jsonYidianMoedel.getString("url");
                    String date = jsonYidianMoedel.getString("date");
                    String source = jsonYidianMoedel.getString("source");
                    //图片
                    String[] images = null;
                    if (jsonYidianMoedel.has("images")) {//包含图片
                        JSONArray jsonArrayImages = jsonYidianMoedel.getJSONArray("images");
                        int imageLength = jsonArrayImages.length();
                        images = new String[imageLength];
                        for (int j = 0; j < imageLength; j++) {
                            String image = jsonArrayImages.getString(j);
                            images[j] = image;
                        }
                    } else {//不包含图片
                        images = new String[0];
                    }
                    YiDianModel yiDianModel = new YiDianModel(title, docid, date, url, source, images);
                    yiDianModelList.add(yiDianModel);
                }
            }
            msgYiDian.arg1 = YiDianQuestCallBack.SUCCESS;
            msgYiDian.obj = yiDianModelList;
        } catch (JSONException e) {
            msgYiDian.arg1 = YiDianQuestCallBack.ERROR_PARSE;
        } finally {
            mHandler.sendMessage(msgYiDian);
        }
    }

    public interface YiDianQuestCallBack {
        public static final int ERROR_CONNECT = -1;
        public static final int ERROR_RESPONSE = -2;
        public static final int ERROR_DATA_NULL = -3;
        public static final int ERROR_PARSE = -4;
        public static final int SUCCESS = 0;

        public void onError(int errorCode);//失败

        public void onSuccess(List<YiDianModel> yiDianModelList);//成功
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

}
