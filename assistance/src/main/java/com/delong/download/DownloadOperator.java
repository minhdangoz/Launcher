package com.delong.download;

import com.delong.download.bean.ServerAppInfo;
import com.liulishuo.filedownloader.BaseDownloadTask;

/**
 * Created by zhuruqiao on 2016/12/6.
 */

public interface DownloadOperator {

    void startTask(ServerAppInfo appInfo);

    void stopTask(ServerAppInfo appInfo);

    BaseDownloadTask createNewTask(ServerAppInfo appInfo);

}
