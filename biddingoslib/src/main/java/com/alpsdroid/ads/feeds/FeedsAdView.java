package com.alpsdroid.ads.feeds;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by：lizw on 2015/12/22 16:56
 */
public class FeedsAdView extends AdView {

    private static final String TAG = "FeedsAdView";
    private View headlineView;
    private View callToActionView;
    private View iconView;
    private View bodyView;
    private View storeView;
    private View starRatingView;
    private View imageView;
    private View linearLayout;
    private  int zoneId=-1;
    private  ActionCallBack actionListener;
    private WindowVisibilityChangedListener visibilityChangedListener;

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        //屏幕展示通知
                if(null!=visibilityChangedListener) {
                    visibilityChangedListener.visibilityHasChanged(visibility,zoneId,actionListener,FeedsAdView.this);
                }
//                LogUtils.i(TAG, "view visible");
//                if(zoneId!=-1) {
//                    (new FeedsAdHelplerImpl()).notifyImpression(zoneId);
//                }else {
//                    LogUtils.i(TAG,"has not build");
//                }
        super.onWindowVisibilityChanged(visibility);
    }

   public interface WindowVisibilityChangedListener {
        void visibilityHasChanged(int visibility, int zoneId, ActionCallBack actionListener, FeedsAdView feedsAdView);
    }
    public void setWindowVisibilityChangedListener(WindowVisibilityChangedListener listener) {
        visibilityChangedListener=listener;
    }
    public FeedsAdView(Context context) {
        super(context);
//        setOnclick();
    }

    public FeedsAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        setOnclick();
    }

//    private void setOnclick() {
//        if(zoneId==-1) {
//            LogUtils.i(TAG,"has not build");
//            return;
//        }
//        (new FeedsAdHelplerImpl()).setOnclickLisenter(this, CurrentZonId);
//    }

    public FeedsAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setOnclick();
    }

//    public FeedsAdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
////        setOnclick();
//    }

    public final void setHeadlineView(View view) {
        this.headlineView = view;
    }
    public final void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }
    public final void setActionListener(ActionCallBack callBack) {
    	this.actionListener = callBack;
    }

    public final void setLinearLayout(View view) {
        this.linearLayout = view;
    }

    public final void setCallToActionView(View view) {
        this.callToActionView = view;
    }

    public final void setIconView(View view) {
        this.iconView = view;
    }

    public final void setBodyView(View view) {
        this.bodyView = view;
    }

    public final void setStoreView(View view) {
        this.storeView = view;
    }


    public final void setImageView(View view) {
        this.imageView = view;
    }

    public final void setStarRatingView(View view) {
        this.starRatingView = view;
    }

    public final View getHeadlineView() {
        return headlineView;
    }

    public final View getCallToActionView() {
        return callToActionView;
    }

    public final View getIconView() {
        return iconView;
    }

    public final View getBodyView() {
        return bodyView;
    }

    public final View getStoreView() {
        return storeView;
    }

    public final View getImageView() {
        return imageView;
    }

    public final View getStarRatingView() {
        return starRatingView;
    }
}
