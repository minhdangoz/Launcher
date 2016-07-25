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
        public static final int ADVERTISEMENT_ADVIEW = 101;//不认识
        public static final int ADVERTISEMENT_BAIDU = 102;//不认识
        //新闻分类----今日头条
        //频道名称,推荐(__all__),热点 (news_hot),社会 (news_society),娱乐 (news_entertainment),科技 (news_tech),汽车(news_car), 财经(news_finance),军事 (news_military),体育 (news_sports)
        public static final int CARD_TYPE_NEWS_TT_REDIAN = 301;//热点
        public static final int CARD_TYPE_NEWS_TT_SHEHUI = 302;//社会
        public static final int CARD_TYPE_NEWS_TT_YULE = 303;//娱乐
        public static final int CARD_TYPE_NEWS_TT_CAIJING = 304;//财经
        public static final int CARD_TYPE_NEWS_TT_TIYU = 305;//体育
        public static final int CARD_TYPE_NEWS_TT_KEJI = 306;//科技
        public static final int CARD_TYPE_NEWS_TT_JUNSHI = 307;//军事
        public static final int CARD_TYPE_NEWS_TT_QICHE = 309;//汽车

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

    public static final int CARD_TYPE_SKIP_AMAP_DIANYING = 501;
    public static final int CARD_TYPE_SKIP_AMAP_YULE = 502;

    public static boolean isKnow(int cardSecondTypeId){
        if (cardSecondTypeId==ADVERTISEMENT_YOKMOB
                ||(cardSecondTypeId>=350&&cardSecondTypeId<=368)//一点资讯
                ||(cardSecondTypeId>=301&&cardSecondTypeId<=307)||cardSecondTypeId==309//今日头条
                ||(cardSecondTypeId>=501&&cardSecondTypeId<=502)//amap跳转
                )
            return true;
        return false;
    }

    /**
     * 今日头条定义的Channel也就是category
     */
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_REDIAN = "news_hot";//热点
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_SHEHUI = "news_society";//社会
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_YULE = "news_entertainment";//娱乐
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_KEJI = "news_tech";//科技
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_QICHE = "news_car";//汽车
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_CARJING = "news_finance";//财经
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_JUNSHI = "news_military";//军事
    public static final String JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_TIYU = "news_sports";//体育

    /**
     * 一点资讯定义的ChannelID.也就是key
     * 热点、社会、娱乐、军事、体育、NBA、财经、科技、民生、美女、段子、健康、时尚、汽车、搞笑、视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
     */
        public static final String YIDIAN_CHANNEL_JINGXUAN = "精选";
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
        public static final String YIDIAN_CHANNEL_DIANYING = "电影";
        public static final String YIDIAN_CHANNEL_JIANSHEN = "健身";
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
            case CARD_TYPE_NEWS_YD_JINGXUAN://精选
                channelId = YIDIAN_CHANNEL_JINGXUAN;
                break;
            case CARD_TYPE_NEWS_YD_REDIAN://热点
                channelId = YIDIAN_CHANNEL_REDIAN;
                break;
            case CARD_TYPE_NEWS_YD_SHEHUI://社会
                channelId = YIDIAN_CHANNEL_SHEHUI;
                break;
            case CARD_TYPE_NEWS_YD_YULE://娱乐
                channelId = YIDIAN_CHANNEL_YULE;
                break;
            case CARD_TYPE_NEWS_YD_CAIJING://财经
                channelId = YIDIAN_CHANNEL_CAIJING;
                break;
            case CARD_TYPE_NEWS_YD_TIYU://体育
                channelId = YIDIAN_CHANNEL_TIYU;
                break;
            case CARD_TYPE_NEWS_YD_KEJI://科技
                channelId = YIDIAN_CHANNEL_KEJI;
                break;
            case CARD_TYPE_NEWS_YD_JUNSHI://军事
                channelId = YIDIAN_CHANNEL_JUNSHI;
                break;
//            case CARD_TYPE_NEWS_YD_NBA:
//                channelId = YIDIAN_CHANNEL_NBA;
//                break;

            //科技、民生、美女、段子、健康、时尚、汽车、搞笑、视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产

            case CARD_TYPE_NEWS_YD_MINSHENG://民生
                channelId = YIDIAN_CHANNEL_MINSHENG;
                break;
            case CARD_TYPE_NEWS_YD_MEINV://美女
                channelId = YIDIAN_CHANNEL_MEINV;
                break;
            case CARD_TYPE_NEWS_YD_DUANZI://段子
                channelId = YIDIAN_CHANNEL_DUANZI;
                break;
            case CARD_TYPE_NEWS_YD_JIANKANG://健康
                channelId = YIDIAN_CHANNEL_JIANKANG;
                break;
            case CARD_TYPE_NEWS_YD_SHISHANG://时尚
                channelId = YIDIAN_CHANNEL_SHISHANG;
                break;
            case CARD_TYPE_NEWS_YD_QICHE://汽车
                channelId = YIDIAN_CHANNEL_QICHE;
                break;
            case CARD_TYPE_NEWS_YD_GAOXIAO://搞笑
                channelId = YIDIAN_CHANNEL_GAOXIAO;
                break;
            //视频、游戏、旅游、科学、互联网、趣图、美食、星座、育儿、情感、房产
            case CARD_TYPE_NEWS_YD_SHIPIN://视频
                channelId = YIDIAN_CHANNEL_SHIPIN;
                break;
//            case CARD_TYPE_NEWS_YD_YOUXI:
//                channelId = YIDIAN_CHANNEL_YOUXI;
//                break;
            case CARD_TYPE_NEWS_YD_DIANYING:
                channelId = YIDIAN_CHANNEL_DIANYING;
                break;
            case CARD_TYPE_NEWS_YD_JIANSHEN:
                channelId = YIDIAN_CHANNEL_JIANSHEN;
                break;
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
     * 此方法用于今日头条SDK
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

    /**
     *此方法适用于今日头条API版本,对应JRTTCardContentManager
     * @param cardSecondTypeId
     * @return
     */
    public static String getTouTiaoCategory (int cardSecondTypeId) {
        String category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_REDIAN;
        switch (cardSecondTypeId) {
            case CARD_TYPE_NEWS_TT_REDIAN:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_REDIAN;
                break;
            case CARD_TYPE_NEWS_TT_SHEHUI:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_SHEHUI;
                break;
            case CARD_TYPE_NEWS_TT_YULE:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_YULE;
                break;
            case CARD_TYPE_NEWS_TT_CAIJING:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_CARJING;
                break;
            case CARD_TYPE_NEWS_TT_TIYU:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_TIYU;
                break;
            case CARD_TYPE_NEWS_TT_KEJI:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_KEJI;
                break;
            case CARD_TYPE_NEWS_TT_JUNSHI:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_JUNSHI;
                break;
            case CARD_TYPE_NEWS_TT_QICHE:
                category = JINRITOUTIAO_CUSTOM_ARTICLE_CATEGORY_QICHE;
                break;

        }
        return category;
    }
}
