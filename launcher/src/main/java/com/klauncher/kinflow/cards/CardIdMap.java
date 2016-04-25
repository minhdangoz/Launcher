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
        public static final int CARD_TYPE_NEWS_YD_MEINV = 359;
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
     * 热点、社会、娱乐、军事、体育、NBA、财经、科技、民生、美女、段子、健康、时尚、汽车、搞笑、视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
     */
        public static final String YIDIAN_CHANNEL_REDIAN = "热点";
        public static final String YIDIAN_CHANNEL_SHEHUI = "社会";
        public static final String YIDIAN_CHANNEL_YULE = "娱乐";
        public static final String YIDIAN_CHANNEL_JUNSHI = "军事";
        public static final String YIDIAN_CHANNEL_TIYU = "体育";
        public static final String YIDIAN_CHANNEL_NBA = "NBA";
        public static final String YIDIAN_CHANNEL_CAIJING = "财经";
        public static final String YIDIAN_CHANNEL_KEJI = "科技";
        public static final String YIDIAN_CHANNEL_MINSHENG = "民生";
        public static final String YIDIAN_CHANNEL_MEINV = "美女";
        public static final String YIDIAN_CHANNEL_DUANZI = "段子";
        public static final String YIDIAN_CHANNEL_JIANKANG = "健康";
        public static final String YIDIAN_CHANNEL_SHISHANG = "时尚";
        public static final String YIDIAN_CHANNEL_QICHE = "汽车";
        public static final String YIDIAN_CHANNEL_GAOXIAO = "搞笑";
        public static final String YIDIAN_CHANNEL_SHIPIN = "视频";
        public static final String YIDIAN_CHANNEL_YOUXI = "游戏";
        public static final String YIDIAN_CHANNEL_LVYOU = "旅游";
        public static final String YIDIAN_CHANNEL_KEXUE = "科学";
        public static final String YIDIAN_CHANNEL_HULIANWANG = "互联网";
        public static final String YIDIAN_CHANNEL_QUTU = "趣图";
        public static final String YIDIAN_CHANNEL_MEISHI = "美食";
        public static final String YIDIAN_CHANNEL_XINGZUO = "星座";
        public static final String YIDIAN_CHANNEL_YUER = "育儿";
        public static final String YIDIAN_CHANNEL_QINGGAN = "情感";
        public static final String YIDIAN_CHANNEL_FANGCHAN = "房产";

    /**
     * 通过我们自己定义的key,获取指定的一点咨询的channelID
     * 热点、社会、娱乐、军事、体育、NBA、财经、科技、民生、美女、段子、健康、时尚、汽车、搞笑、视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
     * @param cardSecondTypeId
     * @return
     */
    public static String getYiDianChannelId(int cardSecondTypeId) {
        String channelId = YIDIAN_CHANNEL_REDIAN;
        switch (cardSecondTypeId) {

            case CARD_TYPE_NEWS_YD_REDIAN:
                channelId = YIDIAN_CHANNEL_REDIAN;
                break;
            case CARD_TYPE_NEWS_YD_SHEHUI:
                channelId = YIDIAN_CHANNEL_SHEHUI;
                break;
            case CARD_TYPE_NEWS_YD_YULE:
                channelId = YIDIAN_CHANNEL_YULE;
                break;
            case CARD_TYPE_NEWS_YD_JUNSHI:
                channelId = YIDIAN_CHANNEL_JUNSHI;
                break;
            case CARD_TYPE_NEWS_YD_TIYU:
                channelId = YIDIAN_CHANNEL_TIYU;
                break;
//            case CARD_TYPE_NEWS_YD_NBA:
//                channelId = YIDIAN_CHANNEL_NBA;
//                break;
            case CARD_TYPE_NEWS_YD_CAIJING:
                channelId = YIDIAN_CHANNEL_CAIJING;
                break;
            //科技、民生、美女、段子、健康、时尚、汽车、搞笑、视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
            case CARD_TYPE_NEWS_YD_KEJI:
                channelId = YIDIAN_CHANNEL_KEJI;
                break;
            case CARD_TYPE_NEWS_YD_MINSHENG:
                channelId = YIDIAN_CHANNEL_MINSHENG;
                break;
            case CARD_TYPE_NEWS_YD_MEINV:
                channelId = YIDIAN_CHANNEL_MEINV;
                break;
            case CARD_TYPE_NEWS_YD_DUANZI:
                channelId = YIDIAN_CHANNEL_DUANZI;
                break;
            case CARD_TYPE_NEWS_YD_JIANKANG:
                channelId = YIDIAN_CHANNEL_JIANKANG;
                break;
            case CARD_TYPE_NEWS_YD_SHISHANG:
                channelId = YIDIAN_CHANNEL_SHISHANG;
                break;
            case CARD_TYPE_NEWS_YD_QICHE:
                channelId = YIDIAN_CHANNEL_QICHE;
                break;
            case CARD_TYPE_NEWS_YD_GAOXIAO:
                channelId = YIDIAN_CHANNEL_GAOXIAO;
                break;
            //视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
            case CARD_TYPE_NEWS_YD_SHIPIN:
                channelId = YIDIAN_CHANNEL_SHIPIN;
                break;
//            case CARD_TYPE_NEWS_YD_YOUXI:
//                channelId = YIDIAN_CHANNEL_YOUXI;
//                break;
            case CARD_TYPE_NEWS_YD_LVYOU:
                channelId = YIDIAN_CHANNEL_LVYOU;
                break;
//            case CARD_TYPE_NEWS_YD_KEXUE:
//                channelId = YIDIAN_CHANNEL_KEXUE;
//                break;
//            case CARD_TYPE_NEWS_YD_HULIANWANG:
//                channelId = YIDIAN_CHANNEL_HULIANWANG;
//                break;
//            case CARD_TYPE_NEWS_YD_QUTU:
//                channelId = YIDIAN_CHANNEL_QUTU;
//                break;
//            case CARD_TYPE_NEWS_YD_MEISHI:
//                channelId = YIDIAN_CHANNEL_MEISHI;
//                break;
//            case CARD_TYPE_NEWS_YD_XINGZUO:
//                channelId = YIDIAN_CHANNEL_XINGZUO;
//                break;
//            case CARD_TYPE_NEWS_YD_YUER:
//                channelId = YIDIAN_CHANNEL_YUER;
//                break;
//            case CARD_TYPE_NEWS_YD_QINGGAN:
//                channelId = YIDIAN_CHANNEL_QINGGAN;
//                break;
//            case CARD_TYPE_NEWS_YD_FANGCHAN:
//                channelId = YIDIAN_CHANNEL_FANGCHAN;
//                break;

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
