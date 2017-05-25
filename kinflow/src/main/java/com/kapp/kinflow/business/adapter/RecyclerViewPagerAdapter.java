package com.kapp.kinflow.business.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * description：recyclew的PagerAdapter
 * <br>author：caowugao
 * <br>time： 2017/05/23 16:22
 */

public class RecyclerViewPagerAdapter extends PagerAdapter {

    private List<RecyclerView> recyclerViewList;

    public RecyclerViewPagerAdapter(List<RecyclerView> recyclerViewList) {
        this.recyclerViewList = recyclerViewList;
    }

    @Override
    public int getCount() {
        return null == recyclerViewList ? 0 : recyclerViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RecyclerView recyclerView = recyclerViewList.get(position);
        ((ViewPager) container).addView(recyclerView);
        return recyclerView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        RecyclerView recyclerView = recyclerViewList.get(position);
        ((ViewPager) container).removeView(recyclerView);
    }

}
