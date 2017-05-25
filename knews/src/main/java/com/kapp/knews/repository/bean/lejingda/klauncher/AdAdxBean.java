package com.kapp.knews.repository.bean.lejingda.klauncher;

import android.view.View;

/**
 * description：添加广告view的Adbean
 * <br>author：caowugao
 * <br>time： 2017/01/05 11:12
 */
public class AdAdxBean extends KlauncherAdBean {
    private View adxView;//ADX-SDK拿到的view

    public AdAdxBean(View adxView) {
        super(null, null);
        this.adxView=adxView;
    }

    public View getAdxView() {
        return adxView;
    }

    public void setAdxView(View adxView) {
        this.adxView = adxView;
    }
}
