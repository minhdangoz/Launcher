package com.kapp.knews.repository.bean;


import com.kapp.knews.base.recycler.data.NewsBaseData;

/**
 * Created by xixionghui on 2016/11/23.
 */

public class DongFangTouTiao extends NewsBaseData<DongFangTouTiaoControl, DongFangTouTiaoBean> {

    public static final int contentProvider = 11;//内容提供商的标识为11=东方头条


    public DongFangTouTiao(DongFangTouTiaoControl newsControl, DongFangTouTiaoBean newsBean) {
        super(NewsBaseData.TYPE_NEWS, contentProvider, newsControl, newsBean);
    }

    public DongFangTouTiao(DongFangTouTiaoBean newsBean) {
        this(null, newsBean);
    }





}
