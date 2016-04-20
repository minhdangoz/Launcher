package com.klauncher.kinflow.cards.model;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.common.utils.Const;

/**
 * Created by xixionghui on 2016/3/29.
 */
public class YiDianRequestManager {

    static final int YIDIAN_CHANNEL_jingXuan = 0;
    static final int YIDIAN_CHANNEL_reMen = 1;
    static final int YIDIAN_CHANNEL_yuLe = 2;
    static final int YIDIAN_CHANNEL_qiChe = 3;
    static final int YIDIAN_CHANNEL_jianKang = 4;
    static final int YIDIAN_CHANNEL_lvYout = 5;

    int channelId;
    int offset;//文章偏移量，返回offset到offset+count的文章
    int count;//期望返回文章数量

    int firstRequest_channelId;
    int firstRequest_offset;
    int firstRequest_count;

    boolean isFirstRequest = true;

    public YiDianRequestManager(int channelId, int offset, int count) {
        this.channelId = channelId;
        this.offset = offset;
        this.count = count;

        firstRequest_channelId = channelId;
        firstRequest_offset = offset;
        firstRequest_count = count;


    }

    public YiDianRequestManager() {
    }

    private static YiDianRequestManager defaultManager;

    public static YiDianRequestManager getDefaultManager() {
        if (null == defaultManager)
            defaultManager = new YiDianRequestManager(YIDIAN_CHANNEL_jingXuan, 0, 5);
        return defaultManager;
    }

    public String nextGroupRequestUrl() {
        //http://o.go2yd.com/oapi/sample/channel?channel_id=1&offset=0&count=5
        if (!isFirstRequest)this.offset = this.offset+5;
        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT);
        stringBuilder.append("?channel_id=").append(String.valueOf(this.channelId));//id
        stringBuilder.append("&offset=").append(String.valueOf(this.offset));//偏移量
        stringBuilder.append("&count=").append(String.valueOf(this.count));
        isFirstRequest = false;
        return stringBuilder.toString();
    }

    /*
    public String firstGroupRequestUrl(){
        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT);
        stringBuilder.append("?channel_id=").append(String.valueOf(this.firstRequest_channelId));//id
        stringBuilder.append("&offset=").append(String.valueOf(this.firstRequest_offset));//偏移量
        stringBuilder.append("&count=").append(String.valueOf(this.firstRequest_count));
        return stringBuilder.toString();
    }
    */

}
