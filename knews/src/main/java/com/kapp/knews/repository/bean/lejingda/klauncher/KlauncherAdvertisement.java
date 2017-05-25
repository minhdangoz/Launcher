package com.kapp.knews.repository.bean.lejingda.klauncher;


import com.kapp.knews.base.recycler.data.NewsBaseData;

/**
 * Created by xixionghui on 2016/12/16.
 */

public class KlauncherAdvertisement extends NewsBaseData<KlauncherAdControl,KlauncherAdBean> {

    public static final int contentProvider = 11;//内容提供商的标识为11=东方头条

    public KlauncherAdvertisement(KlauncherAdControl newsControl, KlauncherAdBean newsBean) {
        super(NewsBaseData.TYPE_AD, contentProvider, newsControl, newsBean);
    }

    public KlauncherAdvertisement(KlauncherAdBean newsBean) {
        this(null,newsBean);
    }

}
