package com.kapp.knews.common;

/**
 * Created by xixionghui on 2016/12/1.
 */

public class LoadNewsType {

    /**
     * 下拉刷新
     */
    public static final int TYPE_LOAD_REFRESH = 10;
    public static final int TYPE_LOAD_REFRESH_SUCCESS = 11;
    public static final int TYPE_LOAD_REFRESH_ERROR = 12;



    /**
     * 上拉加载
     */
    public static final int TYPE_LOAD_MORE = 20;
    public static final int TYPE_LOAD_MORE_SUCCESS = 21;
    public static final int TYPE_LOAD_MORE_ERROR = 22;


    /**
     * 首次加载
     */
    public static final int TYPE_FIRST = 30;

}
