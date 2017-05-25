package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.ShareItemBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：分享弹出框item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/05/21 19:01
 */

public class ShareItemFactory extends BaseItemFactory<ShareItemBean, ShareItemFactory.ShareHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public ShareHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_shared_pop);
        return new ShareHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(ShareHolder holder, List<ShareItemBean> list, int position, int viewType) {
        ShareItemBean bean = list.get(position);
        if (null != bean) {
            holder.ivImage.setImageResource(bean.imageResId);
            holder.tvText.setText(bean.text);
        }
    }

    static class ShareHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_image)
        ImageView ivImage;
        @BindView(R2.id.tv_text)
        TextView tvText;

        public ShareHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
