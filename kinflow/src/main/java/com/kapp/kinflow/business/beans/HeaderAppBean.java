package com.kapp.kinflow.business.beans;

import android.content.Intent;
import android.graphics.Bitmap;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;


/**
 * description：头部应用列表
 * <br>author：caowugao
 * <br>time： 2017/04/19 20:20
 */

public class HeaderAppBean extends BaseItemBean {
    public Bitmap icon;
    public String name;
    public Intent startIntent;

    public HeaderAppBean(Bitmap icon, String name, Intent startIntent) {
        setInnerData(icon, name, startIntent);
    }

    private void setInnerData(Bitmap icon, String name, Intent startIntent) {
        this.icon = icon;
        this.name = name;
        this.startIntent = startIntent;
    }

    public void updateContent(HeaderAppBean bean) {
        setInnerData(bean.icon, bean.name, bean.startIntent);
    }
}
