package com.klauncher.biddingos.commons.ads;

/**
 * 用于放置恒量
 * Created by：lizw on 2016/1/21 14:48
 */
public class Constants {
    /**
     *事务未发生
     */
    public static final int AFFAIRNOTHAPPEN=0x000000;
    /**
     *事务发生
     */
    public static final int AFFAIRHAPPEN=0x000001;
    /**
     *事务请求失败
     */
    public static final int REQUESTFAILED=0x000002;
    /**
     *事务请求成功
     */
    public static final int REQUESTSUCCEED=0x000003;
    /**
     *事务准备请求
     */
    public static final int REQUESTREADY=0x000004;


    /**
     * 广告事务数据库表字段
     */
    public static final String TRANSACTIONID="transactionid";
    public static final String IMPRESSIONHAPPEN="impressionhappen";
    public static final String IMPRESSIONSTATUS="impressionstatus";
    public static final String IMPRESSIONURL="impressionurl";
    public static final String CLICKHAPPEN="clickhappen";
    public static final String CLICKSTATUS="clickstatus";
    public static final String CLICKURL="clickurl";
    public static final String CONVERSIONHAPPEN="conversionhappen";
    public static final String CONVERSIONSTATUS="conversionstatus";
    public static final String CONVERSIONURL="conversionurl";
    public static final String CONTENT="content";
    public static final String TIMESTAMP="timestamp";
}
