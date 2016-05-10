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

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.utilities.KinflowLog;

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

    public void run(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                msg.arg1 = CONNECTION_ERROR;
                handler.sendMessage(msg);

            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    msg.arg1 = RESPONSE_FAIL;
                    handler.sendMessage(msg);
                    response.body().close();
                    return;
                }
                String responseBodyStr = null;
                try {
                    responseBodyStr = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.arg1 = RESPONSE_FAIL;
                    handler.sendMessage(msg);
                    log("服务器响应失败,发生IOException,msg.what="+msg.what);
                    response.body().close();
                }
                if (!TextUtils.isEmpty(responseBodyStr))
                switch (msg.what) {
                    case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                        parseHotWord(responseBodyStr);
                        break;
                    case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                        parseNavigation(responseBodyStr);
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
                }
                response.body().close();
            }
        });
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
                    hotWordList.add(new HotWord(String.valueOf(i), jsonHotWord.getString("word"), jsonHotWord.getString("url")));
                }
                msg.arg1 = SUCCESS;
                msg.obj = hotWordList;
            }
        } catch (JSONException e) {
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }

    void parseNavigation(String responseBody) {
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
            JSONArray navigationJsonArray = jsonObjectAll.getJSONArray("cards");
            int navigationsLength = navigationJsonArray.length();
            if (null == navigationJsonArray || navigationsLength == 0) {
                msg.arg1 = OBTAIN_RESULT_NULL;
                return;
            }

            for (int i = 0; i < navigationsLength; i++) {
                Navigation navigation = new Navigation();
                JSONObject navigationJsonObject = navigationJsonArray.getJSONObject(i);
                navigation.setNavId(navigationJsonObject.getString(Navigation.NAV_ID));
                navigation.setNavName(navigationJsonObject.getString(Navigation.NAV_NAME));
                navigation.setNavIcon(navigationJsonObject.getString(Navigation.NAV_ICON));
                navigation.setNavUrl(navigationJsonObject.getString(Navigation.NAV_URL));
                navigation.setNavOrder(navigationJsonObject.getInt(Navigation.NAV_ORDER));
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
                        opsList.add(opsJsonArray.getString(j));
                    }
                }
                navigation.setNavOpenOptions(opsList);
                navigationList.add(navigation);
            }
            msg.arg1 = SUCCESS;
            msg.obj = navigationList;
            CommonShareData.putString(Const.NAVIGATION_LOCAL_LAST_MODIFIED, String.valueOf(Calendar.getInstance().getTimeInMillis()));
            CommonShareData.putString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL, jsonObjectAll.getString(Const.NAVIGATION_SERVER_UPDATE_INTERVAL));
        } catch (JSONException e) {
            msg.arg1 = PARSE_ERROR;
            Log.d("AsynchronousGet", ("Navigation解析出错：" + e.getMessage()));
        } finally {
            handler.sendMessage(msg);
            if (msg.arg1 == SUCCESS)
                CacheNavigation.getInstance().putNavigationList(navigationList);
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

        } catch (JSONException e) {
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }

    private void parseYiDian(String responseBody) {
//        Log.d("Kinflow", "parseYiDian: responseBody= "+responseBody);
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
                    }

                    YiDianModel yiDianModel = new YiDianModel(title, docid, date, url, source, images);
                    yiDianModelList.add(yiDianModel);
                }
            }
            msg.arg1 = SUCCESS;
            msg.obj = yiDianModelList;
        } catch (JSONException e) {
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
        } catch (JSONException e) {
            msg.arg1 = PARSE_ERROR;
            e.printStackTrace();
        } finally {
            handler.sendMessage(msg);
        }
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }
}
