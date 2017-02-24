package com.klauncher.upgrade;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.klauncher.launcher.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by zhuruqiao on 16/10/24.
 */

public class FloatToast {

    private int animations = -1;

    private Context mContext;

    private int toastWidth;

    private int toastHeight;

    private Toast toast;

    public View contentView;

    private boolean isShow;

    public Handler handler = new Handler();

    private int mDuration;

    public DisplayMetrics metrics;

    private int x;

    private int y;

    private Object mTN;

    private Method show;

    private Method hide;

    private WindowManager mWM;

    private WindowManager.LayoutParams params;

    private boolean isMatchParent;

    public interface OnToastShowListener {

        void onToastShow(View contentView);

        void onToastHide(View contentView);

    }

    public OnToastShowListener onToastShowListener;

    public void setOnToastShowListener(OnToastShowListener onToastShowListener) {
        this.onToastShowListener = onToastShowListener;
    }


    private FloatToast(Context context, View view) {
        mContext = context;
        toast = new Toast(context);
        contentView = view;
        measureToastSize(context, view);
        toast.setView(view);
    }

    private void measureToastSize(Context context, View view) {
        int[] viewSize = getViewSize(view);
        toastWidth = viewSize[0];
        toastHeight = viewSize[1];
        metrics = getMetrics(context);
    }

    public static FloatToast getInstatce(Context mContext, View view) {
        return new FloatToast(mContext, view);
    }

    public FloatToast setAnimations(int animStyles) {
        this.animations = animStyles;
        return this;
    }

    public FloatToast setShowLocation(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (x > metrics.widthPixels - toastWidth) {
            x = metrics.widthPixels - toastWidth;
        }
        if (y < 0) {
            y = 0;
        }
        if (y > metrics.heightPixels - toastHeight - getStatusHeight(mContext)) {
            y = metrics.heightPixels - toastHeight - getStatusHeight(mContext);
        }
        this.x = x;
        this.y = y;
        return this;
    }

    public FloatToast makeMatchParent() {
        this.isMatchParent = true;
        return this;
    }


    public FloatToast setDuration(int millisecond) {
        this.mDuration = millisecond;
        return this;
    }


    public void show() {
        if (isShow) return;
        initTN();
        try {
            show.invoke(mTN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isShow = true;
        if (mDuration > 0) {
            handler.postDelayed(hideRunnable, mDuration);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onToastShowListener != null) {
                    onToastShowListener.onToastShow(contentView);
                }
            }
        }, 100);


    }


    public void hide() {
        if (!isShow) return;
        try {
            hide.invoke(mTN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (onToastShowListener != null) {
            onToastShowListener.onToastHide(contentView);
        }
        isShow = false;
    }


    private void initTN() {
        try {
            Field tnField = toast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            mTN = tnField.get(toast);
            show = mTN.getClass().getMethod("show");
            hide = mTN.getClass().getMethod("hide");
            Field tnParamsField = mTN.getClass().getDeclaredField("mParams");
            tnParamsField.setAccessible(true);
            params = (WindowManager.LayoutParams) tnParamsField.get(mTN);
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.windowAnimations = R.style.ToastAnimation;
            Field tnNextViewField = mTN.getClass().getDeclaredField("mNextView");
            tnNextViewField.setAccessible(true);
            tnNextViewField.set(mTN, toast.getView());
            mWM = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isMatchParent) {
            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        } else {
            toast.setGravity(Gravity.LEFT | Gravity.TOP, x, y);
        }

    }


    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public static int[] getViewSize(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        return new int[]{view.getMeasuredWidth(), view.getMeasuredHeight()};
    }

    public static DisplayMetrics getMetrics(Context mContext) {
        WindowManager mWM = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWM.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }


}
