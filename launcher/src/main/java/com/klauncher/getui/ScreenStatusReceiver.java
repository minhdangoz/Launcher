package com.klauncher.getui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.CommonUtils;

/**
 * Created by wangqinghao on 2016/5/24.
 */
public class ScreenStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ScreenStatusReceiver",intent.getAction());

        SharedPreferences settings = context.getSharedPreferences("miguan", 0);
        boolean isSendBroadcast = false;
        //每天第一次  发广播
        long currentTime = System.currentTimeMillis();
        Log.e("ScreenStatusReceiver","currentTime "+currentTime);
        long lastTime = settings.getLong("lastTime", 0);
        Log.e("ScreenStatusReceiver","lastTime "+lastTime);
        if (lastTime > 0 && (int) (currentTime - lastTime) / (24 * 60 * 60 * 1000) > 0) {
            isSendBroadcast = true;
            //超过一天  将当前时间存为上次时间
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("lastTime", currentTime);
            editor.commit();
            Log.e("ScreenStatusReceiver","11111 ");
        } else if (lastTime == 0) {
            //第一次计时
            isSendBroadcast = true;
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("lastTime", currentTime);
            editor.commit();
            Log.e("ScreenStatusReceiver","22222 ");
        } else {
            isSendBroadcast = false;
        }
        if (isSendBroadcast && CommonUtils.getInstance().canOperateNow()) {
            Intent miguanIntent = new Intent();
            miguanIntent.setAction("com.miguan.folder.CREATE_SHOT_CUT");
            context.sendBroadcast(miguanIntent);
            Log.e("ScreenStatusReceiver","sendBroadcast ");
        }
    }
}
