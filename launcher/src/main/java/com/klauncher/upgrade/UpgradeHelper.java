package com.klauncher.upgrade;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.delong.assistance.AssistanceServiceApi;
import com.delong.assistance.bean.UpgradeInfo;
import com.delong.assistance.config.HttpAction;
import com.delong.assistance.config.RxFactory;
import com.klauncher.launcher.BuildConfig;
import com.klauncher.launcher.R;
import com.klauncher.launcher.databinding.UpgradeTipsBinding;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */

public class UpgradeHelper {

    private static UpgradeHelper mInstance;

    public static final String ACTION_UPGRADE = "com.klauncher.action.ACTION_UPGRADE";

    private final int REQUEST_CODE = 1024;

    private AlarmManager mAlarmManager;

    private Context mContext;

    private BaseDownloadTask mDownloadTask;

    private PendingIntent mPendingIntent;

    private boolean isRunning = false;

    private class MyFileDownloadListener extends FileDownloadListener {

        UpgradeInfo mUpgradeInfo;

        MyFileDownloadListener(UpgradeInfo ui) {
            mUpgradeInfo = ui;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "pending");
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "progress: soFarBytes " + soFarBytes + " totalBytes " + totalBytes);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "completed");
            }
            showUpgradeInstallDialog(mUpgradeInfo);
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "paused");
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "error");
            }
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            if (BuildConfig.DEBUG) {
                Log.e("Klauncher upgrade", "warn");
            }
        }
    };


    private UpgradeHelper(Context ctx) {
        mContext = ctx;
    }

    public static UpgradeHelper getInstance(Context context) {

        synchronized (UpgradeHelper.class) {
            if (mInstance == null) {
                mInstance = new UpgradeHelper(context);
            }
        }
        return mInstance;
    }

    public void checkImmediately() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);

            //TODO param map is empty, we shell set param to this map if web service need
            final Map<String, String> commonParam = new HashMap<>();
            AssistanceServiceApi.service().checkUpgrade(commonParam, pi.versionCode, BuildConfig.FLAVOR)
                    .compose(RxFactory.<UpgradeInfo>callerSchedulers())
                    .subscribe(new HttpAction<UpgradeInfo>() {

                        @Override
                        public void onHttpError(retrofit2.Response response) {
                        }

                        @Override
                        public void onHttpSuccess(UpgradeInfo ui) {
                            if (ui == null || ui.upgradeFlag != 1) {
                                return;
                            }

                            if (mDownloadTask != null && mDownloadTask.isRunning()) {
                                if (!TextUtils.equals(mDownloadTask.getUrl(), ui.apkUrl)) {
                                    mDownloadTask.cancel();
                                } else {
                                    return;
                                }
                            }

                            String filePath = Environment.getExternalStorageDirectory() + "/klauncher/download/upgrade_" + ui.newVerCode + ".apk";

                            ui.filePath = filePath;

                            mDownloadTask = FileDownloader.getImpl().create(ui.apkUrl)
                                    .setPath(filePath)
                                    .setListener(new MyFileDownloadListener(ui))
                                    .setWifiRequired(true);

                            mDownloadTask.start();

                        }
                    });

        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public boolean startUpgradeMission() {

        if (isRunning) {
            return false;
        } else {
            isRunning = true;
        }

        Intent intent = new Intent(ACTION_UPGRADE);
        intent.setClass(mContext, UpgradeReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager = (AlarmManager) mContext.getSystemService(Service.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 10000, 3 * 60 * 60 * 1000, mPendingIntent);

        return true;
    }

    private void showUpgradeInstallDialog(final UpgradeInfo ui) {


        UpgradeTipsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.upgrade_tips, null, false);

        final FloatToast toast = FloatToast.getInstatce(mContext, binding.getRoot())
                .setDuration(30000)
                .setAnimations(R.style.ToastAnimation)
                .setShowLocation(0, 0)
                .makeMatchParent();

        binding.versionName.setText("最新版本:" + ui.newVerName);

        binding.apkSize.setText("版本大小:" + autoFormatByteStr(ui.apkSize, true));

        binding.upgradeDetail.setText(ui.explain);

        binding.btnInstall.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.btnInstall.getPaint().setAntiAlias(true);

        binding.btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk(mContext, ui.filePath);
                toast.hide();
            }
        });

        binding.btnCancel.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.btnCancel.getPaint().setAntiAlias(true);

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast.hide();
            }
        });

        binding.btnCancel.setVisibility(ui.focusUpgrade == 1 ? View.GONE : View.VISIBLE);

        toast.show();
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

    public void cancelUpgradeMission() {
        mAlarmManager.cancel(mPendingIntent);
        isRunning = false;
    }

    private String autoFormatByteStr(long size, boolean needUnit) {
        DecimalFormat df = new DecimalFormat("0");
        if (size / (1024 * 1024 * 1024) > 0) {
            double tmpSize = (double) (size) / (double) (1024 * 1024 * 1024);
            return df.format(tmpSize) + (needUnit ? "GB" : "");
        } else if (size / (1024 * 1024) > 0) {
            double tmpSize = (double) (size) / (double) (1024 * 1024);
            return df.format(tmpSize) + (needUnit ? "MB" : "");
        } else if (size / 1024 > 0) {
            double tmpSize = (double) size / (double) 1024;
            return df.format(tmpSize) + (needUnit ? "KB" : "");
        } else
            return df.format((double) size) + (needUnit ? "B" : "");
    }

}
