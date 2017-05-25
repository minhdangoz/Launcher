package com.kapp.knews.repository.bean.lejingda.klauncher;

import android.os.Parcel;

import com.kapp.knews.base.recycler.data.NewsBaseDataControl;

import java.util.List;


/**
 * Created by xixionghui on 2016/12/16.
 */

public class KlauncherAdControl extends NewsBaseDataControl {
    public KlauncherAdControl(int order, List<String> openOptions) {
        super(order, openOptions);
    }

    protected KlauncherAdControl(Parcel in) {
        super(in);
    }
}
