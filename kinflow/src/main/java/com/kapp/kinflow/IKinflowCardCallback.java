package com.kapp.kinflow;

import com.kapp.kinflow.business.beans.HeaderAppBean;
import com.kapp.kinflow.business.holder.ConstellationCardViewHolder;
import com.kapp.kinflow.business.holder.DailyMitoCardViewHolder;
import com.kapp.kinflow.business.holder.HappyCardViewHolder;
import com.kapp.kinflow.business.holder.HeadlinesCardViewHolder;
import com.kapp.kinflow.business.holder.HotGameCardViewHolder;
import com.kapp.kinflow.business.holder.RealTimeCardViewHolder;
import com.kapp.kinflow.business.holder.SiteNavigationCardViewHolder;

import java.util.ArrayList;

/**
 * description：新版信息流卡片接口回调
 * <br>author：caowugao
 * <br>time： 2017/05/24 11:53
 */

public interface IKinflowCardCallback {
    /**
     * 换一换星座运势
     *
     * @param holder
     */
    void changeConstellationCard(ConstellationCardViewHolder holder);

    /**
     * 换一换热门游戏
     *
     * @param holder
     */
    void changeGameNewsCard(HotGameCardViewHolder holder);

    /**
     * 换一换开心一刻
     *
     * @param holder
     */
    void changeHappyCard(HappyCardViewHolder holder);

    /**
     * 换一换头条资讯
     *
     * @param holder
     */
    void changeHeaderLineCard(HeadlinesCardViewHolder holder);

    /**
     * 换一换实时热点
     *
     * @param holder
     */
    void changeRealTimeCard(RealTimeCardViewHolder holder);

    /**
     * 换一换网址导航
     *
     * @param holder
     */
    void changeSiteNaviCard(SiteNavigationCardViewHolder holder);

    /**
     * 上一张每日美图
     *
     * @param holder
     */
    void preDailyMitoCard(DailyMitoCardViewHolder holder);

    /**
     * 下一张每日美图
     *
     * @param holder
     */
    void nextDailyMitoCard(DailyMitoCardViewHolder holder);

    /**
     * 打开分享对话框
     */
    void openShareDialog();

    /**
     * 置顶
     *
     * @param position 想要置顶的card的位置
     */
    void stickCardItemToTop(int position);

    /**
     * 每日美图更多图片的链接
     *
     * @return String
     */
    String getDailyMitoMoreUrl();

    /**
     * 推荐应用
     *
     * @return
     */
    ArrayList<HeaderAppBean> getRecommendApps();
}
