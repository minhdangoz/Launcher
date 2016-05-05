package com.klauncher.ext;

import android.app.Application;

import com.android.system.ReporterApi;
import com.baidu.apistore.sdk.ApiStoreSDK;
import com.klauncher.kinflow.common.utils.CacheHotWord;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.ping.PingManager;
import com.ss.android.sdk.minusscreen.SsNewsApi;

/**
 * Created by yanni on 16/3/27.
 */
public class KLauncherApplication extends Application {
    //kinflow
    String clientId = "delong";

    @Override
    public void onCreate() {
        super.onCreate();
        // Init DELONG report
        ReporterApi.onApplicationCreated(this);
        ReporterApi.startService(this, ReporterApi.POST_AS_REGULAR);
        KLauncherAppDisguise.getInstance().init(this);
        PingManager.getInstance().init(this);

        //init kinflow
        //今日头条
        SsNewsApi.init(getApplicationContext(), clientId, true);
        //百度天气
        ApiStoreSDK.init(this, Const.BAIDU_APIKEY);
        //三个个缓存文件
        CommonShareData.init(getApplicationContext());
        CacheNavigation.getInstance().createCacheFile(getApplicationContext());
        CacheHotWord.getInstance().createCacheHotWord(getApplicationContext());
        //此版本已没有天气模块,但是保留天气模块相关代码
//        CacheLocation.getInstance().createCacheLocation(getApplicationContext());
    }
}
