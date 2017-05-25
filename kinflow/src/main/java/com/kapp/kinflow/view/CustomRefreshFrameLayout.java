package com.kapp.kinflow.view;

import android.content.Context;
import android.util.AttributeSet;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * description：具有下拉效果的布局
 * <br>author：caowugao
 * <br>time： 2017/04/18 19:28
 */
public class CustomRefreshFrameLayout  extends PtrFrameLayout {
    public CustomRefreshFrameLayout(Context context) {
        super(context);
        init();
    }

    public CustomRefreshFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRefreshFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        PtrMeiTuanHeader mHeaderView = new PtrMeiTuanHeader(getContext());
        setHeaderView(mHeaderView);
        addPtrUIHandler(mHeaderView);
    }
}
