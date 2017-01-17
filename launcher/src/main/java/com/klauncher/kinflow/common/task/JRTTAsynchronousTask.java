package com.klauncher.kinflow.common.task;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.klauncher.kinflow.cards.manager.JRTTCardContentManager;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoToken;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.utilities.TelephonyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by xixionghui on 16/6/14.
 */
public class JRTTAsynchronousTask {
    public static final String TAG = "Kinflow";
    //通用错误代码
    public static final int CONNECTION_ERROR = -1;//没有连接到服务器：网络错误||服务器维护
//    public static final int SUCCESS = 0;//获取数据成功
//    public static final int PARSE_ERROR = 1;//解析数据失败
//    public static final int OBTAIN_RESULT_NULL = 2;//获取到服务器端的响应数据，但是数据为空
//    public static final int RESPONSE_FAIL = 3;//服务器端有响应，但是出现错误：如404,500-----服务器响应失败，请稍后重试

    public static final int EXCEPTION_IO = -2;
    public static final int EXCEPTION_JSON = -3;
    public static final int DATA_NULL = -4;

    //今日头条错误代码
    public static final int SUCCESS = 0;//必须与Asynchronous中的success保持一致,否则mainControl可能无法识别.
    public static final int AUTHENTICATION_FAIL = 1;
    public static final int WITHOUT_UUID = 2;
    public static final int ERROR_CATEGORY = 13;
    public static final int UNSUPPORTED_SYSTEM = 21;
    public static final int UNKNOW_ERROR = 999;


    private final OkHttpClient mClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
    //    private final OkHttpClient mClient = new OkHttpClient();
    public static final MediaType MEDIA_TYPE_FORM
            = MediaType.parse("application/x-www-form-urlencoded");
    public static final MediaType OKHTTP_MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded");
    private Handler mHandler;
    private Message mMessage;
    private Context mContext;

    public JRTTAsynchronousTask(Context context, Handler handler, int messageWhat) {
        this.mContext = context;
        this.mHandler = handler;
        this.mMessage = MessageFactory.createMessage(messageWhat);
    }
    public void accessToken() {
        mMessage.arg1 = UNKNOW_ERROR;
        Response response = null;
        try {
            String timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
            String nonce = CommonUtils.getInstance().getRandomString(5);//安全参数:生成5个随机字符串
            RequestBody formBody = new FormBody.Builder()
                    .add("timestamp", timestamp)//安全参数:时间戳
                    .add("nonce", nonce)//安全参数:生成5个随机字符串
                    .add("partner", Const.TOUTIAO_PARTNER)//公共参数:合作伙伴的id
                    .add("signature", CommonUtils.SHA1(CommonUtils.orderLexicographical(new String[]{Const.TOUTIAO_SECURE_KEY, timestamp, nonce})))//安全参数:加密签名

                    .add("uuid", TelephonyUtils.getIMEI(mContext))
                    .add("openudid", TelephonyUtils.getDeviceId(mContext))//ANDROID_ID
                    .add("os", "Android")
                    .add("os_version", TelephonyUtils.getOsVersion())
                    .add("device_mode", TelephonyUtils.getDM())//设备型号
                    .build();

            Request request = new Request.Builder()
                    .url(Const.TOUTIAO_URL_ACCESS_TOKEN)
                    .post(formBody)
                    .build();
            Log.e(TAG, "accessToken的请求体编写完毕,开始执行请求=============================url = "+request.url());
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {//服务器有响应
                Log.e(TAG, "accessToken: 服务器端有响应");
                String responseBody = response.body().string();
                Log.e("Kinflow", "获取到的accessToken响应体: " + responseBody);
                JSONObject jsonObject = new JSONObject(responseBody);
                int ret = jsonObject.optInt("ret");
                if (ret == AUTHENTICATION_FAIL) {//验证失败
                    mMessage.arg1 = AUTHENTICATION_FAIL;
                } else if (ret == WITHOUT_UUID) {//没有UUID
                    mMessage.arg1 = WITHOUT_UUID;
                } else if (ret == ERROR_CATEGORY) {//错误的分类
                    mMessage.arg1 = ERROR_CATEGORY;
                } else if (ret == UNSUPPORTED_SYSTEM) {//不支持的系统
                    mMessage.arg1 = UNSUPPORTED_SYSTEM;
                } else if (ret == UNKNOW_ERROR) {//未知错误
                    mMessage.arg1 = UNKNOW_ERROR;
                } else if (ret == SUCCESS) {//成功
                    JinRiTouTiaoToken touTiaoToken = new JinRiTouTiaoToken(jsonObject.optJSONObject("data"));
                    if (null != touTiaoToken) {//响应成功&&获取到数据
                        String accessToken = touTiaoToken.getAccess_token();
                        if (null != accessToken) {//响应成功&&获取到数据&&token
                            CommonShareData.putString(CommonShareData.KEY_JINRITOUTIAO_ACCESS_TOKEN, accessToken);
                            mMessage.arg1 = SUCCESS;
                            mMessage.obj = touTiaoToken;
                            Log.e(TAG, "获取access_token成功 : " + touTiaoToken.toString());
                        } else {
                            mMessage.arg1 = DATA_NULL;
                        }
                    } else {
                            mMessage.arg1 = DATA_NULL;
                    }
                }
            } else { //服务器没有响应
                Log.e(TAG, "accessToken: 服务器端没响应");
                mMessage.arg1 = CONNECTION_ERROR;
            }
            //关闭响应体
            if (null != response) response.body().close();
        } catch (IOException e) {
            mMessage.arg1 = EXCEPTION_IO;
            Log.e("Kinflow", "请求accessToken失败,发生IOException:" + e.getMessage() + "推断出错位置:mClient.newCall(request).execute()");
        } catch (JSONException e) {
            mMessage.arg1 = EXCEPTION_JSON;
            Log.e("Kinflow", "请求accessToken失败,发生JSONException:" + e.getMessage() + "推断出错位置:new JSONObject(response.body().string())");
        } catch (Exception e) {
            mMessage.arg1 = UNKNOW_ERROR;
            Log.e("Kinflow", "请求accessToken失败,发生Exception:" + e.getMessage());
        } finally {
            try {
                if (!mHandler.hasMessages(mMessage.what))
                    mHandler.sendMessage(mMessage);
            } catch (Exception e) {
                Log.e(TAG, "获取accessToken时,在最后发送Message时,出错:"+e.getMessage());
                Message msg2 = Message.obtain();
                msg2.what = mMessage.what;
                mHandler.sendMessage(msg2);
            }
        }

    }

