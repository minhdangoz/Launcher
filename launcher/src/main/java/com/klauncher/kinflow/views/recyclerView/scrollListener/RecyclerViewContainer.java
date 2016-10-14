package com.klauncher.kinflow.views.recyclerView.scrollListener;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by xixionghui on 16/9/2.
 */
public class RecyclerViewContainer extends RelativeLayout {

    RecyclerView mRecyclerView;
    ViewDragHelper mViewDragHelper;
    

    public RecyclerViewContainer(Context context) {
        super(context);
//        mViewDragHelper = ViewDragHelper.create(this,);
    }

    public RecyclerViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
