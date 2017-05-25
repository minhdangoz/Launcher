package com.kapp.knews.base.recycler.data;

/**
 * Created by xixionghui on 2016/11/22.
 * BaseData包含属性：
 * 数据类型（新闻类||广告类）
 * 图片个数
 * 顺序
 * 摘要标题
 * 摘要时间
 */

public abstract class NewsBaseData<T extends NewsBaseDataControl,V extends NewsBaseDataBean > implements BaseDataInterface{


    public static final int TYPE_NEWS  = 1;//新闻类型
    public static final int TYPE_AD  = 2;//广告类型


    //====基本特性类
    public int dataType;//数据类型：新闻类型，广告类型
    public int contentProvider;//内容提供商：例如，东方头条，今日头条


    //====控制类
    public T mNewsControl;


    //====新闻属性类
    public V mNewsBean;


    public NewsBaseData(int dataType, int contentProvider, T newsControl, V newsBean) {
        this.dataType = dataType;
        this.contentProvider = contentProvider;
        this.mNewsControl = newsControl;
        this.mNewsBean = newsBean;
    }



    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(int contentProvider) {
        this.contentProvider = contentProvider;
    }

    public NewsBaseDataControl getmNewsControl() {
        return mNewsControl;
    }

    public void setmNewsControl(T mNewsControl) {
        this.mNewsControl = mNewsControl;
    }

    public V getmNewsBean() {
        return mNewsBean;
    }

    public void setmNewsBean(V mNewsBean) {
        this.mNewsBean = mNewsBean;
    }

    @Override
    public boolean isNull() {
        if (dataType!=TYPE_AD
                || dataType!=TYPE_NEWS
                || mNewsControl.isNull()
                || mNewsBean.isNull()
                )
            return true;
        return false;
    }

}
