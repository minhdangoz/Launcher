package com.klauncher.ext;

import android.app.Application;

import com.android.system.ReporterApi;
import com.klauncher.ping.PingManager;

/**
 * Created by yanni on 16/3/27.
 */
public class KLauncherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Init DELONG report
        ReporterApi.onApplicationCreated(this);
        ReporterApi.startService(this, ReporterApi.POST_AS_REGULAR);
        KLauncherAppDisguise.getInstance().init(this);
        PingManager.getInstance().init(this);
    }
}