    public JRTTAsynchronousTask (Context context) {
        this.mContext = context;
    }

    public void reportUserAppLog(final long group_Id) {
        new Thread(){
            @Override
            public void run() {
                Response response = null;
                try {
                    String timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
                    String nonce = CommonUtils.getInstance().getRandomString(5);//安全参数:生成5个随机字符串
                    String signature = CommonUtils.SHA1(CommonUtils.orderLexicographical(new String[]{Const.TOUTIAO_SECURE_KEY, timestamp, nonce}));
                    String events = getEventParams(DateUtils.getInstance().yyyy_MM_dd_HH_mm_ss(), group_Id);
                    String accessToken = CommonShareData.getString(CommonShareData.KEY_JINRITOUTIAO_ACCESS_TOKEN, "");
                    RequestBody formBody = new FormBody.Builder()
                            .add("timestamp", timestamp)//安全参数:时间戳
                            .add("nonce", nonce)//安全参数:生成5个随机字符串
                            .add("partner", Const.TOUTIAO_PARTNER)//公共参数:合作伙伴的id
                            .add("signature", signature)//安全参数:加密签名
                            .add("access_token", accessToken)//token

                            .add("events", events)
                            .build();

                    Request request = new Request.Builder()
                            .url(Const.TOUTIAO_URL_USER_EVENT)
                            .post(formBody)
                            .build();
                    response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBodyJsonString = response.body().string();
                        Log.e(TAG, "reportUserAppLog: responseBodyJsonString = "+responseBodyJsonString);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "run: 上报用户行为,出现未知错误:"+e.getMessage());
                }
            }
        }.start();
    }

    public String getEventParams(String dataTime,long group_Id) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("[{")
                .append("\"category\"").append(":").append("\"open\"").append(",")
                .append("\"tag\"").append(":").append("\"go_detail\"").append(",")
                .append("\"datetime\"").append(":").append("\"").append(dataTime).append("\"").append(",")
                .append("\"value\"").append(":").append(group_Id).append(",")
                .append("\"label\"").append(":").append("\"click_news_hot\"")
                .append("}]");
        return stringBuilder.toString();
    }


    /**
     * @param category 的来源应该由cardSecondTypeId决定
     *                 try...catch要放到最外围
     */
    public void getJinRiToutiaoArticle(String category) {
        try {
            mMessage.arg1 = UNKNOW_ERROR;
            String requestUrl = getJinRiTouTiaoArticleListUrl(category);
            Log.e(TAG, "getJinRiToutiaoArticle: requestUrl= " + requestUrl);
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();
            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mMessage.arg1 = CONNECTION_ERROR;
                    mHandler.sendMessage(mMessage);
                    Log.e(TAG, "onFailure,服务器没有响应:" + e.getMessage() + " msg.what" + mMessage.what);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    Log.e(TAG, "JRTTAsynchronousTask.onResponse,服务器有响应,当前所在线程 = " + Thread.currentThread().getName());
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {//服务器响应失败,需要发送消息通知MainControl
                        mMessage.arg1 = CONNECTION_ERROR;
                        mHandler.sendMessage(mMessage);
                        Log.e(TAG, "onResponse,服务器有响应,但是响应失败, msg.what" + mMessage.what);
                    } else {//服务器响应成,需要解析数据并最终发送消息到MainControl
                        //以下try...catch 只针对服务器有响应&&响应成功情况,进一步对响应数据解析时可能出现的错误.
                        try {
                            List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList = new ArrayList<JinRiTouTiaoArticle>();
                            JSONObject jsonObjectRoot = new JSONObject(responseBody.string());
                            int ret = jsonObjectRoot.optInt("ret", 999);
                            if (ret == 0) {//获取数据成功
                                JSONArray jinRiTouTiaoArticleJsonArray = jsonObjectRoot.optJSONArray("data");
                                if (null == jinRiTouTiaoArticleJsonArray || jinRiTouTiaoArticleJsonArray.length() == 0) {
                                    Log.e(TAG, "获取到的今日头条数据为空");
                                } else {
                                    int jinRiTouTiaoArticleJsonArrayLength = jinRiTouTiaoArticleJsonArray.length();
                                    for (int i = 0; i < jinRiTouTiaoArticleJsonArrayLength; i++) {
                                        jinRiTouTiaoArticleList.add(new JinRiTouTiaoArticle(jinRiTouTiaoArticleJsonArray.optJSONObject(i)));
                                    }
                                }
                            } else if (ret == 1) {//验证失败
                                mMessage.arg1 = AUTHENTICATION_FAIL;
                            } else if (ret == 2) {//没有UUID
                                mMessage.arg1 = WITHOUT_UUID;
                            } else if (ret == 13) {//错误的分类
                                mMessage.arg1 = ERROR_CATEGORY;
                            } else if (ret == 21) {//不支持的系统
                                mMessage.arg1 = UNSUPPORTED_SYSTEM;
                            } else if (ret == 999) {//未知错误
                                mMessage.arg1 = UNKNOW_ERROR;
                            }

                            if (jinRiTouTiaoArticleList.size() == 0) {
                                Log.e(TAG, "获取到的今日头条个数=0");
                                mMessage.arg1 = DATA_NULL;
                            } else {
                                mMessage.arg1 = SUCCESS;
                                mMessage.obj = jinRiTouTiaoArticleList;
                                behotTimeCompare(jinRiTouTiaoArticleList);
                            }
                        } catch (IOException ioException) {
                            Log.e(TAG, "响应体创建JSONObject时,出错,位于:new JSONObject(responseBody.string())" + ioException.getMessage());
                        } catch (JSONException jsonException) {
                            mMessage.arg1 = EXCEPTION_JSON;
                            Log.e(TAG, "解析今日头条数据时,出错:" + jsonException.getMessage());
                        } catch (Exception e) {
                            mMessage.arg1 = UNKNOW_ERROR;
                            Log.e(TAG, "解析今日头条JSON时,出错:" + e.getMessage());
                        } finally {
                            if (null != response) response.body().close();
                            mHandler.sendMessage(mMessage);
                        }
                    }
                }
            });
        } catch (Exception e) {
            try {
                if (!mHandler.hasMessages(mMessage.what))
                    mHandler.sendMessage(mMessage);
            } catch (Exception e1) {
                Message msg2 = Message.obtain();
                msg2.what = mMessage.what;
                mHandler.sendMessage(msg2);
                Log.e("Kinflow","获取今日头条Artilce时,发生未知异常,捕获后发送消息Message时发生错误:"+e1.getMessage());
            }
        }
    }

    public void behotTimeCompare (List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList) {

        try {
            JinRiTouTiaoArticle minJinRiTouTiaoArticle = jinRiTouTiaoArticleList.get(0);
            JinRiTouTiaoArticle maxJinRiTouTiaoArticle = jinRiTouTiaoArticleList.get(0);
            int size = jinRiTouTiaoArticleList.size();
            for (int i = 1; i < size; i++) {
                JinRiTouTiaoArticle currentJinRiTouTiaoArticle = jinRiTouTiaoArticleList.get(i);
                if (currentJinRiTouTiaoArticle.getBehot_time()<minJinRiTouTiaoArticle.getBehot_time()) minJinRiTouTiaoArticle = currentJinRiTouTiaoArticle;
                if (currentJinRiTouTiaoArticle.getBehot_time()>maxJinRiTouTiaoArticle.getBehot_time()) maxJinRiTouTiaoArticle = currentJinRiTouTiaoArticle;
            }

            long minBehotTime = maxJinRiTouTiaoArticle.getBehot_time();//返回上一刷历史的文章中最 大的那个时间戳
            long maxBehotTime = minJinRiTouTiaoArticle.getBehot_time();//上一刷最小的时间戳
            CommonShareData.putLong(CommonShareData.KEY_ARTICLE_MIN_BEHOT_TIME,minBehotTime);
            CommonShareData.putLong(CommonShareData.KEY_ARTICLE_MAX_BEHOT_TIME,maxBehotTime);
            Log.e(TAG, "behotTimeCompare: 存储behotTime, minBehotTime="+minBehotTime+"  ,maxBehotTime="+maxBehotTime);
        } catch (Exception e) {
            Log.e(TAG, "behotTimeCompare: 存储minbeHotTime和maxbeHotTime时出错,详情:"+e.getMessage());
        }

    }

    /**
     * 此方法是管理刷新,获取更多,请求条数等操作的入口
     *
     * @return
     */
    public String getJinRiTouTiaoArticleListUrl(String category) {
        String timestamp = String.valueOf((int) ((System.currentTimeMillis()) / 1000));
        String nonce = CommonUtils.getInstance().getRandomString(5);//生成5个随机字符串
        String signature = CommonUtils.SHA1(CommonUtils.orderLexicographical(new String[]{Const.TOUTIAO_SECURE_KEY, timestamp, nonce}));
        StringBuilder sbUrl = getCommonParameter(signature, timestamp, nonce);
        sbUrl.append("&").append("category").append("=").append(category);

//        long minBehotTime = CommonShareData.getLong(CommonShareData.KEY_ARTICLE_MIN_BEHOT_TIME, -1);
//        sbUrl.append("&").append("min_behot_time").append("=").append(String.valueOf(minBehotTime));//refresh 时使用:如果客户端没有历史,传入当前时间戳-10
        long maxBehotTime = CommonShareData.getLong(CommonShareData.KEY_ARTICLE_MAX_BEHOT_TIME, Calendar.getInstance().getTimeInMillis() / 1000);
        sbUrl.append("&").append("max_behot_time").append("=").append(String.valueOf(maxBehotTime));//loadmore时使用:如果没有历史,则传递当前时间戳

//        Log.e(TAG, "getJinRiTouTiaoArticleListUrl:请求今日头条使用的behot:    max_behot_time=" + maxBehotTime + "  minBhehot="+minBehotTime);
        sbUrl.append("&").append("count").append("=").append(JRTTCardContentManager.REQUEST_ARTICLE_COUNT);//请求25条数据
        return sbUrl.toString();
    }

    /**
     * 获取公共参数:
     * 签名,时间戳,随机数,合作方id,token
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     */
    private StringBuilder getCommonParameter(String signature, String timestamp, String nonce) {
        StringBuilder sbUrl = new StringBuilder(Const.URL_JINRITOUTIAO_GET_CUSTOM_ARTICLE);
        sbUrl.append("?").append("signature").append("=").append(signature);
        sbUrl.append("&").append("timestamp").append("=").append(timestamp);
        sbUrl.append("&").append("nonce").append("=").append(nonce);
        sbUrl.append("&").append("partner").append("=").append(Const.TOUTIAO_COMMON_PARTNER);
        //这里应该进行一次token判断
        sbUrl.append("&").append("access_token").append("=").append(CommonShareData.getString(CommonShareData.KEY_JINRITOUTIAO_ACCESS_TOKEN, ""));
        return sbUrl;
    }
}
