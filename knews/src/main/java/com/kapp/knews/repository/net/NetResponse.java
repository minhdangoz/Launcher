package com.kapp.knews.repository.net;

/**
 * Created by xixionghui on 2016/11/28.
 */

public class NetResponse {

    //通用错误代码
    public static final int CONNECTION_ERROR = -1;//没有连接到服务器：网络错误||服务器维护
    public static final int RESPONSE_FAIL = -2;
    public static final int EXCEPTION_IO = -3;
    public static final int EXCEPTION_JSON = -4;
    public static final int DATA_NULL = -5;
    public static final int RESPONSE_STATE_ERROR = -6;
    public static final int PARSE_ERROR = -7;


    public static final int UNKNOW_EXECTION = -999;


    //通用成功代码
    public static final int SUCCESS = 1;

}
