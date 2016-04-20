package com.klauncher.kinflow;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.baidu.apistore.sdk.ApiStoreSDK;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CacheHotWord;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.ss.android.sdk.minusscreen.SsNewsApi;

/**
 * Created by xixionghui on 2016/3/16.
 */
public class KinflowApplication extends Application{
    String clientId = "delong";
    public static String cid;
    @Override
    public void onCreate() {
        super.onCreate();
        //今日头条
        SsNewsApi.init(getApplicationContext(), clientId, true);
        //百度天气
        ApiStoreSDK.init(this, Const.BAIDU_APIKEY);
        //三个个缓存文件
        CommonShareData.init(getApplicationContext());
        CacheNavigation.getInstancce().createCacheFile(getApplicationContext());
        CacheHotWord.getInstance().createCacheHotWord(getApplicationContext());
        //getCid
        try {
            ApplicationInfo applicationInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            cid = applicationInfo.metaData.getString("KappChannel");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
