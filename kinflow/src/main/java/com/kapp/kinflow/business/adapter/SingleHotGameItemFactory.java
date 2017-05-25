package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.HotGameCardBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：热门游戏item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/20 19:31
 */
public class SingleHotGameItemFactory extends BaseItemFactory<HotGameCardBean.SingleHotGameBean,
        SingleHotGameItemFactory.SingHotGameHolder> {

    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public SingHotGameHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_single_hot_game_content);
        return new SingHotGameHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(SingHotGameHolder holder, List<HotGameCardBean.SingleHotGameBean>
            beanList, int position, int viewType) {
        HotGameCardBean.SingleHotGameBean singleHotGameBean = beanList.get(position);
        if (null != singleHotGameBean) {
            if (null != singleHotGameBean.imageUrl && !"".equals(singleHotGameBean.imageUrl)) {
                // TODO: 2017/4/21 0021
            }
            holder.tvName.setText(singleHotGameBean.name);
            holder.tvHandler.setText(singleHotGameBean.getStatusDescrption());
        }
    }

    static class SingHotGameHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_icon)
        ImageView ivIcon;
        @BindView(R2.id.tv_name)
        TextView tvName;
        @BindView(R2.id.tv_handler)
        TextView tvHandler;

        public SingHotGameHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
