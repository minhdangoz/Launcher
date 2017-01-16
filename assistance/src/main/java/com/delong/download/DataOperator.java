package com.delong.download;


import android.content.Context;

import com.delong.download.bean.ServerAppInfo;
import com.delong.download.db.AppsDao;
import com.delong.download.utils.AppUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuruqiao on 2016/12/8.
 * <p>
 * 用于改边serverAppInfo的状态，及剔除一些特殊应用来满足列表显示
 */

public class DataOperator {

    /**
     * 为推荐页面准备数据
     *
     * @param sourceList
     */
    public static void prepareDtaForRecommendPage(List<ServerAppInfo> sourceList, Context mContext) {
        removeInstalledApp(sourceList, mContext);
        AppPool.getInstance().setAppsStatus(sourceList);

    }

    /**
     * 为搜索页面准备数据
     *
     * @param sourceList
     */
    public static void covertAndReplace(List<ServerAppInfo> sourceList, Context mContext) {

        //将本地应用状态改成已安装
        changeStatusIfAppIsInstall(sourceList, mContext);
        AppPool.getInstance().setAppsStatus(sourceList);

    }

    /**
     * 为首页推荐列表准备数据
     *
     * @param sourceList
     */

    public static void prepareDtaForFamousRecommend(List<ServerAppInfo> sourceList, Context mContext) {
        removeUnInstallApp(sourceList, mContext);
        AppPool.getInstance().setAppsStatus(sourceList);
    }

    /**
     * 为首页默认列表准备数据
     *
     * @param sourceList
     */

    public static void prepareForAppListFragment(List<ServerAppInfo> sourceList, Context mContext) {
        //首次进来
        removeUnInstallApp(sourceList, mContext);
        calculateProgressAndSetStatus(sourceList);
        AppPool.getInstance().setAppsStatus(sourceList);

    }


    /**
     * 移除未安装应用,并且在数据库中移除，用于界面应用的首次加载
     *
     * @param sourceList
     * @return
     */
    public static List<ServerAppInfo> removeUnInstallApp(List<ServerAppInfo> sourceList, Context mContext) {

        if (sourceList == null || sourceList.size() == 0) {
            return sourceList;
        }

        List<ServerAppInfo> tempList = new ArrayList<>();

        for (ServerAppInfo serverAppInfo : sourceList) {

            if (!AppUtils.isAppInstalled(mContext, serverAppInfo.pkgName) && serverAppInfo.status.get() == ServerAppInfo.INSTALLED) {
                tempList.add(serverAppInfo);
                AppsDao.deleteApp(mContext, serverAppInfo);
            }
        }
        sourceList.removeAll(tempList);

        return sourceList;

    }


    /**
     * 计算下载进度 用于应用首次加载，将下载中或者暂停中的应用进度计算出来，并把状态都改成暂停
     *
     * @param sourceList
     * @return
     */

    public static List<ServerAppInfo> calculateProgressAndSetStatus(List<ServerAppInfo> sourceList) {
        if (sourceList == null || sourceList.size() == 0) {
            return sourceList;
        }
        for (ServerAppInfo serverAppInfo : sourceList) {
            if (serverAppInfo.status.get() == ServerAppInfo.PAUSE || serverAppInfo.status.get() == ServerAppInfo.DOWNLOADING) {
                serverAppInfo.status.set(ServerAppInfo.PAUSE);
                serverAppInfo.progress.set((int) (serverAppInfo.currentLength * 100 / serverAppInfo.fileSize));
            }
        }
        return sourceList;
    }

    /**
     * 将部分已安装却还显示为未安装的应用状态改变成已安装，同时修改数据库状态
     *
     * @param sourceList
     * @return
     */
    public static List<ServerAppInfo> changeStatusIfAppIsInstall(List<ServerAppInfo> sourceList, Context mContext) {

        if (sourceList == null || sourceList.size() == 0) {
            return sourceList;
        }
        for (ServerAppInfo serverAppInfo : sourceList) {
            if (AppUtils.isAppInstalled(mContext, serverAppInfo.pkgName)) {
                if (AppsDao.getAppStatus(mContext, serverAppInfo) != ServerAppInfo.INSTALLED) {
                    serverAppInfo.status.set(ServerAppInfo.INSTALLED);
                    AppsDao.updateStatus(mContext, serverAppInfo);
                }
            }

        }
        return sourceList;

    }

    /**
     * 列表中去除本地安装的应用
     *
     * @param sourceList
     * @return
     */

    public static List<ServerAppInfo> removeInstalledApp(List<ServerAppInfo> sourceList, Context mContext) {

        if (sourceList == null || sourceList.size() == 0) {
            return sourceList;
        }
        List<ServerAppInfo> installedList = new ArrayList<>();
        for (ServerAppInfo serverAppInfo : sourceList) {
            if (AppUtils.isAppInstalled(mContext, serverAppInfo.pkgName)) {
                installedList.add(serverAppInfo);
            }
        }
        sourceList.removeAll(installedList);
        return sourceList;
    }

    public static List<ServerAppInfo> checkStatus(List<ServerAppInfo> sourceList, Context mContext) {
        if (sourceList == null || sourceList.size() == 0) {
            return sourceList;
        }
        for (ServerAppInfo serverAppInfo : sourceList) {
            changeStatus(mContext, serverAppInfo);
        }
        return sourceList;

    }

    public static void changeStatus(Context mContext, ServerAppInfo serverAppInfo) {
        if (!AppUtils.isAppInstalled(mContext, serverAppInfo.pkgName, serverAppInfo.versionCode) && serverAppInfo.status.get() == ServerAppInfo.INSTALLED) {
            File file = new File(DownloadManager.getFileName(serverAppInfo.appName));
            if (file.exists()) {
                serverAppInfo.status.set(ServerAppInfo.FINISHED);
            } else {
                serverAppInfo.status.set(ServerAppInfo.NON);
            }
            AppsDao.updateStatus(mContext, serverAppInfo);

        }
    }


}
