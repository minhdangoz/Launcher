package com.delong.download;


import android.content.Context;
import android.view.View;

import com.delong.assistance.bean.ServerAppInfo;
import com.delong.download.db.AppsDao;
import com.delong.download.utils.NetWorkUtils;
import com.delong.download.view.CustomDialog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.util.List;

/**
 * Created by zhuruqiao on 2016/12/6.
 */

public class DownloadManager implements DownloadOperator {

    private static volatile DownloadManager instance;


    public Context mContext;


    private FileDownloader fileDownloader;

    public static String savePath = null;


    private DownloadManager(String savePath) {
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.savePath = savePath;
        fileDownloader = FileDownloader.getImpl();
        fileDownloader.enableAvoidDropFrame();
    }

    /**
     *
     * @param savePath 保存路径
     * @param context
     */
    public static void init(String savePath, Context context) {

        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    FileDownloader.init(context);
                    dataPreparation(context);
                    instance = new DownloadManager(savePath);
                }
            }
        } else {
            return;
        }
    }

    /**
     * 准备数据
     * @param context
     */
    private static void dataPreparation(Context context) {
        List<ServerAppInfo> allApps = AppsDao.getAllApps(context);
//        DataOperator.removeInstalledApp(allApps, context);
        DataOperator.calculateProgressAndSetStatus(allApps);
        DataOperator.checkStatus(allApps, context);
        AppPool.getInstance().setAppsStatus(allApps);
    }

    public static DownloadManager getInstance(Context mContext) {
        instance.mContext = mContext;
        return instance;
    }


    @Override
    public void startTask(ServerAppInfo appInfo) {

        if (!NetWorkUtils.isNetworkAvailable(mContext)) {
            showNetworkUnavailableDialog();
            return;
        }
        if (NetWorkUtils.getNetworkType(mContext) != NetWorkUtils.NET_TYPE_WIFI) {
            showDownloadDialog(appInfo);
        } else {

            startTask(createNewTask(appInfo), appInfo);
        }
    }

    private void startTask(BaseDownloadTask newTask, ServerAppInfo appInfo) {
        appInfo.status.set(ServerAppInfo.DOWNLOADING);
        newTask.start();
    }


    @Override
    public void stopTask(ServerAppInfo appInfo) {
        fileDownloader.pause(appInfo.downLoadListener);

    }

    @Override
    public BaseDownloadTask createNewTask(ServerAppInfo appInfo) {

        appInfo.downLoadListener = new DownloadListener(appInfo, mContext);
        appInfo.downloadTask = FileDownloader.getImpl().create(appInfo.downloadUrl)
                .setPath(getFileName(appInfo.appName))
                .setListener(appInfo.downLoadListener);
        return appInfo.downloadTask;
    }


    private void showNetworkUnavailableDialog() {
        final CustomDialog dialog = new CustomDialog(mContext, CustomDialog.NORMAL);
        dialog.setMessage(R.string.msg_network_unavailable);
        dialog.setLiftButtonText(R.string.dialog_btn_cancel);
        dialog.setRightButtonText(R.string.dialog_btn_ok);
        dialog.setOnRightBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetWorkUtils.openNetworkSetting(mContext);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void showDownloadDialog(final ServerAppInfo appInfo) {
        final CustomDialog dialog = new CustomDialog(mContext, CustomDialog.NORMAL);
        dialog.setMessage(R.string.msg_confirm_start_download)
                .setLiftButtonText(R.string.dialog_btn_cancel)
                .setRightButtonText(R.string.dialog_btn_ok)
                .setOnRightBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask(createNewTask(appInfo), appInfo);
                        dialog.cancel();
                    }
                }).setOnLiftBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();


            }
        }).show();
    }


    public static String getFileName(String appName) {
        return savePath + File.separator + appName + ".apk";
    }

}
