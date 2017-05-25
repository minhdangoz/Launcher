package com.kapp.knews.repository.net;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.repository.bean.DongFangTouTiao;
import com.kapp.knews.repository.bean.DongFangTouTiaoBean;
import com.kapp.knews.repository.server.DongFangRequestManager;
import com.kapp.knews.repository.utils.Const;
import com.kapp.knews.repository.utils.MD5;
import com.kapp.knews.repository.utils.StringUtils;
import com.kapp.knews.repository.utils.TelephonyUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by xixionghui on 2016/11/28.
 */

public class DongFangAsynchronousTask {

    private final OkHttpClient mClient = new OkHttpClient();

    private Handler mHandler;
    private Context mContext;


    public DongFangAsynchronousTask(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }


    /**
     * get Key

     public void accessKey() {

     final Message msgKey = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_KEY);

     try {
     //创建request
     Request request = new Request.Builder().url(Const.DONGFANG_RANDOM_CHARS).build();
     //通过request创建回调Call
     Call call = mClient.newCall(request);
     //请求加入调度
     call.enqueue(new Callback() {
    @Override public void onFailure(Call call, IOException e) {
    msgKey.arg1 = NetResponse.CONNECTION_ERROR;
    mHandler.sendMessage(msgKey);
    LogUtils.e("请求东方头条的key时，服务端没有响应");
    }

    @Override public void onResponse(Call call, Response response) throws IOException {

    LogUtils.e("请求东方头条时，服务端有响应，当前所在线程 = " + Thread.currentThread().getName());
    ResponseBody responseBody = response.body();
    if (!response.isSuccessful()) {//服务器响应失败,需要发送消息通知MainControl
    msgKey.arg1 = NetResponse.CONNECTION_ERROR;
    mHandler.sendMessage(msgKey);
    LogUtils.e("请求东方头条的key时，服务端有响应，但是响应失败");
    } else {
    try {
    JSONObject jsonObject = new JSONObject(responseBody.string());
    String code = jsonObject.optString("code");
    String key = analysisKey(code);
    //                            String key = analysisKey("3M7iJQuwAv3Oj");
    if (TextUtils.isEmpty(key))
    throw new NullPointerException("获取到的东方头条的可以为空");
    msgKey.arg1 = NetResponse.SUCCESS;
    msgKey.obj = key;
    } catch (Exception e) {
    msgKey.arg1 = NetResponse.EXCEPTION_JSON;
    LogUtils.e("在解析东方头条的key时，出错：" + e.getMessage());
    } finally {
    if (null != response) response.body().close();
    mHandler.sendMessage(msgKey);
    }
    }

    }
    });
     } catch (Exception e) {
     msgKey.arg1 = NetResponse.UNKNOW_EXECTION;
     try {
     if (!mHandler.hasMessages(msgKey.what)) {
     mHandler.sendMessage(msgKey);
     }
     } catch (Exception e1) {
     Message msg2 = Message.obtain();
     msg2.what = msgKey.what;
     mHandler.sendMessage(msg2);
     LogUtils.e("请求东方头条时出错" + e.getMessage());
     }
     }

     }
     */


    /**
     * @param loadType 下拉刷新还是上拉加载
     * @param tabInfo
     */
    public void accessKey(int loadType, TabInfo tabInfo) {
        final Message msgKey = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_KEY);
        Bundle bundle = new Bundle();
        bundle.putParcelable(DongFangRequestManager.TAB_INFO, tabInfo);
        bundle.putInt(DongFangRequestManager.LOAD_TYPE, loadType);
        msgKey.setData(bundle);

