package com.klauncher.biddingos.interstitial;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alpsdroid.ads.banner.AdSize;
import com.alpsdroid.ads.interstitial.InterstitialAdHelper;
import com.alpsdroid.ads.interstitial.InterstitialAdListener;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpTask;
import com.klauncher.biddingos.commons.task.TaskCallback;
import com.klauncher.biddingos.commons.task.TaskManager;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.web.BaseWebInterface;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by：lizw on 2016/3/29 10:24
 */
public class InterstitialHelperImpl implements InterstitialAdHelper {
    private static final String TAG = InterstitialHelperImpl.class.getSimpleName();
    FrameLayout frameLayout;
    private static Map<Integer, InterstitialAdListener> InterstitialLiseners = new ConcurrentHashMap();
    private static Map<Integer, InterstitialWebView> InterstitialWebViews = new ConcurrentHashMap();

    /**
     * 广告activity生命周期onCreate调用
     * @param activity
     * @param zoneId 广告位
     */
    @Override
    public void onCreate(final Activity activity, int zoneId) {
        RelativeLayout linearLayout = new RelativeLayout(activity);
        linearLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setBackgroundColor(Color.parseColor("#50000000"));

        frameLayout = new FrameLayout(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        frameLayout.setLayoutParams(params);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);

        LogUtils.i(TAG, "zoneid=" + zoneId);
        if (InterstitialWebViews != null && InterstitialWebViews.containsKey(zoneId)) {
            InterstitialWebView tempView = InterstitialWebViews.get(zoneId);
            tempView.showState = InterstitialWebView.ISSHOWING;
            frameLayout.addView(tempView);
            final ImageView closedImg = getCloseImg(activity,tempView.nDpi);
            frameLayout.addView(closedImg);
            linearLayout.addView(frameLayout);
            activity.setContentView(linearLayout);
            //展示上报
            notifyImpression(tempView);
            if (InterstitialLiseners.containsKey(zoneId)) {
                InterstitialAdListener adListener = InterstitialLiseners.get(zoneId);
                if (null != adListener) {
                    adListener.onAdImpression();
                }
            }
        } else {
            activity.finish();
        }
    }

    private void notifyImpression(final InterstitialWebView ad) {
        if (ad.notifyImpression) {
            LogUtils.d(TAG, "已经通知过有效展示 => " + ad.st);
            return;
        }
        LogUtils.d(TAG, "展示通知 " + ad.st);
        String ts = String.valueOf(System.currentTimeMillis() / 1000);

        HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, ad.logUrl + "&ts=" + ts, AdInfo.getStBody(ts, ad.st, ad.transactionid), new TaskCallback<HttpRequest, HttpResponse>() {
            @Override
            public void onSuccess(HttpRequest input, HttpResponse output) {
                //设置已展示
                ad.notifyImpression=true;
                LogUtils.i(TAG, "notifyImpression Success st: " + ad.st);
            }

            @Override
            public void onFailure(HttpRequest input, Throwable th) {
                LogUtils.w(TAG, "notifyImpression Failed st: " + ad.st);
            }
        });

