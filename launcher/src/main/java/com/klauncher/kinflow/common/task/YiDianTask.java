package com.klauncher.kinflow.common.task;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.utils.CommonShareData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xixionghui on 2016/3/31.
 */
public class YiDianTask {

    private final OkHttpClient client = new OkHttpClient();

    private YiDianQuestCallBack mCallBack;
    private Context mContext;
    private int mOurDefineChannelId;
    private Handler mHandler;
    private Message mMessage;
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
                    int offset = CommonShareData.getInt(CardContentManagerFactory.OFFSET_NAME + mOurDefineChannelId, 0)+5;
                    CommonShareData.putInt(CardContentManagerFactory.OFFSET_NAME+mOurDefineChannelId,offset);
                    mCallBack.onSuccess((List<YiDianModel>) msg.obj);
                    break;
            }
            return true;
        }
    };

    /**
     *
     * @param context
     * @param ourDefineChannelId 不同的channelID,用于控制offset
     * @param callBack
     */
    public YiDianTask(Context context,int ourDefineChannelId,YiDianQuestCallBack callBack) {
        this.mContext = context;
        this.mOurDefineChannelId = ourDefineChannelId;
        this.mCallBack = callBack;
        mMessage = Message.obtain();
        mHandler = new Handler(handleCallback);
    }

    public void run(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //网络连接错误
                mMessage.arg1 = YiDianQuestCallBack.ERROR_CONNECT;
                mHandler.sendMessage(mMessage);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {//响应失败
                    mMessage.arg1 = YiDianQuestCallBack.ERROR_RESPONSE;
                }else {//响应成功
                    parseYiDian(response.body().string());
                }
                mHandler.sendMessage(mMessage);
                response.body().close();
            }
        });
    }

    private void parseYiDian(String responseBody) {
//        handler.sendMessage(msg);
        List<YiDianModel> yiDianModelList = new ArrayList<>();
        try {
            JSONObject allYiDianObject = new JSONObject(responseBody);
            int code = allYiDianObject.getInt("code");
            if (code != 0) {//获取失败
                mMessage.arg1 = YiDianQuestCallBack.ERROR_DATA_NULL;
            } else {//获取成功
                JSONArray jsonArrayYiDianModel = allYiDianObject.getJSONArray("result");
                int length = jsonArrayYiDianModel.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonYidianMoedel = jsonArrayYiDianModel.getJSONObject(i);
                    String title = jsonYidianMoedel.getString("title");
                    String docid = jsonYidianMoedel.getString("docid");
                    String url = jsonYidianMoedel.getString("url");
                    String date = jsonYidianMoedel.getString("date");
                    String source= jsonYidianMoedel.getString("source");
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
                    }else {//不包含图片
                        images = new String[0];
                    }
                    YiDianModel yiDianModel = new YiDianModel(title,docid,date,url,source,images);
                    yiDianModelList.add(yiDianModel);
                }
            }
            mMessage.arg1 = YiDianQuestCallBack.SUCCESS;
            mMessage.obj = yiDianModelList;
        } catch (JSONException e) {
            mMessage.arg1 = YiDianQuestCallBack.ERROR_PARSE;
        }
    }

    public interface YiDianQuestCallBack {
        public static final  int ERROR_CONNECT = -1;
        public static final  int ERROR_RESPONSE = -2;
        public static final  int ERROR_DATA_NULL = -3;
        public static final  int ERROR_PARSE = -4;
        public static final  int SUCCESS = 0;

        public void onError(int errorCode);//失败
        public void onSuccess(List<YiDianModel> yiDianModelList);//成功
    }
}