        try {
            //创建request
            Request request = new Request.Builder().url(Const.DONGFANG_RANDOM_CHARS).build();
            //通过request创建回调Call
            Call call = mClient.newCall(request);
            //请求加入调度
            call.enqueue(new Callback() {
                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.e("请求东方头条的key时，服务端没有响应:"+e.getMessage());
                                 msgKey.arg1 = NetResponse.CONNECTION_ERROR;
                                 mHandler.sendMessage(msgKey);
                             }

                             @Override
                             public void onResponse(Call call, Response response) throws IOException {

                                 LogUtils.e("请求东方头条时，服务端有响应，当前所在线程 = " + Thread.currentThread().getName());
                                 ResponseBody responseBody = response.body();
                                 if (!response.isSuccessful()) {//服务器响应失败,需要发送消息通知MainControl
                                     msgKey.arg1 = NetResponse.CONNECTION_ERROR;
                                     mHandler.sendMessage(msgKey);
                                     LogUtils.e("请求东方头条的key时，服务端有响应，但是响应失败");
                                 } else {
                                     try {
                                         JSONObject jsonObject = new JSONObject(responseBody.string());
                                         String code = jsonObject.optString("code");
                                         String key = analysisKey(code);
                                         //                            String key = analysisKey("3M7iJQuwAv3Oj");
                                         if (TextUtils.isEmpty(key))
                                             throw new NullPointerException("获取到的东方头条的可以为空");
                                         msgKey.arg1 = NetResponse.SUCCESS;
                                         msgKey.obj = key;
                                     } catch (Exception e) {
                                         msgKey.arg1 = NetResponse.EXCEPTION_JSON;
                                         LogUtils.e("在解析东方头条的key时，出错：" + e.getMessage());
                                     } finally {
                                         if (null != response) response.body().close();
                                         mHandler.sendMessage(msgKey);
                                     }
                                 }

                             }
                         }
            );
        } catch (Exception e) {
            msgKey.arg1 = NetResponse.UNKNOW_EXECTION;
            try {
                if (!mHandler.hasMessages(msgKey.what)) {
                    mHandler.sendMessage(msgKey);
                }
            } catch (Exception e1) {
                Message msg2 = Message.obtain();
                msg2.what = msgKey.what;
                mHandler.sendMessage(msg2);
                LogUtils.e("请求东方头条时出错" + e.getMessage());
            }
        }


    }


    public void requestDongFangTouTiaoArticle(String key, boolean isFirst) {
        try {

            List<DongFangTouTiao> dongFangTouTiaoList = new ArrayList<>();
            Response response = null;
            final Message msgArticle = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE);
            try {
                //创建请求体
//                RequestBody formBody = new FormBody.Builder()
//                        .add("type", tabInfo.getTabChannelName())
//                        .build();
                String imei = TelephonyUtils.getIMEI(mContext);
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                formBodyBuilder.add("type", "toutiao")
                        .add("pgnum", "1")
                        .add("ime", imei)
                        .add("key", key)
                        .add("idx", "0")
                        .add("apptypeid", "kdaohang")
//                        .add("appver", BuildConfig.VERSION_NAME)
                        .add("appver", "2.1")
                        .add("qid", "kdaohang")
                        .add("position", "北京");

                if (!isFirst) {
                    LogUtils.e("不是第一次请求东方头条的，添加参数startkey和newkey");
                }

                RequestBody formBody = formBodyBuilder.build();


                //创建请求对象
                Request request = new Request.Builder()
                        .url(Const.DONGFANG_ARTICLE_URL)
                        .post(formBody)
                        .build();

                //通过request创建回调call
                Call call = mClient.newCall(request);
                //回调获取响应体
                response = call.execute();

                if (!response.isSuccessful()) {
                    LogUtils.e("请求东方头条,服务端响应失败");
                    msgArticle.arg1 = NetResponse.RESPONSE_FAIL;
                } else {
                    JSONObject jsonDongFangTouTiao = new JSONObject(response.body().string());

                    int state = jsonDongFangTouTiao.optInt("stat", 0);
                    if (state != 1) {//返回数据失败

                        msgArticle.arg1 = NetResponse.RESPONSE_STATE_ERROR;


                    } else {//返回数据成功

                        String endkey = jsonDongFangTouTiao.optString("endkey", "");
                        String newkey = jsonDongFangTouTiao.optString("newkey", "");
                        JSONArray jsonArrayDongFangBean = jsonDongFangTouTiao.optJSONArray("data");
                        for (int i = 0; i < jsonArrayDongFangBean.length(); i++) {
                            dongFangTouTiaoList.add(new DongFangTouTiao(new DongFangTouTiaoBean(jsonArrayDongFangBean.optJSONObject(i))));
                        }

                        //添加新闻列表
                        Bundle bundle = new Bundle();
                        bundle.putString(DongFangRequestManager.PARAMETER_START_KEY, endkey);
                        bundle.putString(DongFangRequestManager.PARAMETER_NEW_KEY, newkey);
                        msgArticle.setData(bundle);
                        msgArticle.obj = dongFangTouTiaoList;
                        msgArticle.arg1 = NetResponse.SUCCESS;

                    }
                }

            } catch (Exception e) {
                LogUtils.e("请求东方头条新闻时，出错错误：" + e.getMessage());
            } finally {
                if (null != response)
                    response.body().close();
                mHandler.sendMessage(msgArticle);

            }

        } catch (Exception e) {
            final Message exceptionMsg = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE);
            mHandler.sendMessage(exceptionMsg);
        }
    }


    public void requestDongFangTouTiaoArticle(int loadType, TabInfo tabInfo, RequestBody formBody,long pgnum) {
        try {

            ArrayList<DongFangTouTiao> dongFangTouTiaoList = new ArrayList<>();
            Response response = null;
            final Message msgArticle = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE);
            Bundle bundle = new Bundle();
            bundle.putInt(DongFangRequestManager.LOAD_TYPE, loadType);
            bundle.putParcelable(DongFangRequestManager.TAB_INFO, tabInfo);
            bundle.putLong(DongFangRequestManager.PARAMETER_PGNUM,pgnum);
            try {
                //创建请求对象
                Request request = new Request.Builder()
                        .url(Const.DONGFANG_ARTICLE_URL)
                        .post(formBody)
                        .build();

                //通过request创建回调call
                Call call = mClient.newCall(request);
                //回调获取响应体
                response = call.execute();

                if (!response.isSuccessful()) {
                    LogUtils.e("请求东方头条,服务端响应失败");
                    msgArticle.arg1 = NetResponse.RESPONSE_FAIL;
                } else {
                    JSONObject jsonDongFangTouTiao = new JSONObject(response.body().string());

                    int state = jsonDongFangTouTiao.optInt("stat", 0);
                    if (state != 1) {//返回数据失败

                        msgArticle.arg1 = NetResponse.RESPONSE_STATE_ERROR;


                    } else {//返回数据成功

                        String endkey = jsonDongFangTouTiao.optString("endkey", "");
                        String newkey = jsonDongFangTouTiao.optString("newkey", "");
                        JSONArray jsonArrayDongFangBean = jsonDongFangTouTiao.optJSONArray("data");
                        for (int i = 0; i < jsonArrayDongFangBean.length(); i++) {
                            dongFangTouTiaoList.add(new DongFangTouTiao(new DongFangTouTiaoBean(jsonArrayDongFangBean.optJSONObject(i))));
                        }

                        //添加新闻列表

                        bundle.putString(DongFangRequestManager.PARAMETER_START_KEY, endkey);
                        bundle.putString(DongFangRequestManager.PARAMETER_NEW_KEY, newkey);
                        msgArticle.obj = dongFangTouTiaoList;
                        msgArticle.arg1 = NetResponse.SUCCESS;


                    }
                }

            } catch (Exception e) {
                LogUtils.e("请求东方头条新闻时，出错错误：" + e.getMessage());
            } finally {
                if (null != response)
                    response.body().close();
                msgArticle.setData(bundle);
                mHandler.sendMessage(msgArticle);

            }

        } catch (Exception e) {
            final Message exceptionMsg = NetMessageFactory.createMessage(NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE);
            mHandler.sendMessage(exceptionMsg);
        }
    }


    /**
     * @param randomCharacter 2  cRGc4iFI  y  tjS
     */
    public String analysisKey(String randomCharacter) throws NullPointerException {

        LogUtils.e("获取到东方头条的key的code = " + randomCharacter);

        if (null == randomCharacter || randomCharacter.length() != 13)
            throw new NullPointerException();

//        char[] randomChars = randomCharacter.toCharArray();

        String secondPart = randomCharacter.substring(1, 9);//cRGc4iFI
        String thirdPart = randomCharacter.substring(10, randomCharacter.length());

        LogUtils.e("secondPart:" + secondPart);
        LogUtils.e("thridPart:" + thirdPart);

        String finalStr = getFinalString(secondPart, thirdPart, randomCharacter);
        LogUtils.e("finalStr = " + finalStr);

//        return StringUtils.md5Hex(finalStr);
        return MD5.encode(finalStr);
    }

    private String getFinalString(String secondPart, String thirdPart, String randomCharacter) {
        //1. string 转换成ascii码
        int[] charsAscii = StringUtils.string2Ascii(thirdPart);
        LogUtils.e("第三部分String转换成ascii码:" + charsAscii);
        //2.
        int firstInteger = charsAscii[0];//获取ascii对应的三个整数
        int secondInteger = charsAscii[1];
        int thirdInteger = charsAscii[2];
        LogUtils.e("第三部分String转换成ascii码后，分别是：firstInteger=" + firstInteger + "  secondInteger" + secondInteger + "  thirdInteger" + thirdInteger);

        //对第三部分的前两位分别取余
        int firstRem = firstInteger % 8;//对前两个整数分别取余
        int secondRem = secondInteger % 8;

        firstRem = firstRem < 3 ? 3 : firstRem;
        secondRem = secondRem < 3 ? 3 : secondRem;
        LogUtils.e("前两部分分别取余并将小于3的值直接赋值为3后的结果： firstRem = " + firstRem + " secondRem = " + secondRem);

        //获取前两位分别对应的字符串
        String firstRemContract = secondPart.substring(0, firstRem);
        String secondPartReverse = StringUtils.reverse(secondPart);
        String secondRemContract = secondPartReverse.substring(0, secondRem);
        LogUtils.e("前两位分别对应的字符串，firstRemContract=" + firstRemContract + " ， secondRemContract=" + secondRemContract);

        //将前两位对应字符串长度小的一方，遍历补充，赋值新的对应字符串
        int firstRemContractLength = firstRemContract.length();
        int secondRemContractLength = secondRemContract.length();

        StringBuilder stringBuilderFill = null;
        if (firstRemContractLength < secondRemContractLength) {//第一取余对应字符长度<第二取余对应字符长度
            stringBuilderFill = new StringBuilder(firstRemContract);

            for (int i = 0; i < firstRemContractLength; i++) {
                if (stringBuilderFill.length() == secondRemContractLength) break;
                stringBuilderFill.append(firstRemContract.charAt(i));
                if (i == firstRemContractLength - 1)
                    i = -1;
            }

            firstRemContract = stringBuilderFill.toString();

        } else {//第二取余对应字符长度<第一取余对应字符长度
            stringBuilderFill = new StringBuilder(secondRemContract);

            for (int j = 0; j < secondRemContractLength; j++) {
                if (stringBuilderFill.length() == firstRemContractLength) break;
                stringBuilderFill.append(secondRemContract.charAt(j));
                if (j == secondRemContractLength - 1)
                    j = -1;
            }


            secondRemContract = stringBuilderFill.toString();
        }

        LogUtils.e("将前两位对应字符串长度小的一方，遍历补充，赋值新的对应字符串: firstRemContract = " + firstRemContract + " ， secondRemContract = " + secondRemContract);


        // 对第三个整数除以4取余
        int thirdRem = thirdInteger % 4;
        StringBuilder finalStr = new StringBuilder("kdaohang");
        String arithmeticResult = null;
        switch (thirdRem) {
            case 0://与操作
                arithmeticResult = bitAnd(firstRemContract, secondRemContract);
                LogUtils.e("位与操作算法结果：bitAnd = " + arithmeticResult);
                break;
            case 1://加操作
                arithmeticResult = bitAdd(firstRemContract, secondRemContract);
                LogUtils.e("位加操作算法结果：bitAdd = " + arithmeticResult);
                break;
            case 2://异或操作
                arithmeticResult = bitXor(firstRemContract, secondRemContract);
                LogUtils.e("异或操作算法结果：bitXor = " + arithmeticResult);
                break;
            case 3://减操作
                arithmeticResult = bitSubtraction(firstRemContract, secondRemContract);
                LogUtils.e("减操作算法结果：bitSubtraction = " + arithmeticResult);
                break;
            default:
                LogUtils.e("对第三个整数除以4取余,得到非0，1，2，3的数，出现错误，无法进行算法操作");
                break;
        }

        finalStr.append(arithmeticResult).append(randomCharacter);
        return finalStr.toString();

    }

    /*
    * 位与操作
    * */
    private String bitAnd(String firstRemContract, String secondRemContract) {
        LogUtils.e("开始执行位与运算，firstRemContract = " + firstRemContract + "  ，secondRemContract = " + secondRemContract);

        int length = firstRemContract.length();
        StringBuilder bitAndStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //获取字符串中对应字符
            char firstRemContractChar = firstRemContract.charAt(i);
            char secondRemContractChar = secondRemContract.charAt(i);
            //做位与运算
            String hexStr = Integer.toHexString((firstRemContractChar & secondRemContractChar));
            //将与操作之后的数据拼接到bitAndStringBuilder
            if (hexStr.length() < 2) {
                bitAndStringBuilder.append(0).append(hexStr);
            } else {
                bitAndStringBuilder.append(hexStr);
            }
        }
        LogUtils.e("将两个字符串对应的字符做位与操作后，拼接的字符串为： " + bitAndStringBuilder.toString());
        //如果stringBuilder的长度小于16，则在尾部拼接0
        if (bitAndStringBuilder.length() < 16) {
            for (int x = 0; x < 16; x++) {
                if (bitAndStringBuilder.length() == 16) break;
                bitAndStringBuilder.append(0);
            }
        }
        LogUtils.e("如果两个字符串做位与操作后长度小于16，则在后面拼接0，结果为：" + bitAndStringBuilder.toString());
        BigInteger bigint = new BigInteger(bitAndStringBuilder.toString(), 16);
        return String.valueOf(bigint);
    }

    /**
     * 位加操作
     *
     * @param firstRemContract
     * @param secondRemContract
     * @return
     */
    private String bitAdd(String firstRemContract, String secondRemContract) {
        LogUtils.e("开始执行位加运算，firstRemContract = " + firstRemContract + "  ，secondRemContract = " + secondRemContract);

        int length = firstRemContract.length();
        StringBuilder bitAddStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //获取字符串中对应字符
            char firstRemContractChar = firstRemContract.charAt(i);
            char secondRemContractChar = secondRemContract.charAt(i);

            //做位加运算
            String hexStr = Integer.toHexString((firstRemContractChar + secondRemContractChar));
            //将与操作之后的数据拼接到bitAddStringBuilder
            if (hexStr.length() < 2) {
                bitAddStringBuilder.append(0).append(hexStr);
            } else {
                bitAddStringBuilder.append(hexStr);
            }
        }
        LogUtils.e("将两个字符串对应的字符做位加操作后，拼接的字符串为： " + bitAddStringBuilder.toString());

        //如果stringBuilder的长度小于16，则在尾部拼接0
        if (bitAddStringBuilder.length() < 16) {
            for (int x = 0; x < 16; x++) {
                if (bitAddStringBuilder.length() == 16) break;
                bitAddStringBuilder.append(0);
            }
        }

        LogUtils.e("如果两个字符串做位加操作后长度小于16，则在后面拼接0，结果为：" + bitAddStringBuilder.toString());

        BigInteger bigint = new BigInteger(bitAddStringBuilder.toString(), 16);
        return String.valueOf(bigint);
    }

    /**
     * 异或运算
     *
     * @param firstRemContract
     * @param secondRemContract
     * @return
     */
    private String bitXor(String firstRemContract, String secondRemContract) {
        LogUtils.e("开始执行异或运算，firstRemContract = " + firstRemContract + "  ，secondRemContract = " + secondRemContract);

        int length = firstRemContract.length();
        StringBuilder bitXorStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //获取字符串中对应字符
            char firstRemContractChar = firstRemContract.charAt(i);
            char secondRemContractChar = secondRemContract.charAt(i);

            //做异或运算
            String hexStr = Integer.toHexString((firstRemContractChar ^ secondRemContractChar));
            //将与操作之后的数据拼接到bitAddStringBuilder
            if (hexStr.length() < 2) {
                bitXorStringBuilder.append(0).append(hexStr);
            } else {
                bitXorStringBuilder.append(hexStr);
            }
        }
        LogUtils.e("将两个字符串对应的字符做异或操作后，拼接的字符串为： " + bitXorStringBuilder.toString());

        //如果stringBuilder的长度小于16，则在尾部拼接0
        if (bitXorStringBuilder.length() < 16) {
            for (int x = 0; x < 16; x++) {
                if (bitXorStringBuilder.length() == 16) break;
                bitXorStringBuilder.append(0);
            }
        }

        LogUtils.e("如果两个字符串做异或操作后长度小于16，则在后面拼接0，结果为：" + bitXorStringBuilder.toString());

        BigInteger bigint = new BigInteger(bitXorStringBuilder.toString(), 16);
        return String.valueOf(bigint);
    }

    /**
     * 位减操作
     *
     * @param firstRemContract
     * @param secondRemContract
     * @return
     */
    private String bitSubtraction(String firstRemContract, String secondRemContract) {
        LogUtils.e("开始执行减运算，firstRemContract = " + firstRemContract + "  ，secondRemContract = " + secondRemContract);

        int length = firstRemContract.length();
        StringBuilder bitSubtractionStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //获取字符串中对应字符
            char firstRemContractChar = firstRemContract.charAt(i);
            char secondRemContractChar = secondRemContract.charAt(i);

            //做位减运算
            int charSubtraction = firstRemContractChar - secondRemContractChar;
            charSubtraction = charSubtraction < 0 ? Math.abs(charSubtraction) : charSubtraction;
            String hexStr = Integer.toHexString(charSubtraction);
            //bitSubtractionStringBuilder
            if (hexStr.length() < 2) {
                bitSubtractionStringBuilder.append(0).append(hexStr);
            } else {
                bitSubtractionStringBuilder.append(hexStr);
            }
        }
        LogUtils.e("将两个字符串对应的字符做减操作后，拼接的字符串为： " + bitSubtractionStringBuilder.toString());

        //如果stringBuilder的长度小于16，则在尾部拼接0
        if (bitSubtractionStringBuilder.length() < 16) {
            for (int x = 0; x < 16; x++) {
                if (bitSubtractionStringBuilder.length() == 16) break;
                bitSubtractionStringBuilder.append(0);
            }
        }

        LogUtils.e("如果两个字符串做减操作后长度小于16，则在后面拼接0，结果为：" + bitSubtractionStringBuilder.toString());

        BigInteger bigint = new BigInteger(bitSubtractionStringBuilder.toString(), 16);
        return String.valueOf(bigint);
    }


}
