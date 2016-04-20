package com.klauncher.kinflow.cards.control;

import android.content.Context;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.utils.Const;

/**
 * Created by xixionghui on 16/4/9.
 * 本类用于管理一点资讯Card的内容.
 */
public class YiDianContentManager {

    private CardInfo mCardInfo;//最好有默认
    private int mChannelId = CardIdMap.YIDIAN_CHANNEL_JIANKANG;
    private final int mCount = 5;//每次请求个数,通常为5不变
    private int mOffSet = 0;
    private boolean isFirst = true;
    public YiDianContentManager(CardInfo cardInfo) {
        this.mCardInfo = cardInfo;
        this.mChannelId = CardIdMap.getYiDianChannelId(this.mCardInfo.getCardSecondTypeId());
    }

    /**
     * 换一换功能
     */
    public void next() {
        if (!isFirst)this.mOffSet = this.mOffSet+5;
    }

    /**
     * 更多xxx新闻
     * 通过{@link #mChannelId}直接跳转到指定网页
     */
    public void moreNews(Context context){

    }

    /**
     * 恢复管理器的默认值
     * mOffset = 0;
     * isFirst = true;
     */
    private void recover () {
        this.mOffSet = 0;
        this.isFirst = true;
    }

    private String getRequestUrl () {
        StringBuilder stringBuilder = new StringBuilder(Const.URL_YI_DIAN_ZI_XUN_HOUT);
        stringBuilder.append("?channel_id=").append(String.valueOf(this.mChannelId));//channelId
        stringBuilder.append("&offset=").append(String.valueOf(this.mOffSet));//偏移量
        stringBuilder.append("&count=").append(String.valueOf(this.mCount));
        return stringBuilder.toString();
    }

    /**
     * 获取第一次请求的url
     * @deprecated 没必要存在,直接使用
     * @see #getNextRequestUrl();
     * @return
     */
    public String getFirstRequestUrl(){
        recover();
        return getRequestUrl();
    }


    public String getNextRequestUrl() {
        if (!isFirst)this.mOffSet = this.mOffSet+5;
        this.isFirst = false;
        return getRequestUrl();
    }
}
