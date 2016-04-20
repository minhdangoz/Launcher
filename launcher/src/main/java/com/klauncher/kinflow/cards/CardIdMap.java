package com.klauncher.kinflow.cards;

/**
 * Created by xixionghui on 2016/3/30.
 * 本类服务于Card.是我们自己维护的一个map表,key是CardInfo.SecondTypeId. value是我们请求的一点咨询等的频道chann的ID
 * 获取到CardInfo后,switch它的SecondTypeId.case选项就是此类的内部类NewsType
 */
public class CardIdMap {

    //三大主类
    public static final int CARD_TYPE_AD = 1;
    public static final int CARD_TYPE_APPS = 2;
    public static final int CARD_TYPE_NEWS = 3;


    /**
     * 我们自己定义的新闻类型,也就是CardInfo.SecondTypeID的选项卡
     */
        //广告类别
        public static final int ADVERTISEMENT_YOKMOB = 100;
        public static final int ADVERTISEMENT_ADVIEW = 101;
        public static final int ADVERTISEMENT_BAIDU = 102;
        //新闻分类----今日头条
        public static final int CARD_TYPE_NEWS_TT_REDIAN = 301;
        public static final int CARD_TYPE_NEWS_TT_SHEHUI = 302;
        //新闻分类----一点资讯
        public static final int CARD_TYPE_NEWS_YD_JINGXUAN = 350;
        public static final int CARD_TYPE_NEWS_YD_REDIAN = 351;
        public static final int CARD_TYPE_NEWS_YD_SHEHUI = 352;
        public static final int CARD_TYPE_NEWS_YD_YULE = 353;
        public static final int CARD_TYPE_NEWS_YD_CAIJING = 354;
        public static final int CARD_TYPE_NEWS_YD_TIYU = 355;
        public static final int CARD_TYPE_NEWS_YD_KEJI = 356;
        public static final int CARD_TYPE_NEWS_YD_JUNSHI = 357;
        public static final int CARD_TYPE_NEWS_YD_MINSHENG = 358;
        public static final int CARD_TYPE_NEWS_YD_MEIMV = 359;
        public static final int CARD_TYPE_NEWS_YD_DUANZI = 360;
        public static final int CARD_TYPE_NEWS_YD_JIANKANG = 361;
        public static final int CARD_TYPE_NEWS_YD_SHISHANG = 362;
        public static final int CARD_TYPE_NEWS_YD_QICHE = 363;
        public static final int CARD_TYPE_NEWS_YD_GAOXIAO = 364;
        public static final int CARD_TYPE_NEWS_YD_SHIPIN = 365;
        public static final int CARD_TYPE_NEWS_YD_DIANYING = 366;
        public static final int CARD_TYPE_NEWS_YD_JIANSHEN = 367;
        public static final int CARD_TYPE_NEWS_YD_LVYOU = 368;

    /**
     * 一点资讯定义的ChannelID.也就是key
     */
        public static final int YIDIAN_CHANNEL_JINGXUAN = 0;
        public static final int YIDIAN_CHANNEL_REMEN = 1;
        public static final int YIDIAN_CHANNEL_YULE = 2;
        public static final int YIDIAN_CHANNEL_QICHE = 3;
        public static final int YIDIAN_CHANNEL_JIANKANG = 4;
        public static final int YIDIAN_CHANNEL_LVYOU = 5;

    /**
     * 通过我们自己定义的key,获取指定的一点咨询的channelID
     * @param cardSecondTypeId
     * @return
     */
    public static int getYiDianChannelId(int cardSecondTypeId) {
        int channelId = YIDIAN_CHANNEL_JINGXUAN;
        switch (cardSecondTypeId) {
            case CARD_TYPE_NEWS_YD_YULE:
                channelId = YIDIAN_CHANNEL_YULE;
                break;
            case CARD_TYPE_NEWS_YD_QICHE:
                channelId = YIDIAN_CHANNEL_QICHE;
                break;
            case CARD_TYPE_NEWS_YD_JIANKANG:
                channelId = YIDIAN_CHANNEL_JIANKANG;
                break;
            case CARD_TYPE_NEWS_YD_LVYOU:
                channelId = YIDIAN_CHANNEL_LVYOU;
                break;
            case CARD_TYPE_NEWS_YD_REDIAN:
                channelId = YIDIAN_CHANNEL_REMEN;
                break;
        }
        return channelId;
    }

    /**
     *此方法用于通过{@link com.klauncher.kinflow.cards.model.CardInfo#cardSecondTypeId}
     *获取头条channel频道的的方法.
     *@version 1.0
     * 当前版本无用.因为今日头条目前版本不能根据chanelId获取新闻
     * 当前版本为将来今日头条给了openAPI做准备.
     * @param cardSecondTypeId
     * @return
     */
    public static int getTouTiaoChannelId (int cardSecondTypeId) {
        int channelId = CARD_TYPE_NEWS_TT_REDIAN;
        switch (cardSecondTypeId) {
            case CARD_TYPE_NEWS_TT_SHEHUI:
                break;
        }
        return channelId;
    }

}
