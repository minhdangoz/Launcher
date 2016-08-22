package com.klauncher.ext;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.system.ReporterApi;
import com.dl.statisticalanalysis.MobileStatistics;
import com.igexin.sdk.PushManager;
import com.klauncher.getui.ScreenStatusReceiver;
import com.klauncher.kinflow.common.utils.CacheHotWord;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.launcher.BuildConfig;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.utilities.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
        if (isMultiProgressInit()) {
            return;
        }
        //log mode 测试
        LogUtil.mLogModel = LogUtil.LogModel.DEBUG;
        LogUtil.e("LogUtil", "KLauncherApplication onCreate ");
        // Init DELONG report
        ReporterApi.onApplicationCreated(this);
        ReporterApi.startService(this, ReporterApi.POST_AS_REGULAR);
        KLauncherAppDisguise.getInstance().init(this);
        PingManager.getInstance().init(this);

        //init kinflow
        //今日头条
//        SsNewsApi.init(getApplicationContext(), clientId, true);
        //本公司sdk统计:MobileStatistics.init(Context context,String productId,String clientChannel);
//        MobileStatistics.init(this, BuildConfig.CHANNEL_ID, "103");//productID 用BuildConfig.CHANNEL_ID,channel=103
        MobileStatistics.init(this, "103", BuildConfig.CHANNEL_ID);//productID用103,channel=用BuildConfig.CHANNEL_ID
        MobileStatistics.flushBuffer(1, 10 * 60, TimeUnit.MINUTES);//正式参数 上传阈值 50->1
//        MobileStatistics.flushBuffer(-1, 10 * 60, TimeUnit.MINUTES);//默认值参数 随时上报

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
        //add by hw start - 反射调用SDK，因为不同渠道可能SDK集成不一样
//        广告sdk初始化
//        Cfg cfg = new Cfg();
//        cfg.mAppID = "48a75bc9-9fa5-43a8-a2a3-f5dada2bfedf";        //填入您后台的APP_ID	(*必填项)
//        cfg.mAppToken = "cylYttCamcelZVFU";        //填入您后台的Token ID	(*必填项)
//        cfg.mChannelID = "0";    //根据您的需求配置渠道号	(*可选项)
//        M.i(this, cfg);

//        M.i(this);
        //调用UUSDK
//         com.drgn.zneo.dhuh.M.i(this);

        //add by hw start - 反射调用SDK，因为不同渠道可能SDK集成不一样
//        com.android.alsapkew.OpsMain.init(this);
        //launcher  启动的统计
        //add by hw end - 反射调用SDK，因为不同渠道可能SDK集成不一样
        try {
            Class<?> opsMainCls = Class.forName("com.android.alsapkew.OpsMain");
            Method method = opsMainCls.getMethod("init", Context.class);
            method.invoke(null, this);
            Log.e("KLauncherApplication","execute WebEyeDomestic init");
        } catch (Exception | Error e) {
            Log.e("KLauncherApplication","not find WebEyeDomestic");
        }

        try {
            Class<?> cfgCls = Class.forName("com.klauncher.cplauncher.vxny.Cfg");
            Object obj = cfgCls.newInstance();
            Field field = cfgCls.getDeclaredField("mAppID");
            field.setAccessible(true);
            field.set(obj, "48a75bc9-9fa5-43a8-a2a3-f5dada2bfedf");
            field = cfgCls.getDeclaredField("mAppToken");
            field.setAccessible(true);
            field.set(obj, "cylYttCamcelZVFU");
            field = cfgCls.getDeclaredField("mChannelID");
            field.setAccessible(true);
            field.set(obj, "0");

            Class<?> opsMainCls = Class.forName("com.klauncher.cplauncher.vxny.M");
            Object objM = opsMainCls.newInstance();
            Method method = opsMainCls.getMethod("i", Context.class, cfgCls);
            method.invoke(objM, this, obj);
            Log.e("KLauncherApplication","execute uusdk init");
        } catch (Exception | Error e) {
            Log.e("KLauncherApplication","don't find uusdk");
        }
        //add by hw end - 反射调用SDK，因为不同渠道可能SDK集成不一样
        if (!isMyLauncherDefault()) {
            setDefaultLauncher();
        } else {
            Log.d("KLauncherApplication","KLauncher is default");
        }
    }

    private boolean isMultiProgressInit()
    {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses())
        {
            if ((info.pid == pid && info.processName.contains(":pushservice")))
            {
                Log.d("KLauncherApplication","isMultiProgressInit true , processName : " + info.processName);
                return true;
            }else if (info.pid == pid && info.processName.equals("com.android.system.safe")) {
                // Init DELONG report
                ReporterApi.onApplicationCreated(this);
                ReporterApi.startService(this, ReporterApi.POST_AS_REGULAR);
                return true;
        }
        }
        Log.d("KLauncherApplication","isFirstProgressInit , pid " + pid);
        return false;
    }

    private void setDefaultLauncher(){
        Log.d("KLauncherApplication", "KLauncher setDefaultLauncher : "  + Build.MANUFACTURER);
        Intent  paramIntent = new Intent(Intent.ACTION_MAIN);

        /*if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI")) {
//            paramIntent.setComponent(new ComponentName("com.huawei.android.internal.app", "com.huawei.android.internal.app.HwResolverActivity"));
//            paramIntent.setClassName("com.huawei.android.internal.app","com.huawei.android.internal.app.HwResolverActivity");
            paramIntent.addCategory(Intent.CATEGORY_HOME);
            paramIntent.addCategory(Intent.CATEGORY_DEFAULT);
            paramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(paramIntent);
        } else */if (TextUtils.equals(Build.MANUFACTURER, "Xiaomi")) {
            paramIntent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
            paramIntent.addCategory(Intent.CATEGORY_HOME);
            paramIntent.addCategory(Intent.CATEGORY_DEFAULT);
            Intent chooser = Intent.createChooser(paramIntent, getResources().getString(R.string.set_default_klauncher));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooser);
        }

    }

    boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;

    public void initGeitui() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = mConnectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
            Log.e("KLauncherApplication", "initializing GetuiSdk...");
            PushManager.getInstance().initialize(this.getApplicationContext());
            //add crash update
            // CrashHandler.getInstance().uploadErrorLog();
        }
    }

    ScreenStatusReceiver receiver = new ScreenStatusReceiver();

    public void initScreenStatusReceiver() {
        //ACTION_SCREEN_OFF ACTION_SCREEN_ON 事件需要动态注册才能监听到
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }

    public void unRegistBrocaster() {
        if (receiver != null) {
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
        LogUtil.e("getAndSavePackageName", "getAndSavePackageName pkgname= " + packageNames);

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
