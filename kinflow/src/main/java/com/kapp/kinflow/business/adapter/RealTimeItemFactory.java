package com.kapp.kinflow.business.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.business.beans.RealTimeCardBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;


/**
 * description：实时热点item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/05/18 17:27
 */
public class RealTimeItemFactory extends BaseItemFactory<RealTimeCardBean.SingeRealTimeBean, RealTimeItemFactory
        .RealTimeViewHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public RealTimeViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_real_time_card_content);
        return new RealTimeViewHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(RealTimeViewHolder holder, List<RealTimeCardBean.SingeRealTimeBean>
            beanList, int position, int viewType) {
        final RealTimeCardBean.SingeRealTimeBean singeRealTimeBean = beanList.get(position);
        if (null != singeRealTimeBean) {
            holder.tvName.setText(singeRealTimeBean.name);
        }
    }

    static class RealTimeViewHolder extends IItemFactory.BaseViewHolder {
        private TextView tvName;

        public RealTimeViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
