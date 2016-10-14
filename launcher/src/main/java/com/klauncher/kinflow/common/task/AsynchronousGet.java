/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.klauncher.kinflow.common.task;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.kinflow.cards.model.server.ServerController;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class AsynchronousGet {

    public static final int CONNECTION_ERROR = -1;//没有连接到服务器：网络错误||服务器维护
    public static final int SUCCESS = 0;//获取数据成功
    public static final int PARSE_ERROR = 1;//解析数据失败
    public static final int OBTAIN_RESULT_NULL = 2;//获取到服务器端的响应数据，但是数据为空
    public static final int RESPONSE_FAIL = 3;//服务器端有响应，但是出现错误：如404,500-----服务器响应失败，请稍后重试

    //    private final OkHttpClient client = new OkHttpClient();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
    private final Gson gson = new GsonBuilder().create();
    private Handler handler;

    private Message msg;

    public AsynchronousGet(Handler handler, int messageWhat) {
        this.handler = handler;
        msg = MessageFactory.createMessage(messageWhat);
    }

    public void run(final String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    msg.arg1 = CONNECTION_ERROR;
                    handler.sendMessage(msg);
                    log("服务器连接失败,msg.what=" + msg.what);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    //1,如果响应失败
                    if (!response.isSuccessful()) {
                        log("服务器响应失败,msg.what=" + msg.what);
                        msg.arg1 = RESPONSE_FAIL;
                        handler.sendMessage(msg);
                        response.body().close();
                        return;
                    }
                    //2,响应体有错误
                    String responseBodyStr = null;
                    try {
                        responseBodyStr = response.body().string();
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.arg1 = RESPONSE_FAIL;
                        handler.sendMessage(msg);
                        log("服务器响应失败,发生IOException,msg.what=" + msg.what);
                        response.body().close();
                        return;
                    }
                    //3,准备解析
                    if (!TextUtils.isEmpty(responseBodyStr)) {
                        switch (msg.what) {
                            case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                                parseHotWord(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                                parseNavigation(responseBodyStr, Navigation.rootJsonKey_WEB_NAVIGATION);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY:
                                log("准备解析全局内容导航:\n" + responseBodyStr);
                                parseNavigation(responseBodyStr, Navigation.rootJsonKey_CONTENT_NAVIGATION);
                                break;
                            //此版本已没有天气模块,但是保留天气模块相关代码
//                    case MessageFactory.MESSAGE_WHAT_OBTAION_CITY_NAME:
//                        parseLocaiton(responseBodyStr);
//                        break;
                            case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN:
                                parseYiDian(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_TIMESTAMP:
                                parseTimestamp(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH:
                                parseConfigSwitch(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST:
                                parseConfigList(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG:
                                parseConfig(responseBodyStr);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAIN_KINFLOW2_SERVER_CONTROLLER:
                                parseServerController(responseBodyStr);
                                break;
                            default:
                                log("未知请求,URL=" + url);
                                msg.arg1 = CONNECTION_ERROR;
                                handler.sendMessage(msg);
                                break;
                        }
                        response.body().close();
                    } else {
                        log("Message.what=" + msg.what + "的响应体为空,url=" + url);
                        msg.arg1 = RESPONSE_FAIL;//RESPONSE_NULL
                        if (!handler.hasMessages(msg.what))
                            handler.sendMessage(msg);
                    }
                }
            });
        } catch (Exception e) {
            log("AsynchronousGet网络请求出错:" + e.getMessage());
        }
    }

    /**
     * 解析百度热词
     *
     * @param responseBody
     */
    void parseHotWord(String responseBody) {
        List<HotWord> hotWordList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONObject jsonHot = jsonObject.getJSONObject("hot");

            int jsonHotLength = jsonHot.length();

            if (jsonObject.isNull("hot") || jsonHot.length() == 0) {//没有获取到热词列表
                msg.arg1 = OBTAIN_RESULT_NULL;
            } else {
                for (int i = 1; i <= jsonHotLength; i++) {
                    JSONObject jsonHotWord = jsonHot.getJSONObject(String.valueOf(i));
                    hotWordList.add(new HotWord(String.valueOf(i), jsonHotWord.optString("word"), jsonHotWord.optString("url"), HotWord.TYPE_BAIDU));
                }
                msg.arg1 = SUCCESS;
                msg.obj = hotWordList;
            }
        } catch (Exception e) {
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }

    void parseNavigation(String responseBody,String jsonRootKey) {
        List<Navigation> navigationList = new ArrayList<>();
        ;
        try {
//            JSONArray jsonArray = new JSONArray(responseBody);
//            JSONObject jsonObjectAll = new JSONArray(responseBody).getJSONObject(0);
            JSONObject jsonObjectAll = new JSONObject(responseBody);
            if (null == jsonObjectAll) {
                msg.arg1 = OBTAIN_RESULT_NULL;
                return;
            }
//            JSONArray navigationJsonArray = jsonObjectAll.getJSONArray("cards");//kinfow1
            JSONArray navigationJsonArray = jsonObjectAll.getJSONArray(jsonRootKey);//kinfow2
            int navigationsLength = navigationJsonArray.length();
            if (null == navigationJsonArray || navigationsLength == 0) {
                msg.arg1 = OBTAIN_RESULT_NULL;
                return;
            }

            for (int i = 0; i < navigationsLength; i++) {
//                Navigation navigation = new Navigation();
                JSONObject navigationJsonObject = navigationJsonArray.getJSONObject(i);
                /*
                navigation.setNavId(navigationJsonObject.optString(Navigation.NAV_ID));
                navigation.setNavName(navigationJsonObject.optString(Navigation.NAV_NAME));
                navigation.setNavIcon(navigationJsonObject.optString(Navigation.NAV_ICON));
                navigation.setNavUrl(navigationJsonObject.optString(Navigation.NAV_URL));
                navigation.setNavOrder(navigationJsonObject.optInt(Navigation.NAV_ORDER));
                JSONArray opsJsonArray = navigationJsonObject.getJSONArray(CardInfo.CARD_OPEN_OPTIONS);
                List<String> opsList = new ArrayList<>();
                int opsJsonArrayLength = opsJsonArray.length();
                if (opsJsonArrayLength <= 0) {
//                    Log.d("AsynchronousGet", "opsArray为null启动自带浏览器");
                    opsList.add("23");
                    opsList.add("com.baidu.browser.apps/com.baidu.browser.framework.BdBrowserActivity");
                    opsList.add("0");
                } else {
                    for (int j = 0; j < opsJsonArrayLength; j++) {
                        opsList.add(opsJsonArray.optString(j));
                    }
                }
                navigation.setNavOpenOptions(opsList);
                */
                navigationList.add(new Navigation(navigationJsonObject));
            }
            msg.arg1 = SUCCESS;
            msg.obj = navigationList;
            if (jsonRootKey.equals(Navigation.rootJsonKey_WEB_NAVIGATION)) {
                CommonShareData.putString(Const.NAVIGATION_LOCAL_LAST_MODIFIED, String.valueOf(Calendar.getInstance().getTimeInMillis()));
                CommonShareData.putString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL, jsonObjectAll.optString(Const.NAVIGATION_SERVER_UPDATE_INTERVAL));
                CacheNavigation.getInstance().putNavigationList(navigationList);
            }
        } catch (Exception e) {
            msg.arg1 = PARSE_ERROR;
            Log.e("AsynchronousGet", ("Navigation解析出错：" + e.getMessage()));
        } finally {
            handler.sendMessage(msg);
//            if (msg.arg1 == SUCCESS)
//                CacheNavigation.getInstance().putNavigationList(navigationList);
        }
    }

    void parseLocaiton(String responseBody) {
//        final Weather[] weatherObject = {null};
        //获取城市中文名称
        try {
            final JSONObject jsonObject = new JSONObject(responseBody);
            if (jsonObject.get("status").equals("OK")) {//获取成功
                JSONObject resultAll = (JSONObject) jsonObject.get("result");
                JSONObject jsonAddressComponent = resultAll.getJSONObject("addressComponent");
                String jsonCityName = ((String) jsonAddressComponent.get("city"));
                if (jsonCityName.length() > 1) {//城市名称大于1
                    String cityName = jsonCityName.substring(0, jsonCityName.length() - 1);
                    msg.arg1 = SUCCESS;
                    msg.obj = cityName;
                } else if (jsonCityName.length() == 1) {//城市名称就一个字
                    String cityName = jsonCityName;
                    msg.arg1 = SUCCESS;
                    msg.obj = cityName;
                } else {//获取到城市名字为空
                    msg.arg1 = PARSE_ERROR;
                }
            } else {
                msg.arg1 = RESPONSE_FAIL;
            }

        } catch (Exception e) {
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }

    private void parseYiDian(String responseBody) {
        log("解析一点咨询内容: responseBody= " + responseBody);
//        handler.sendMessage(msg);
        List<YiDianModel> yiDianModelList = new ArrayList<>();
        try {
            JSONObject allYiDianObject = new JSONObject(responseBody);
            int code = allYiDianObject.getInt("code");
            if (code != 0) {//获取失败
                msg.arg1 = RESPONSE_FAIL;
            } else {//获取成功
                JSONArray jsonArrayYiDianModel = allYiDianObject.getJSONArray("result");
                int length = jsonArrayYiDianModel.length();
                for (int i = 0; i < length; i++) {
                    JSONObject jsonYidianMoedel = jsonArrayYiDianModel.getJSONObject(i);
                    String title = jsonYidianMoedel.optString("title");
                    String docid = jsonYidianMoedel.optString("docid");
                    String url = jsonYidianMoedel.optString("url");
                    String date = jsonYidianMoedel.optString("date");
                    String source = jsonYidianMoedel.optString("source");
                    //图片
                    String[] images = null;
                    if (jsonYidianMoedel.has("images")) {//包含图片
                        JSONArray jsonArrayImages = jsonYidianMoedel.getJSONArray("images");
                        int imageLength = jsonArrayImages.length();
                        images = new String[imageLength];
                        for (int j = 0; j < imageLength; j++) {
                            String image = jsonArrayImages.optString(j);
                            images[j] = image;
                        }
                    }

                    YiDianModel yiDianModel = new YiDianModel(title, docid, date, url, source, images);
                    yiDianModelList.add(yiDianModel);
                }
                msg.arg1 = SUCCESS;
                msg.obj = yiDianModelList;
            }
        } catch (Exception e) {
            Log.d("AsynchronousGet", "一点资讯解析出错:" + e.getMessage());
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }

    private void parseTimestamp(String responseBody) {
        try {
            JSONArray jsonArray = new JSONArray(responseBody);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            int time = (int) (jsonObject.getLong("time") / 1000);
            msg.arg1 = SUCCESS;
            msg.obj = String.valueOf(time);
        } catch (Exception e) {
            msg.arg1 = PARSE_ERROR;
            Log.d("AsynchronousGet", "时间戳解析出错:" + e.getMessage());
        } finally {
            handler.sendMessage(msg);
        }
    }

    private void parseConfigSwitch(String responseBodyStr) {
        try {
            JSONObject jsonObjectRoot = new JSONObject(responseBodyStr);
            JSONArray jsonArrayConfigList = jsonObjectRoot.getJSONArray("cw");
            if (null == jsonArrayConfigList || jsonArrayConfigList.length() == 0) {
                msg.arg1 = RESPONSE_FAIL;
            } else {
                //----------------
                int jsonArrayLength = jsonArrayConfigList.length();
                for (int i = 0; i < jsonArrayLength; i++) {
                    JSONObject configJson = jsonArrayConfigList.getJSONObject(i);
                    Iterator<String> keysIterator = configJson.keys();
                    while (keysIterator.hasNext()) {
                        String key = keysIterator.next();
                        if ("app_active".equals(key)) {
                            String value = configJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if ("kinfo".equals(key)) {
                            String value = configJson.optString(key);
                            CommonShareData.putString(key, value);
                        }
                    }
                }

                msg.arg1 = SUCCESS;
            }
        } catch (Exception e) {
            log("解析Config的Json数据时,出错:" + e.getMessage());
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
            log("解析config配置完毕"
                            + "  app_active = " + CommonShareData.getString("app_active", "默认")
                            + "  kinfo = " + CommonShareData.getString("kinfo", "默认")
            );
        }
    }

    private void parseConfigList(String responseBodyStr) {
        log("开始解析配置功能列表:parseConfigList");
        try {
            JSONObject jsonObjectRoot = new JSONObject(responseBodyStr);
            JSONArray jsonArrayFunList = jsonObjectRoot.getJSONArray("cw");
            if (null == jsonArrayFunList || jsonArrayFunList.length() == 0) {
                msg.arg1 = RESPONSE_FAIL;
            } else {
                int jsonArrayLength = jsonArrayFunList.length();
                for (int i = 0; i < jsonArrayLength; i++) {
                    JSONObject funJson = jsonArrayFunList.getJSONObject(i);
                    Iterator<String> keysIterator = funJson.keys();
                    while (keysIterator.hasNext()) {
                        String key = keysIterator.next();
                        if ("bd_prct".equals(key)) {
                            String value = funJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if ("sm_prct".equals(key)) {
                            String value = funJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if ("dis_act".equals(key)) {//
                            String value = funJson.optString(key);
                            String[] devList = value.split(",");
                            Set<String> set = new HashSet<>(Arrays.asList(devList));
                            CommonShareData.putSet("dis_act", set);
                        }
                    }
                }
                msg.arg1 = SUCCESS;
            }
        } catch (Exception e) {
            log("解析配置功能列表的Json数据时,出错:" + e.getMessage());
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
            //---
            Set<String> devicesSet = new HashSet<>();
            devicesSet = CommonShareData.getSet("dis_act", devicesSet);
            StringBuilder stringBuilder = new StringBuilder();
            for (String device : devicesSet) {
                stringBuilder.append(device).append(",");
            }
            log("解析配置功能列表完毕"
                            + "  bd_prct = " + CommonShareData.getString("bd_prct", "默认")
                            + "  sm_prct = " + CommonShareData.getString("sm_prct", "默认")
                            + "  dis_act = " + stringBuilder.toString()
            );
            if (devicesSet.contains("coolpad8702D")) {
                log("包含设备:coolpad8702D");
            } else {
                log("不包含设备:coolpad8702D");
            }
        }
    }

    private void parseConfig(String responseBodyStr) {
        log("开始解析所有配置项");
        try {
            JSONObject jsonObjectRoot = new JSONObject(responseBodyStr);
            JSONArray jsonArrayFunList = jsonObjectRoot.getJSONArray("cw");
            if (null == jsonArrayFunList || jsonArrayFunList.length() == 0) {
                msg.arg1 = RESPONSE_FAIL;
            } else {
                if (!CommonShareData.containsKey(CommonShareData.KEY_CONFIG_FIRST_UPDATE)) {//config更新时间
                    CommonShareData.putLong(CommonShareData.KEY_CONFIG_FIRST_UPDATE,
                            System.currentTimeMillis());
                }
                boolean appActive = false;
                boolean devEnable = true;
                int jsonArrayLength = jsonArrayFunList.length();
                for (int i = 0; i < jsonArrayLength; i++) {
                    JSONObject funJson = jsonArrayFunList.getJSONObject(i);
                    Iterator<String> keysIterator = funJson.keys();
                    while (keysIterator.hasNext()) {
                        String key = keysIterator.next();
                        if ("app_active".equals(key)) {//如果存在app_active这个key
                            String value = funJson.optString(key);
                            if ("1".equals(value)) {
                                appActive = true;
                            }
                        } else if ("dis_act".equals(key)) {//
                            String[] devArray = funJson.optString(key).split(",");
                            for (String dev : devArray) {
                                if (Build.MODEL.contains(dev)) devEnable = false;
                                break;
                            }
                        } else if ("kinfo".equals(key)) {
                            String value = funJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if ("bd_prct".equals(key)) {
                            String value = funJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if ("sm_prct".equals(key)) {
                            String value = funJson.optString(key);
                            CommonShareData.putString(key, value);
                        } else if (CommonShareData.KEY_ACTIVE_2345.equals(key)) {//是否激活2345跳转---->网页跳转
                            String value = funJson.optString(key);
                            CommonShareData.putBoolean(key, "1".equals(value));//如果为1,则激活跳转,否则关闭跳转
                        } else if (CommonShareData.KEY_ACTIVE_INTERVAL_2345.equals(key)) {//网页跳转的间隔时间
                            String value = funJson.optString(key);
                            try {
                                CommonShareData.putInt(key, Integer.valueOf(value));
                            } catch (Exception e) { }
                        } else if (CommonShareData.KEY_WEBPAGE_SKIP_URL_LIST.equals(key)) {//跳转网页的,网址列表
                                CommonShareData.putString(key,funJson.optString(key));
                        } else if (CommonShareData.KEY_OPERATOR_DELAY.equals(key)) {
                            String value = funJson.optString(key);
                            try {
                                CommonShareData.putInt(key, Integer.valueOf(value));
                            } catch (Exception e) { }
                        }
                    }
                }
                CommonShareData.putBoolean(CommonShareData.KEY_APP_ACTIVE, appActive && devEnable);
                msg.arg1 = SUCCESS;
            }
        } catch (Exception e) {
            log("解析配置Json数据时,出错:" + e.getMessage());
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
//            log("获取到所有配置项:\n"
//                     + "  kinfo = " + CommonShareData.getString("kinfo", "默认")
//                    +"  active="+CommonShareData.getBoolean("active", false)
//                    +"  bd_prct="+CommonShareData.getString("bd_prct", "默认")
//                    +"  sm_prct="+CommonShareData.getString("sm_prct","默认")
//                    + "  当前设备名称= " + Build.MODEL
//            );
        }
    }

    public void parseServerController(String responseJsonStr) {
        /*
        try {
            JSONObject responseJsonObject = new JSONObject(responseJsonStr);
            log("开始解析服务器端控制ServiceController,开始打印:\n");
            ServerController serverController = new ServerController(responseJsonObject);
            if (serverController.isNull()) {
                msg.arg1 = OBTAIN_RESULT_NULL;
            }else {
                msg.arg1 = SUCCESS;
                msg.obj = serverController;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            log("解析服务端控制器的时候出错:"+e.getMessage());
            e.printStackTrace();
        }
        */

        try {
            JSONObject responseJsonObject = new JSONObject(responseJsonStr);
            log("开始解析服务器端控制ServiceController,开始打印:\n");
            ServerController serverController = new ServerController(responseJsonObject);
            if (serverController.isNull()) {
                msg.arg1 = OBTAIN_RESULT_NULL;
            }else {
                msg.arg1 = SUCCESS;
                msg.obj = serverController;
            }
        } catch (Exception e) {
            log("解析服务端控制器的时候出错:"+e.getMessage());
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }

    }

    final protected static void log(String msg) {
        KinflowLog.e(msg);
    }
}
