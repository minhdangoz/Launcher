package com.klauncher.kinflow.cards.manager;

import android.content.Context;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.common.utils.CommonShareData;

/**
 * Created by xixionghui on 16/4/12.
 * 此类用于生成Card内容管理器,这里会有所有的Card的类型
 * 以后Card内容类别的增删只需要修改这里即可.
 */
public class CardContentManagerFactory {


    public static BaseCardContentManager createCardContentManager (Context context,int cardSecondTypeId) {
        BaseCardContentManager cardContentManager = null;
        switch (cardSecondTypeId) {
            case CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN:
            case CardIdMap.CARD_TYPE_NEWS_YD_REDIAN:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHEHUI:
            case CardIdMap.CARD_TYPE_NEWS_YD_YULE:
            case CardIdMap.CARD_TYPE_NEWS_YD_CAIJING:
            case CardIdMap.CARD_TYPE_NEWS_YD_TIYU:
            case CardIdMap.CARD_TYPE_NEWS_YD_KEJI:
            case CardIdMap.CARD_TYPE_NEWS_YD_JUNSHI:
            case CardIdMap.CARD_TYPE_NEWS_YD_MINSHENG:
            case CardIdMap.CARD_TYPE_NEWS_YD_MEINV:
            case CardIdMap.CARD_TYPE_NEWS_YD_DUANZI:
            case CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHISHANG:
            case CardIdMap.CARD_TYPE_NEWS_YD_QICHE:
            case CardIdMap.CARD_TYPE_NEWS_YD_GAOXIAO:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHIPIN:
            case CardIdMap.CARD_TYPE_NEWS_YD_DIANYING:
            case CardIdMap.CARD_TYPE_NEWS_YD_JIANSHEN:
            case CardIdMap.CARD_TYPE_NEWS_YD_LVYOU:
                cardContentManager = new YDCardContentManager(context);
                break;
            case CardIdMap.CARD_TYPE_NEWS_TT_REDIAN://头条
                cardContentManager = new TTCardContentManager(context);
                break;
            case CardIdMap.ADVERTISEMENT_YOKMOB:
                cardContentManager = new YMCardContentManager(context);
                break;
            case CardIdMap.ADVERTISEMENT_ADVIEW:
                cardContentManager = new ADVCardContentManager(context);
                break;
            case CardIdMap.ADVERTISEMENT_BAIDU:
                break;
        }
        return cardContentManager;
    }

    //统一管理清空offset
    public static final String KEY_CARD_OFFSET_ADVIEW = "offset_adview";
    public static final String KEY_CARD_OFFSET_BAIDU = "offset_baidu";
    public static final String KEY_CARD_OFFSET_YOKMOB = "offset_yokmob";
    public static final String KEY_CARD_OFFSET_TOUTIAO_REDIAN = "offset_toutiao_redian";
    public static final String KEY_CARD_OFFSET_YIDIAN_JINGXUAN = "offset_yidian_jingxuan";
    public static final String KEY_CARD_OFFSET_YIDIAN_REDIAN = "offset_yidian_redian";
    public static final String KEY_CARD_OFFSET_YIDIAN_YULE = "offset_yidian_yule";
    public static final String KEY_CARD_OFFSET_YIDIAN_JIANKANG = "offset_yidian_jikang";
    public static final String KEY_CARD_OFFSET_YIDIAN_QICHE = "offset_yidian_qiche";
    public static final String KEY_CARD_OFFSET_YIDIAN_LVYOU = "offset_yidian_lvyou";

    public static final String OFFSET_NAME = "offset_";
    public static void clearAllOffset(){
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.ADVERTISEMENT_ADVIEW));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.ADVERTISEMENT_BAIDU));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.ADVERTISEMENT_YOKMOB));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_REDIAN));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_YULE));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_QICHE));
        CommonShareData.clearInt(OFFSET_NAME+String.valueOf(CardIdMap.CARD_TYPE_NEWS_YD_LVYOU));
    }

}
