package com.klauncher.ext;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.settings.SettingsValue;
import com.android.system.ReporterApi;
import com.dl.statisticalanalysis.MobileStatistics;
import com.igexin.sdk.PushManager;
import com.klauncher.getui.ScreenStatusReceiver;
import com.klauncher.kinflow.common.utils.CacheHotWord;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.launcher.BuildConfig;
import com.klauncher.ping.PingManager;
import com.klauncher.utilities.LogUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanni on 16/3/27.
 */
public class KLauncherApplication extends Application {
    //kinflow
    String clientId = "delong";
    public static KLauncherApplication mKLauncherApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        //log mode 测试
        LogUtil.mLogModel = LogUtil.LogModel.DEBUG;
        LogUtil.e("LogUtil","KLauncherApplication onCreate ");
        // Init DELONG report
        ReporterApi.onApplicationCreated(this);
        ReporterApi.startService(this, ReporterApi.POST_AS_REGULAR);
        KLauncherAppDisguise.getInstance().init(this);
        PingManager.getInstance().init(this);

        //init kinflow
        //今日头条
//        SsNewsApi.init(getApplicationContext(), clientId, true);
        //本公司sdk统计:MobileStatistics.init(Context context,String productId,String clientChannel);
        MobileStatistics.init(this, BuildConfig.CHANNEL_ID, "103");//productID 用BuildConfig.CHANNEL_ID,channel=103
        MobileStatistics.flushBuffer(50, 10 * 60, TimeUnit.MINUTES);//全部设置为默认值

        //三个个缓存文件
        CommonShareData.init(getApplicationContext());
        CacheNavigation.getInstance().createCacheFile(getApplicationContext());
        CacheHotWord.getInstance().createCacheHotWord(getApplicationContext());
        //此版本已没有天气模块,但是保留天气模块相关代码
//        CacheLocation.getInstance().createCacheLocation(getApplicationContext());
        mKLauncherApplication = this;
        //crash save sd
       /* CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());*/
        initGeitui();
        initScreenStatusReceiver();
        //读取包名 并存储到本地
        getAndSavePackageName();
        //高低机型判断
        //initKinflowStatus();
        //CommonShareData.getBoolean("active", false);
        //obtain false  关闭信息流配置项  不包含 true 打开配置项
        //SettingsValue.setKinflowSetOn(getApplicationContext(),CommonShareData.getBoolean(
        //        CommonShareData.KEY_APP_ACTIVE, false));

    }
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;
    public void initGeitui() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = mConnectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
            Log.e("KLauncherApplication","initializing GetuiSdk...");
            PushManager.getInstance().initialize(this.getApplicationContext());
            //add crash update
           // CrashHandler.getInstance().uploadErrorLog();
        }
    }
    ScreenStatusReceiver receiver = new ScreenStatusReceiver();
    public void initScreenStatusReceiver(){
        //ACTION_SCREEN_OFF ACTION_SCREEN_ON 事件需要动态注册才能监听到
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }
    public void unRegistBrocaster(){
        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }

    public void getAndSavePackageName() {
        //Thread.dumpStack(); 跟踪方法调用
        PackageInfo info;
        String packageNames = "";
        try {
            info = getApplicationContext().getPackageManager().getPackageInfo(this.getPackageName(), 0);
            // 当前应用的版本名称
            //String versionName = info.versionName;
            // 当前版本的版本号
            //int versionCode = info.versionCode;
            // 当前版本的包名
            packageNames = info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            packageNames = "null";
        } finally {
            CommonShareData.putString(CommonShareData.KEY_APP_PACKAGE_NAME, packageNames);
        }
        LogUtil.e("getAndSavePackageName","getAndSavePackageName pkgname= "+packageNames);

    }
    /*//低配手机列表
    String[] lowModeList = {

    };*/
    /*public void initKinflowStatus(){
        //已经打开信息流
        if(SettingsValue.isKinflowSetOn(getApplicationContext())){
            boolean isContain = false;
            String currentmode = android.os.Build.MODEL;
            if(!TextUtils.isEmpty(currentmode)) {
                //判断是否包含
                for (String mode : lowModeList) {
                    if (mode.equalsIgnoreCase(currentmode)) {
                        isContain = true;
                        break;
                    }
                }
            }
            if(isContain){
                SettingsValue.setKinflowSetOn(getApplicationContext(),false);
            }

        }

    }*/

}
