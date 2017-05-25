package com.kapp.kinflow.business.adapter;

import android.view.ViewGroup;

import com.kapp.kinflow.R;


/**
 * description：网址大全顶部app item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/28 10:47
 */
public class MoreSiteHeaderAppItemFactory extends HeaderAppItemFactory {
    @Override
    public HeaderAppHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        HeaderAppHolder headerAppHolder = super.onCreateNormalViewHolder(parent, viewType);
        headerAppHolder.tvIcon.setTextColor(parent.getResources().getColor(R.color.app_text_black_color));
        return headerAppHolder;
    }
}