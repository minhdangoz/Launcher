package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.business.beans.BrandOffersCardBean;
import com.kapp.kinflow.business.beans.ConstellationCardBean;
import com.kapp.kinflow.business.beans.DailyMitoCardBean;
import com.kapp.kinflow.business.beans.HappyCardBean;
import com.kapp.kinflow.business.beans.HeadlineCardBean;
import com.kapp.kinflow.business.beans.HotGameCardBean;
import com.kapp.kinflow.business.beans.NovelCardBean;
import com.kapp.kinflow.business.beans.RealTimeCardBean;
import com.kapp.kinflow.business.beans.SiteNavigationCardBean;
import com.kapp.kinflow.business.holder.BaseCustomViewHolder;
import com.kapp.kinflow.business.holder.BrandOffersCardViewHolder;
import com.kapp.kinflow.business.holder.ConstellationCardViewHolder;
import com.kapp.kinflow.business.holder.DailyMitoCardViewHolder;
import com.kapp.kinflow.business.holder.HappyCardViewHolder;
import com.kapp.kinflow.business.holder.HeadlinesCardViewHolder;
import com.kapp.kinflow.business.holder.HotGameCardViewHolder;
import com.kapp.kinflow.business.holder.NovelCardViewHolder;
import com.kapp.kinflow.business.holder.RealTimeCardViewHolder;
import com.kapp.kinflow.business.holder.SiteNavigationCardViewHolder;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import java.util.List;


/**
 * description：主页面内容的item创建工厂，这里只进行布局创建，数据绑定由各个holder决定
 * <br>author：caowugao
 * <br>time： 2017/04/19 17:51
 */

public class MainContentItemFactory extends BaseItemFactory<BaseItemBean, BaseCustomViewHolder> {

    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public BaseCustomViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View baseItemView = BaseCustomViewHolder.getBaseItemView(parent);
        if (viewType == HeadlineCardBean.TYPE_HEADER_LINE) {//头条资讯
            return new HeadlinesCardViewHolder(baseItemView);
        } else if (viewType == ConstellationCardBean.TYPE_CONSTELLATION) {//星座运势--已撤
            return new ConstellationCardViewHolder(baseItemView);
        } else if (viewType == HappyCardBean.TYPE_HAPPY) {//开心一刻
            return new HappyCardViewHolder(baseItemView);
        } else if (viewType == HotGameCardBean.TYPE_HOT_GAME) {//热门游戏--已撤
            return new HotGameCardViewHolder(baseItemView);
        } else if (viewType == SiteNavigationCardBean.TYPE_SITE_NAVI) {//网址导航
            return new SiteNavigationCardViewHolder(baseItemView);
        } else if (viewType == DailyMitoCardBean.TYPE_DAILY_MITO) {//每日美图
            return new DailyMitoCardViewHolder(baseItemView);
        } else if (viewType == RealTimeCardBean.TYPE_REAL_TIME) {//实时热点
            return new RealTimeCardViewHolder(baseItemView);
        } else if (viewType == NovelCardBean.TYPE_NOVEL) {//小说阅读
            return new NovelCardViewHolder(baseItemView);
        } else if (viewType == BrandOffersCardBean.TYPE_BRAND_OFFERS) {//品牌优惠
            return new BrandOffersCardViewHolder(baseItemView);
        }
        return null;
    }

    @Override
    public void onBindNormalViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int
            viewType) {
        holder.onBindViewHolder(holder, beanList, position, viewType);
    }
}
