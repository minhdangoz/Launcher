package com.klauncher.getui;


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
 */
public class BaiduStatist {
    public static final String STATIST_CLICK_URL = "http://debug.mobads.baidu.com/mopt/click?uuid=33420fee-bcac-4a2a-8c2c-4d05c991d871";
    public static final String STATIST_SHOW_URL = "http://debug.mobads.baidu.com/mopt/show?uuid=33420fee-bcac-4a2a-8c2c-4d05c991d871";

    public static void reportClickStatist() {
        Log.d("BaiduStatist","reportClickStatist 1111111");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STATIST_CLICK_URL).build();
        Log.d("BaiduStatist","reportClickStatist 222222");
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("BaiduStatist","reportClickStatist onFailure");

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("BaiduStatist","reportClickStatist success");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportShowStatist() {
        Log.d("BaiduStatist","reportShowStatist 1111111");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STATIST_SHOW_URL).build();
        Log.d("BaiduStatist","reportShowStatist 22222");

        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("BaiduStatist","reportShowStatist onFailure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("BaiduStatist","reportShowStatist success");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
