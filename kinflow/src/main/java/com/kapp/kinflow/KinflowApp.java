package com.kapp.kinflow;


import com.kapp.kinflow.business.http.OKHttpUtil;
import com.kapp.knews.KnewsApp;
import com.kapp.knews.utils.SystemUtil;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/18 19:50
 */

public class KinflowApp extends KnewsApp {

    @Override
    public void onCreate() {
        super.onCreate();
//        FreelineCore.init(this);
        OKHttpUtil.init(this, SystemUtil.getApplicationName(this), null);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        OKHttpUtil.release();
    }
}
