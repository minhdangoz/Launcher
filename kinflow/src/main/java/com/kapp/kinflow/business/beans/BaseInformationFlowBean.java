package com.kapp.kinflow.business.beans;


import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import static com.kapp.kinflow.view.recyclerview.IItemType.TYPE_NORMAL_MIN;

/**
 * description：基类信息流bean
 * <br>author：caowugao
 * <br>time： 2017/04/20 19:06
 */
public class BaseInformationFlowBean extends BaseItemBean {
    public static final int TYPE_HEADER_LINE = TYPE_NORMAL_MIN + 1;//头条资讯
    public static final int TYPE_HOT_GAME = TYPE_NORMAL_MIN + 2;//热门游戏---已撤
    public static final int TYPE_CONSTELLATION = TYPE_NORMAL_MIN + 3;//星座运势---已撤
    public static final int TYPE_HAPPY = TYPE_NORMAL_MIN + 4;//开心一刻
    public static final int TYPE_SITE_NAVI = TYPE_NORMAL_MIN + 5;//网址导航
    public static final int TYPE_DAILY_MITO = TYPE_NORMAL_MIN + 6;//每日美图
    public static final int TYPE_REAL_TIME = TYPE_NORMAL_MIN + 7;//实时热点
    public static final int TYPE_NOVEL = TYPE_NORMAL_MIN + 8;//小说阅读
    public static final int TYPE_BRAND_OFFERS = TYPE_NORMAL_MIN + 9;//品牌优惠
}
