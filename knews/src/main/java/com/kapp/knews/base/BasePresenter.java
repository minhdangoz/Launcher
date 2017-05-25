package com.kapp.knews.base;

import android.support.annotation.NonNull;

/**
 * 作者:  android001
 * 创建时间:   16/10/26  下午2:42
 * 版本号:
 * 功能描述:
 */
public interface BasePresenter {

    void onCreate();

    void attachView(@NonNull BaseView view); //自身添加

    void onDestroy();
}
