package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.ManagerCardBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：卡片管理item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/20 15:40
 */

public class ManagerCardItemFactory extends BaseItemFactory<ManagerCardBean, ManagerCardItemFactory.ManagerCardHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public ManagerCardHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_manager_card);
        return new ManagerCardHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(ManagerCardHolder holder, List<ManagerCardBean> beanList, int position,
                                       int viewType) {
        ManagerCardBean managerCardBean = beanList.get(position);
        if (null != managerCardBean) {
            holder.tvContent.setText(managerCardBean.tittle);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    static class ManagerCardHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_delete)
        ImageView ivDelete;
        @BindView(R2.id.tv_content)
        TextView tvContent;

        public ManagerCardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
