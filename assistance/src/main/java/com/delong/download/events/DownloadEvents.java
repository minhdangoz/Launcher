package com.delong.download.events;

import android.content.Context;
import android.widget.Toast;

import com.delong.download.DownloadManager;
import com.delong.download.R;
import com.delong.download.bean.ServerAppInfo;
import com.delong.download.utils.AppUtils;

import java.io.File;

/**
 * Created by zhuruqiao on 2016/12/16.
 */

public class DownloadEvents {

    public static void download(Context ctx, ServerAppInfo info) {

        switch (info.status.get()) {
            case ServerAppInfo.DOWNLOADING://下载中->暂停
                DownloadManager.getInstance(ctx).stopTask(info);
                break;
            case ServerAppInfo.INSTALLED://打开app
                if (AppUtils.isAppInstalled(ctx, info.pkgName)) {
                    AppUtils.openApp(ctx, info.pkgName);
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.tips_app_not_exist), Toast.LENGTH_SHORT).show();
                }
                break;
            case ServerAppInfo.NON://开始下载
                DownloadManager.getInstance(ctx).startTask(info);
                break;
            case ServerAppInfo.PAUSE://继续下载
                DownloadManager.getInstance(ctx).startTask(info);
                break;
            case ServerAppInfo.FINISHED://安装
                File file = new File(DownloadManager.getFileName(info.appName));
                if (file.exists()) {
                    AppUtils.installApk(ctx, DownloadManager.getFileName(info.appName));
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.tips_file_not_exist), Toast.LENGTH_SHORT).show();
                    DownloadManager.getInstance(ctx).startTask(info);
                }
                break;

        }

    }


}