        TaskManager.getInstance().submitRealTimeTask(httpTask);
    }

    /**
     * 广告activity生命周期onDestroy调用
     * @param zoneId
     */
    @Override
    public void onDestroy(int zoneId) {
        if (InterstitialWebViews.containsKey(zoneId)) {
            InterstitialWebViews.get(zoneId).showState = InterstitialWebView.HASSHOWED;
        }
        if (InterstitialLiseners.containsKey(zoneId)) {
            InterstitialLiseners.get(zoneId).onAdClosed();
        }
    }

    /**
     * 关闭按钮
     *
     * @param activity
     * @return
     */
    @NonNull
    private ImageView getCloseImg(final Activity activity,int dpi) {
        final ImageView closedImg = new ImageView(activity);
        FrameLayout.LayoutParams ivParams = new FrameLayout.LayoutParams(30*dpi/160,30*dpi/160);
        ivParams.gravity = Gravity.TOP | Gravity.RIGHT;
//        ivParams.setMargins(0, 5, 5, 0);
        closedImg.setLayoutParams(ivParams);
        closedImg.setPadding(5*dpi/160,5*dpi/160,5*dpi/160,5*dpi/160);
        closedImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream in = classLoader.getResourceAsStream("assets/delete.png");
        if(in!=null) {
            Bitmap backgroundBitmap = BitmapFactory.decodeStream(in);
            closedImg.setImageBitmap(backgroundBitmap);
        }
        closedImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameLayout.removeAllViews();
                activity.finish();
            }
        });
        return closedImg;
    }

    /**
     * 客户端设置事件监听
     * @param zoneId
     * @param listener
     */
    @Override
    public void setAdListener(int zoneId, InterstitialAdListener listener) {
        InterstitialLiseners.put(zoneId, listener);
    }

    /**
     * 判断广告是否加载
     * @param zoneId
     * @return
     */
    @Override
    public boolean isAdReady(int zoneId) {
        if (InterstitialWebViews.containsKey(zoneId)) {
            return InterstitialWebViews.get(zoneId).isAdload;
        } else {
            return false;
        }
    }

    /**
     * 展示广告
     * @param zoneId
     */
    @Override
    public void show(int zoneId) {
        if (!InterstitialWebViews.containsKey(zoneId)) {
            LogUtils.i(TAG, "has not load");
            return;
        }
        if ((InterstitialWebViews.get(zoneId)).showState == InterstitialWebView.ISSHOWING) {
            LogUtils.i(TAG, "has showing");
            return;
        }
        Intent intent = new Intent(Setting.context, InterstitialActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(InterstitialActivity.INTERSTITIALKEY, zoneId);
        Setting.context.startActivity(intent);
    }

    /**
     * 加载广告
     * @param zoneId
     * @param adSize
     */
    @Override
    public void load(int zoneId, AdSize adSize) {
        if(InterstitialWebViews.containsKey(zoneId)) {
            if ((InterstitialWebViews.get(zoneId)).isLoading) {
                LogUtils.i(TAG, "is loading");
                return;
            }
        }
        InterstitialWebView adview = new InterstitialWebView(Setting.context, adSize);
        if (!adview.init(zoneId, mHandler)) {
            LogUtils.i(TAG, "Interstitial web init failed");
            return;
        }

        InterstitialWebViews.put(zoneId, adview);
    }

    /**
     * 事件回调
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleMsg(msg.what, msg.getData());
        }
    };

    private void handleMsg(int what, Bundle data) {
        int placeMentId = data.getInt("placeMentId");
        switch (what) {
            case BaseWebInterface.MSG_TYPE_REQUEST_FAILED:
                String desc = data.getString("desc");
                if (InterstitialLiseners.containsKey(placeMentId)) {
                    InterstitialAdListener adListener = InterstitialLiseners.get(placeMentId);
                    if (null != adListener) {
                        adListener.onLoadFailed(desc);
                    }
                }
                break;
            case BaseWebInterface.MSG_TYPE_REQUEST_SUCCESS:
                if (InterstitialLiseners.containsKey(placeMentId)) {
                    InterstitialAdListener adListener = InterstitialLiseners.get(placeMentId);
                    if (null != adListener) {
                        adListener.onAdReady();
                    }
                }
                break;
            case BaseWebInterface.MSG_TYPE_CLICK:
                if (InterstitialLiseners.containsKey(placeMentId)) {
                    InterstitialAdListener adListener = InterstitialLiseners.get(placeMentId);
                    if (null != adListener) {
                        adListener.onAdClicked();
                    }
                }
                break;

            case BaseWebInterface.MSG_TYPE_CONVERSION:
                if (InterstitialLiseners.containsKey(placeMentId)) {
                    InterstitialAdListener adListener = InterstitialLiseners.get(placeMentId);
                    if (null != adListener) {
                        adListener.onConversion();
                    }
                }
                break;
            default:
                break;
        }
    }
}
