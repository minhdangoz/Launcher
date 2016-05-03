package com.klauncher.getui;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by wangqinghao on 2016/5/3.
 */
public class NotifyClickService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String strType = intent.getStringExtra("type");
            if("startApp".equals(strType)){
                //打开App
                String pkgName = intent.getStringExtra("pkgName");
                String startActName = intent.getStringExtra("startActName");

                Intent intent1 = new Intent(Intent.ACTION_MAIN);
                intent1.setClassName(pkgName,startActName);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }else if("downApk".equals(strType)){
                //下载未安装的apk
                String downUrl = intent.getStringExtra("downUrl");
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(downUrl));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
            }else if("openAd".equals(strType)){
                //打开广告
                String url = intent.getStringExtra("url");
                Intent intent3 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent3);

            }

        }
        return super.onStartCommand(intent, flags, startId);
    }
}
