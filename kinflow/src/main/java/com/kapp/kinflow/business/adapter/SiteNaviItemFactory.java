package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.SiteNavigationCardBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：首页网址导航卡片的item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/27 15:08
 */

public class SiteNaviItemFactory extends BaseItemFactory<SiteNavigationCardBean.SingleNaviBean, SiteNaviItemFactory.SiteNaviHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public SiteNaviHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_site_navi_card_content);
        return new SiteNaviHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(SiteNaviHolder holder, List<SiteNavigationCardBean.SingleNaviBean>
            beanList, int position, int viewType) {
        SiteNavigationCardBean.SingleNaviBean singleNaviBean = beanList.get(position);
        if (null != singleNaviBean) {
            holder.tvSiteName.setText(singleNaviBean.name);
            if (singleNaviBean.isHot) {
                holder.ivHot.setVisibility(View.VISIBLE);
            } else {
                holder.ivHot.setVisibility(View.GONE);
            }
        }
    }

    static class SiteNaviHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.tv_site_name)
        TextView tvSiteName;
        @BindView(R2.id.iv_hot)
        ImageView ivHot;

        public SiteNaviHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
