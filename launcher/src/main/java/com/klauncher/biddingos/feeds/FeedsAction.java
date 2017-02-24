package com.klauncher.biddingos.feeds;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alpsdroid.ads.feeds.ActionCallBack;
import com.alpsdroid.ads.feeds.FeedsAdView;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.ads.AdInfo;
import com.klauncher.biddingos.commons.analytics.EventHelper;
import com.klauncher.biddingos.commons.analytics.UserEvent;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpTask;
import com.klauncher.biddingos.commons.task.TaskCallback;
import com.klauncher.biddingos.commons.task.TaskManager;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.BrowserUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.io.File;

/**
 * Created by：lizw on 2015/12/24 11:34
 */
public class FeedsAction {
    private static final String TAG = "FeedsAction";
    private ActionCallBack actionListener;
    private FeedsAdView feedsAdView;

    public FeedsAction() {

    }

    /**
     * 设置事件回调监听
     * @param callBack
     */
    public void setActionListener(ActionCallBack callBack) {
        if(callBack!=null) {
            this.actionListener=callBack;
        }
    }
    /**
     * 用zoneId区分不同的广告点击
     *
     * @param view
     * @param zoneId
     */
    public void setOnClick(final FeedsAdView view, final int zoneId) {
        this.feedsAdView=view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i(TAG, "v on click zoneId=" + zoneId);
                AdInfo adInfo= FeedsAdsPool.feedsAd_Info.get((Integer) zoneId);
                if(null==adInfo) {
                    LogUtils.i(TAG, "v on click adInfo is null zoneId=" + zoneId);
                }else {
                    String payMode=adInfo.getPay_mode();
                    if(!TextUtils.isEmpty(payMode)&&payMode.equals(AdInfo.MODE_CPC)) {
                        //cpc
                        BrowserUtils.startDefaultBrowser(Setting.context, adInfo.getClickUrl());
                        actionListener.onAdClick(zoneId, feedsAdView,payMode);
                        actionListener.onConversion(zoneId,feedsAdView,payMode);
                        //通知点击
//                        notifyClick(adInfo);
                    }else {
                        //TODO 以后可能针对不同计费模式调整
                        actionListener.onAdClick(zoneId, feedsAdView,payMode);
                        download(zoneId,adInfo);
                    }
                }
            }
        });
    }

    /**
     * 通知有效展示
     */
    public static void notifyImpression(int zoneId) {
        AdInfo ad = FeedsAdsPool.feedsAd_Info.get((Integer) (zoneId));
        if (null != ad) {
            if (ad.isNotifyimpression()) {
                LogUtils.d(TAG, "已经通知过有效展示 => " + ad.getPackageName());
                return;
            }
            LogUtils.d(TAG, "展示通知 " + ad.getPackageName());
//            ad.notifyImpression(true);
            ad.notifyImpression();
            //上报展示事件
            EventHelper.postAdsEvent(UserEvent.EVENT_ADS_IMPRESSION, ad.getCreativeid(), String.valueOf((zoneId)));
        }
    }

    /**
     * 通知有效点击
     */
    private void notifyClick(AdInfo ad) {
        if (null != ad) {
            if (ad.isNotifyclick()) {
                LogUtils.d(TAG, "已经通知过有效点击 => " + ad.getPackageName());
            } else {
                LogUtils.d(TAG, "点击通知 " + ad.getPackageName());
                ad.notifyClick(null, -1, false);
                //上报点击事件
                EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_START, ad.getCreativeid(), String.valueOf(ad.getZoneid()));
            }
        }
    }

    /**
     * 下载
     *
     * @param ad
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void download(final int zoneId,final AdInfo ad) {
            //判断是否处于下载中
            if (ad.getbIsDownloading()) {
                Toast.makeText(Setting.context, "正在下载中，请耐心等待...", Toast.LENGTH_LONG).show();
                LogUtils.w(TAG, "ad has been downloading apk file");
                return;
            }
            LogUtils.i(TAG, "下载APK:" + ad.getAppDownloadUrl());
            Toast.makeText(Setting.context, "开始下载...", Toast.LENGTH_LONG).show();
            DownloadManager downloadManager = (DownloadManager) Setting.context.getSystemService(Setting.context.DOWNLOAD_SERVICE);

            String apkUrl = ad.getAppDownloadUrl();
            if (TextUtils.isEmpty(apkUrl) || "null".equals(apkUrl) || (!apkUrl.startsWith("http"))) {
                LogUtils.i(TAG, "url is null apkUrl=" + apkUrl);
//            Toast.makeText(Setting.context,"下载地址有误",Toast.LENGTH_SHORT).show();
                return;
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("Downloading apk...");
            request.setMimeType("application/vnd.android.package-archive");
            final String fileName = String.valueOf(System.currentTimeMillis()) + ".apk";
            LogUtils.i(TAG, "fileName:" + fileName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            final long downloadId = downloadManager.enqueue(request);

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            final String sTiUrl = ad.getTrackUrl();
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        LogUtils.i(TAG, "Downloaded end");
                        //设置不在下载中
                        ad.setbIsDownloading(false);
                        //检测下载文件完整性
                        String apkPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
//                        double downSize = FileUtils.getFileOrFilesSize(apkPath, FileUtils.SIZETYPE_B);
//                        LogUtils.i(TAG, " apksize=" + ad.getAppSize() + " downsize=" + downSize +
//                                "  caninstall=" + AppUtils.isApkCanInstall(Setting.context,apkPath));
//                    if(ad.getAppSize()==downSize) {
                        if (AppUtils.isApkCanInstall(Setting.context, apkPath)) {
                            downloadSucceed(zoneId,apkPath, sTiUrl, ad);
                        } else {
//                        Toast.makeText(Setting.context,"下载失败",Toast.LENGTH_SHORT).show();
                            LogUtils.d(TAG, "down failed");
                        }

                    }
                }
            };
        Setting.context.getApplicationContext().registerReceiver(receiver, filter);
            ad.setbIsDownloading(true);
            //通知点击
            notifyClick(ad);
    }

    private void downloadSucceed(final int zoneId,String apkPath, String sTiUrl, final AdInfo ad) {
        Intent intent;//通知ti
        if (!TextUtils.isEmpty(sTiUrl) && !ad.isNotifyconversion()) {
            String ts = String.valueOf(System.currentTimeMillis() / 1000);
            HttpTask httpTask = new HttpTask(HttpRequest.Method.POST, sTiUrl + "&ts=" + ts, AdInfo.getStBody(ts, ad.getSt(), ad.getTransactionid()), new TaskCallback<HttpRequest, HttpResponse>() {
                @Override
                public void onSuccess(HttpRequest input, HttpResponse output) {
                    ad.setNotifyconversion(true);
                    LogUtils.w(TAG, "notifyConversion success for app: " + ad.getCreativeid());
                }

                @Override
                public void onFailure(HttpRequest input, Throwable th) {
                    LogUtils.w(TAG, "notifyConversion Failed for app: " + ad.getCreativeid());
                }
            });

            TaskManager.getInstance().submitRealTimeTask(httpTask);
            //上报下载完成
            if(null!=actionListener) {
                actionListener.onConversion(zoneId,feedsAdView,ad.getPay_mode());
            }
            EventHelper.postAdsEvent(UserEvent.EVENT_ADS_DOWNLOAD_END, ad.getCreativeid(), String.valueOf(ad.getZoneid()));
        }
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        Setting.context.startActivity(intent);
    }


}
