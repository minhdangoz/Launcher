package com.kapp.kinflow.business.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/03/31 19:22
 */

public class CommonFragmentPageAdapter extends FragmentPagerAdapter {


    private List<Fragment> fragments;
    private List<String> tittles;

    /**
     * @param fm
     * @param fragments
     * @param tittles
     */
    public CommonFragmentPageAdapter(FragmentManager fm, List<Fragment> fragments, List<String> tittles) {
        super(fm);
        this.fragments = fragments;
        this.tittles = tittles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null == tittles ? "" : tittles.get(position % tittles.size());
    }

    /**
     * @param fm
     */
    public CommonFragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        return fragments.get(index);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}

