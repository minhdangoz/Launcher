package com.klauncher.kinflow.common.task;

/**
 * Created by xixionghui on 16/9/26.
 */
public enum NetStatus {

//    public static final int CONNECTION_ERROR = -1;//没有连接到服务器：网络错误||服务器维护
//    public static final int SUCCESS = 0;//获取数据成功
//    public static final int PARSE_ERROR = 1;//解析数据失败
//    public static final int OBTAIN_RESULT_NULL = 2;//获取到服务器端的响应数据，但是数据为空
//    public static final int RESPONSE_FAIL = 3;//服务器端有响应，但是出现错误：如404,500-----服务器响应失败，请稍后重


    SUCCESS(0),
    RESPONSE_SUCCESS(1),


    RESPONSE_FAIL(-1),//响应失败
    RESPONSE_RESULT_NULL(-2),//响应结果为空
    RESPONSE_RESULT_FORMAT_ERROR(-3),//响应结果,返回的格式错误
    RESPONSE_RESULT_PARSE_ERROR(-4),//响应结果,返回的格式解析错误

    ;


    private int status;
    private NetStatus(int status) {
        this.status = status;
    }
}
