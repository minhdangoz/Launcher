package com.klauncher.kinflow.views.recyclerView.data;

import android.view.View;

/**
 * description： ADX-SDK的Banner
 * <br>author：caowugao
 * <br>time： 2017/01/13 10:38
 */

public class AdxSdkBanner extends BaseRecyclerViewAdapterData  {
    private View adxView;//ADX-SDK拿到的view
    public AdxSdkBanner(View adxView){
        this.adxView=adxView;
        kinflowConentType=TYPE_ADX_BANNER;
    }
    public View getAdxView() {
        return adxView;
    }

    public void setAdxView(View adxView) {
        this.adxView = adxView;
    }
}
