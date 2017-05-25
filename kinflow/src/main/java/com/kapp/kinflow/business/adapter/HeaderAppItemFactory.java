package com.kapp.kinflow.business.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.HeaderAppBean;
import com.kapp.kinflow.business.util.TextViewUtil;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：头部应用列表的item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/19 20:19
 */

public class HeaderAppItemFactory extends BaseItemFactory<HeaderAppBean, HeaderAppItemFactory.HeaderAppHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public HeaderAppHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_header_app);
        return new HeaderAppHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(final HeaderAppHolder holder, List<HeaderAppBean> beanList, int position, int
            viewType) {
        final HeaderAppBean headerAppBean = beanList.get(position);
        if (null != headerAppBean) {
            holder.tvIcon.setText(headerAppBean.name);
            if (null != headerAppBean.icon) {
                TextViewUtil.setDrawableTop(holder.tvIcon, new GlideBitmapDrawable(holder.itemView.getResources(),
                        headerAppBean.icon));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = holder.itemView.getContext();
                    context.startActivity(headerAppBean.startIntent);
                }
            });
        }
    }

    static class HeaderAppHolder extends BaseItemFactory.BaseViewHolder {

        @BindView(R2.id.tv_icon)
        TextView tvIcon;

        public HeaderAppHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}
