package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.SingleTextBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：热词搜索的item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/20 11:36
 */

public class HotSearchItemFactory extends BaseItemFactory<SingleTextBean, HotSearchItemFactory.HotSearchHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public HotSearchHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_hot_search);
        return new HotSearchHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(HotSearchHolder holder, List<SingleTextBean> beanList, int position, int
            viewType) {
        SingleTextBean singleTextBean = beanList.get(position);
        if (null != singleTextBean) {
            holder.tvWord.setText(singleTextBean.text);
        }

    }

    static class HotSearchHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.tv_word)
        TextView tvWord;

        public HotSearchHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
