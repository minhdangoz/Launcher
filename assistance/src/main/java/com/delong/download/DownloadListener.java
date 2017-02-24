package com.delong.download;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.delong.assistance.bean.ServerAppInfo;
import com.delong.download.db.AppsDao;
import com.delong.download.receiver.SystemBroadcastReceiver;
import com.delong.download.utils.NetWorkUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuruqiao on 2016/12/6.
 */

public class DownloadListener extends FileDownloadListener implements SystemBroadcastReceiver.ConnectivityChangeListener {

    public static List<ServerAppInfo> appInfos = new ArrayList<>();

    private ServerAppInfo appInfo;

    private Context mContext;

    public DownloadListener(ServerAppInfo appInfo, Context mContext) {
        this.appInfo = appInfo;
        this.appInfos.add(appInfo);
        this.mContext = mContext;
        SystemBroadcastReceiver.registerConnectivityChangeListener(this);
    }

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        //添加到数据库
        boolean insert = AppsDao.insert(mContext, appInfo);
        appInfo.getActionCallback().onStart(appInfo);

    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        appInfo.currentLength = soFarBytes;
        appInfo.progress.set((int) (soFarBytes * 100L / totalBytes));
        //保存进度
        boolean success = AppsDao.updateCurrentLength(mContext, appInfo);
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        appInfo.progress.set(100);
        appInfo.status.set(ServerAppInfo.FINISHED);
        boolean success = AppsDao.updateStatus(mContext, appInfo);
        if (!success) {
            AppsDao.insert(mContext, appInfo);
        }
        installApk(mContext, DownloadManager.getFileName(appInfo.appName));
        appInfos.remove(appInfo);
        appInfo.getActionCallback().onComplete(appInfo);
    }

    private void installApk(Context context, String filePath) {

        File apkFile = new File(filePath);
        if (!apkFile.exists()) {
            return;
        }
        Uri uri = Uri.fromFile(apkFile);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(i);
    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        appInfo.status.set(ServerAppInfo.PAUSE);
        boolean success = AppsDao.updateStatus(mContext, appInfo);

    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        appInfo.status.set(ServerAppInfo.PAUSE);
        appInfo.getActionCallback().onFail(appInfo);
    }

    @Override
    protected void warn(BaseDownloadTask task) {

    }

    @Override
    public void onConnectivityChange(boolean isNetworkAvailable, int netType) {
        if (netType != NetWorkUtils.NET_TYPE_WIFI) {
            for (ServerAppInfo info : appInfos) {
                info.downloadTask.pause();
                info.status.set(ServerAppInfo.PAUSE);
            }
        }
    }
}
