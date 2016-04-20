package com.klauncher.kinflow.cards.adapter;

import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by yanni on 16/4/10.
 */
public class CardItemDecoration extends RecyclerView.ItemDecoration {
    private int mVerticalSpacing = 0;

    public CardItemDecoration(int spacing) {
        mVerticalSpacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (view instanceof CardView) {
            outRect.top = mVerticalSpacing;
        }
    }
}
