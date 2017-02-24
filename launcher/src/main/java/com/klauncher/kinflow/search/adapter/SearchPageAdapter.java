package com.klauncher.kinflow.search.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by xixionghui on 2016/3/15.
 */
public class SearchPageAdapter extends PagerAdapter {
    private ArrayList<View> views;


    private void setViews(ArrayList<View> views) {
        if (views != null)
            this.views = views;
        else
            this.views = new ArrayList<View>();
    }

    public SearchPageAdapter(ArrayList<View> views) {
        this.setViews(views);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    /**
     * 删除页卡
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }


    /**
     * 实例化页卡
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position),0);
        return views.get(position);
    }





















}
