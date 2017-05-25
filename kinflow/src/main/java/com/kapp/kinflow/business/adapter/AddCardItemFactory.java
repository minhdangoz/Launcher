package com.kapp.kinflow.business.adapter;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.AddCardBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：添加卡片item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/20 14:06
 */

public class AddCardItemFactory extends BaseItemFactory<AddCardBean, AddCardItemFactory.AddCardHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public AddCardHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_add_card);
        return new AddCardHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(final AddCardHolder holder, List<AddCardBean> beanList, int position, int
            viewType) {
        final AddCardBean addCardBean = beanList.get(position);
        if (null != addCardBean) {
            holder.ivImage.setBackgroundResource(addCardBean.imageResId);
            holder.tvTittle.setText(addCardBean.tittle);
            holder.tvSunTittle.setText(addCardBean.subTtittle);
            final Resources resources = holder.itemView.getContext().getResources();
            holder.tvAdd.setText(addCardBean.isShowed ? resources.getString(R.string.added) : resources.getString(R
                    .string.add));
            holder.tvAdd.setBackgroundResource(addCardBean.isShowed ? R.drawable.bg_solid_gray_corner : R.drawable
                    .bg_solid_yellow_corner);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            holder.tvAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addCardBean.isShowed = !addCardBean.isShowed;
                    holder.tvAdd.setBackgroundResource(addCardBean.isShowed ? R.drawable.bg_solid_gray_corner : R
                            .drawable
                            .bg_solid_yellow_corner);
                    holder.tvAdd.setText(addCardBean.isShowed ? resources.getString(R.string.added) : resources.getString(R
                            .string.add));
                }
            });
        }
    }

    static class AddCardHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_image)
        ImageView ivImage;
        @BindView(R2.id.tv_tittle)
        TextView tvTittle;
        @BindView(R2.id.tv_sun_tittle)
        TextView tvSunTittle;
        @BindView(R2.id.tv_add)
        TextView tvAdd;

        public AddCardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
