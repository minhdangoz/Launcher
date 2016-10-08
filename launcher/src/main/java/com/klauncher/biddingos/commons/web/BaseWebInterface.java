package com.klauncher.biddingos.commons.web;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.net.HttpRequest;
import com.klauncher.biddingos.commons.net.HttpResponse;
import com.klauncher.biddingos.commons.net.HttpTask;
import com.klauncher.biddingos.commons.task.TaskCallback;
import com.klauncher.biddingos.commons.task.TaskManager;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.io.File;

public class BaseWebInterface {
    private static final String TAG = "BaseWebInterface";
    public static final int MSG_TYPE_REQUEST_FAILED = 1;//通知加载请求广告失败
    public static final int MSG_TYPE_REQUEST_SUCCESS = 2;//通知加载广告成功
    public static final int MSG_TYPE_CLICK = 3;//通知广告点击事件
    public static final int MSG_TYPE_REFRESH = 4;//刷新广告
    public static final int MSG_TYPE_CONVERSION = 5;//通知广告下载成功
    protected Context mContext = null;
    protected int placeMentId = 0;
    protected Handler mHandler = null;
    protected boolean bIsDownloading = false; //是否下载中
    protected Handler mBosHandler = null;

    /**
     * Instantiate the interface and set the context
     */
    @SuppressLint("HandlerLeak")
    protected BaseWebInterface(Context c, int placeMentId, Handler mBosHandler) {
        this.mContext = c;
        this.placeMentId = placeMentId;
        this.mBosHandler = mBosHandler;
    }

    public void handleMsg(int nType) {
        switch (nType) {
            case MSG_TYPE_REQUEST_FAILED:
                //通知listener
                if (null != mBosHandler) {
                    Message msg = mBosHandler.obtainMessage(MSG_TYPE_REQUEST_FAILED);
                    Bundle data = new Bundle();
                    data.putInt("placeMentId", placeMentId);
                    data.putString("desc", "Ad cannot get the creatives.");
                    msg.setData(data);
                    mBosHandler.sendMessageDelayed(msg, 1);
                }
                break;
            case MSG_TYPE_REQUEST_SUCCESS:
                //通知listener
                if (null != mBosHandler) {
                    Message msg = mBosHandler.obtainMessage(MSG_TYPE_REQUEST_SUCCESS);
                    Bundle data = new Bundle();
                    data.putInt("placeMentId", placeMentId);
                    msg.setData(data);
                    mBosHandler.sendMessageDelayed(msg, 1);
                }
                break;
            case MSG_TYPE_CLICK:
                //通知listener
                if (null != mBosHandler) {
                    Message msg = mBosHandler.obtainMessage(MSG_TYPE_CLICK);
                    Bundle data = new Bundle();
                    data.putInt("placeMentId", placeMentId);
                    msg.setData(data);
                    mBosHandler.sendMessageDelayed(msg, 1);
                }
                break;
            default:
                break;
        }
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @JavascriptInterface
    public void click(String nType, String ckUrl, String tiUrl) {
        LogUtils.i(TAG, "nType=" + nType + " ckurl=" + ckUrl + " tiurl=" + tiUrl);
        if (null == mContext) return;
        if (nType.equals("0")) {
            Log.i(TAG, "跳转页面:" + ckUrl);
            Uri uri = Uri.parse(ckUrl);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(uri);
            mContext.startActivity(intent);
        } else if (nType.equals("1")) {
            //判断是否处于下载中
            if (this.bIsDownloading) {
                Toast.makeText(mContext, "正在下载，请耐心等待...", Toast.LENGTH_LONG).show();
                Log.w(TAG, "ad has been downloading apk file");
                return;
            }

            Log.i(TAG, "下载APK:" + ckUrl);
            Toast.makeText(mContext, "开始下载...", Toast.LENGTH_LONG).show();
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

            String apkUrl = ckUrl;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("Downloading apk...");
            request.setMimeType("application/vnd.android.package-archive");
            final String fileName = String.valueOf(System.currentTimeMillis()) + ".apk";
            Log.i(TAG, "fileName:" + fileName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            final long downloadId = downloadManager.enqueue(request);

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            final String sTiUrl = tiUrl;
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        //unregisterReceiver(receiver);
                        bIsDownloading = false;
                        Log.i(TAG, "Downloaded");
                        String apkPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
                        //判断安装包是否可以安装
                        if (AppUtils.isApkCanInstall(Setting.context, apkPath)) {
                            Message msg = mBosHandler.obtainMessage(MSG_TYPE_CONVERSION);
                            Bundle data = new Bundle();
                            data.putInt("placeMentId", placeMentId);
                            msg.setData(data);
                            mBosHandler.sendMessage(msg);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
                            mContext.startActivity(intent);

                            //通知ti
                            if (null == sTiUrl || "" == sTiUrl) {
                                return;
                            }

                            HttpTask httpTask = new HttpTask(sTiUrl, new TaskCallback<HttpRequest, HttpResponse>() {
                                @Override
                                public void onSuccess(HttpRequest input, HttpResponse output) {
                                    Log.i(TAG, "Notify Ti Successfully:" + sTiUrl);
                                }

                                @Override
                                public void onFailure(HttpRequest input, Throwable th) {
                                    Log.i(TAG, "Notify Ti Failed:" + sTiUrl);
                                }
                            });
                            TaskManager.getInstance().submitRealTimeTask(httpTask);
                        } else {
                            Log.i(TAG, "apk can not install");
                        }

                    }
                }
            };
            mContext.getApplicationContext().registerReceiver(receiver, filter);

            this.bIsDownloading = true;
        }

        //通知点击
        Message msg = mHandler.obtainMessage(MSG_TYPE_CLICK);
        mHandler.sendMessageDelayed(msg, 1);
    }


    public boolean isConnect() {
        if (!checkPermission("android.permission.ACCESS_NETWORK_STATE")) {
            return false;
        }
        try {
            ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            //TODO: handle exception
            Log.v("error", e.toString());
        }
        return false;
    }

    /**
     * 判断设备是否具有某权限
     */
    public boolean checkPermission(String permName) {
        PackageManager pm = mContext.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, mContext.getPackageName());
    }


}