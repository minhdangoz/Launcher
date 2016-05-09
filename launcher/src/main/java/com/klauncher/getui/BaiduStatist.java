package com.klauncher.getui;


import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 百度统计
 * Created by wangqinghao on 2016/5/4.
 * 0509 不设默认显示 统计url链接 推送过来数据设置 链接为空不统计
 */
public class BaiduStatist {
    /* public static String STATIST_SHOW_URL = "http://debug.mobads.baidu.com/mopt/show?uuid=33420fee-bcac-4a2a-8c2c-4d05c991d871";
     public static String STATIST_CLICK_URL = "http://debug.mobads.baidu.com/mopt/click?uuid=33420fee-bcac-4a2a-8c2c-4d05c991d871";
     */
    public static String STATIST_SHOW_URL = "";
    public static String STATIST_CLICK_URL = "";

    public static void setShowStatist(String showstatisturl) {
        STATIST_SHOW_URL = showstatisturl;
    }

    public static void setOnclickStatist(String onclickstatisturl) {
        STATIST_CLICK_URL = onclickstatisturl;
    }

    public static void reportClickStatist() {
        if (TextUtils.isEmpty(STATIST_CLICK_URL)) {
            Log.e("BaiduStatist", "STATIST_CLICK_URL null");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STATIST_CLICK_URL).build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("BaiduStatist", "reportClickStatist onFailure");

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("BaiduStatist", "reportClickStatist success");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportShowStatist() {
        if (TextUtils.isEmpty(STATIST_SHOW_URL)) {
            Log.e("BaiduStatist", "STATIST_SHOW_URL null");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STATIST_SHOW_URL).build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("BaiduStatist", "reportShowStatist onFailure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("BaiduStatist", "reportShowStatist success");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
