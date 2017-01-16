package com.delong.assistance.bean;


import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.view.View;

import com.delong.download.DataOperator;
import com.delong.download.DownloadListener;
import com.delong.download.db.AppsDao;
import com.delong.download.receiver.SystemBroadcastReceiver;
import com.delong.download.view.CircleProgressView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.liulishuo.filedownloader.BaseDownloadTask;


/**
 * Created by zhuruqiao on 2016/12/2.
 */

public class ServerAppInfo extends AppInfo implements SystemBroadcastReceiver.PackageActiveListener {

    public static final int INSTALLED = 1;//本地已安装

    public static final int FINISHED = 2;//下载完毕

    public static final int DOWNLOADING = 3;//下载中

    public static final int PAUSE = 4;//暂停

    public static final int NON = 5;//网络获取，还未开始下载

    public static final int ADD_ITEM = 6;//添加选项

    @JsonIgnore
    public boolean isEmpty;


    public long fileSize;

    public long currentLength;

    public String crc;

    public String iconUrl;

    public String fileMd5;

    public String downloadUrl;

    public int appId;

    public int appCategoryId;

    public long createTime;

    public int taskId;


    @JsonIgnore
    public ObservableInt status = new ObservableInt(NON);

    @JsonIgnore
    public ObservableInt progress = new ObservableInt(0);

    @JsonIgnore
    public ObservableInt appType = new ObservableInt();

    @JsonIgnore
    public DownloadListener downLoadListener;

    @JsonIgnore
    public BaseDownloadTask downloadTask;

    @JsonIgnore
    public ObservableInt iconDeleteVisible = new ObservableInt(View.INVISIBLE);

    @JsonIgnore
    public ObservableBoolean isSelected = new ObservableBoolean(false);


    public ServerAppInfo() {
        SystemBroadcastReceiver.registerPackageActiveListener(this);
    }


    /**
     * 回调进度显示
     *
     * @param view
     * @param progress
     */
    @BindingAdapter("progress")
    public static void changeProgress(CircleProgressView view, int progress) {
        view.setProgress(progress);
    }

    /**
     * 回调暂停状态
     *
     * @param view
     * @param status
     */
    @BindingAdapter("status")
    public static void pause(CircleProgressView view, int status) {
        if (status == PAUSE) {
            view.setPause();
        } else if (status == FINISHED) {
            view.setInstall();
        } else if (status == DOWNLOADING) {
            view.setDownload();
        }
    }


    /**
     * 进度条与icon的互斥显示
     *
     * @param status
     * @param b
     * @return
     */
    public int getVisible(int status, boolean b) {
        if (status == PAUSE || status == DOWNLOADING || status == FINISHED) {
            if (b) {
                return View.VISIBLE;
            } else {
                return View.INVISIBLE;
            }
        } else {
            if (b) {
                return View.INVISIBLE;
            } else {
                return View.VISIBLE;
            }
        }
    }

    public int getDownloadIconVisisble(int status) {

        if (status == NON) {
            return View.VISIBLE;
        } else {
            return View.INVISIBLE;
        }
    }


    @Override
    public void onPackageInstall(Context ctx, String pkg) {
        if (pkg.equals(pkgName)) {
            status.set(INSTALLED);
            if (AppsDao.getAppStatus(ctx, this) == ServerAppInfo.INSTALLED) {
                return;
            } else {
                if (AppsDao.updateStatus(ctx, this)) {
                }
            }
        }

    }

    @Override
    public void onPackageUninstall(Context ctx, String pkg) {
        if (pkg.equals(pkgName)) {
            if (AppsDao.getAppStatus(ctx, this) == ServerAppInfo.INSTALLED) {
                DataOperator.changeStatus(ctx,this);
            }
        }
    }

    @Override
    public void onPackageReplaced(Context ctx, String pkg) {

    }

    public void unregisterBrocastReceiver() {
        SystemBroadcastReceiver.unregisterPackageActiveListener(this);
    }


    public int getSelected(boolean isSelected) {
        if (isSelected) {
            return View.VISIBLE;
        } else {
            return View.INVISIBLE;
        }
    }
}
