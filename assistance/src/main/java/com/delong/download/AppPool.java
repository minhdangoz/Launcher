package com.delong.download;

import android.text.TextUtils;


import com.delong.assistance.bean.ServerAppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuruqiao on 2016/12/29.
 * e-mail:563325724@qq.com
 * 主要用于盛放下载任务中的对象，以及服务器返回的对象
 */

public class AppPool {

    private static AppPool instance = new AppPool();

    public final List<ServerAppInfo> appPool = new ArrayList<>();

    private AppPool() {

    }

    public static AppPool getInstance() {

        return instance;

    }


    /**
     * 找出sourceList与appPool中相同包名的对象，并将sourceList中相同的对象用pool中的替换，将pool中不包含的对象填充到pool中
     *
     * @param sourceList
     * @return
     */
    public List<ServerAppInfo> setAppsStatus(List<ServerAppInfo> sourceList) {

        List<ServerAppInfo> tmpList = new ArrayList<>();

        if (sourceList.size() == 0 || null == sourceList) {
            return sourceList;
        }
        //遍历池中数据，找出包名相同的数据
        if (appPool.size() == 0) {
            appPool.addAll(sourceList);

        } else {

            for (ServerAppInfo sourceApp : sourceList) {

                ServerAppInfo tmpApp = null;

                for (ServerAppInfo appInfo : appPool) {
                    if (!TextUtils.isEmpty(appInfo.pkgName) && !TextUtils.isEmpty(sourceApp.pkgName) && appInfo.pkgName.equals(sourceApp.pkgName)) {
                        tmpApp = appInfo;
                        break;
                    }
                }
                if (tmpApp == null) {
                    //说明池中无此应用，添加到池中
                    appPool.add(sourceApp);
                } else {
                    //说明池中有该应用,将改应用添加到临时集合
                    tmpList.add(tmpApp);
                }
            }
            if (tmpList.size() == 0) {
                return sourceList;
            } else {
                //将原集合中某些已存在于池中的对象用池中的对象所取代

                for (ServerAppInfo tmpApp : tmpList) {
                    ServerAppInfo sameApp = null;
                    for (ServerAppInfo appInfo : sourceList) {
                        if (!TextUtils.isEmpty(appInfo.pkgName) && !TextUtils.isEmpty(tmpApp.pkgName) && appInfo.pkgName.equals(tmpApp.pkgName)) {
                            //认为是同一个应用
                            sameApp = appInfo;
                            break;
                        }
                    }
                    if (sameApp != null) {
                        //原列表中包含此应用
                        int index = sourceList.indexOf(sameApp);
                        if (index != -1) {
                            sourceList.set(index, tmpApp);
                        }

                    }

                }

            }
            return sourceList;

        }


        return sourceList;

    }

    /**
     * 获取对象池子
     *
     * @return
     */
    public List<ServerAppInfo> getApps() {
        return appPool;
    }


}
