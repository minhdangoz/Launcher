package com.kapp.knews.repository.bean;


import com.kapp.knews.base.recycler.data.NewsBaseDataControl;

import java.util.List;


/**
 * Created by xixionghui on 2016/11/28.
 */

public class DongFangTouTiaoControl extends NewsBaseDataControl {

    public static final int contentProvider = 1;//内容提供商的标识为11=Klauncher后台提供的广告内容

    public DongFangTouTiaoControl(int order, List<String> openOptions) {
        super(order, openOptions);
    }
}
