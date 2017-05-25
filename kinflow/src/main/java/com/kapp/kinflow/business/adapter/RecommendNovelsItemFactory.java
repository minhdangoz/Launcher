package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.RecommendNovelsBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：小说推荐item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:12
 */

public class RecommendNovelsItemFactory extends BaseItemFactory<RecommendNovelsBean, RecommendNovelsItemFactory
        .RecommendNovelsHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public RecommendNovelsHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_recommend_novels);
        return new RecommendNovelsHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(RecommendNovelsHolder holder, List<RecommendNovelsBean> beanList,
                                       int position, int viewType) {
        RecommendNovelsBean bean = beanList.get(position);
        if (null != bean) {
            GlideDisplay.getInstance().display(holder.itemView.getContext(), holder.ivImage, bean.imageUrl);
            holder.tvTitle.setText(bean.title);
            holder.tvAuthor.setText(getString(holder.itemView.getContext(), R.string.author) + ":" + bean.author);
            holder.tvSubTitle.setText(bean.subtitle);
        }
    }

    static class RecommendNovelsHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_image)
        ImageView ivImage;
        @BindView(R2.id.tv_title)
        TextView tvTitle;
        @BindView(R2.id.tv_author)
        TextView tvAuthor;
        @BindView(R2.id.tv_sub_title)
        TextView tvSubTitle;

        public RecommendNovelsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
