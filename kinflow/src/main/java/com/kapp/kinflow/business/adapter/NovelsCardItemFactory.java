package com.kapp.kinflow.business.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.R;


/**
 * description：小说卡片item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:49
 */

public class NovelsCardItemFactory extends MineNovelsItemFactory {
    @Override
    public MineNovelsHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
//        View view = getView(parent, R.layout.item_mine_novels);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_novels, parent, false);
        MineNovelsHolder mineNovelsHolder = new MineNovelsHolder(view);
        mineNovelsHolder.tvTitle.setTextColor(Color.WHITE);
        mineNovelsHolder.tvTotal.setTextColor(Color.WHITE);
        mineNovelsHolder.tvStatus.setTextColor(Color.WHITE);
        return mineNovelsHolder;
    }
}
