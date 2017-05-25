package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.adapter.RealTimeItemFactory;
import com.kapp.kinflow.business.beans.RealTimeCardBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.common.browser.KinflowBrower;

import java.util.List;


/**
 * description：实时热点卡片
 * <br>author：caowugao
 * <br>time： 2017/05/18 16:58
 */

public class RealTimeCardViewHolder extends BaseCustomViewHolder {
    private TextView tvChangeCotent;
    private RecyclerView recyclerview;

    public RealTimeCardViewHolder(View itemView) {
        super(itemView, RealTimeCardBean.TYPE_REAL_TIME);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.real_time_hot_spots);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvChangeCotent = (TextView) buttomLeftView.findViewById(R.id.textview1);
        TextView textview2 = (TextView) buttomLeftView.findViewById(R.id.textview2);
        TextView textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);

        textview2.setVisibility(View.GONE);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvChangeCotent.setText(resources.getString(R.string.change_content));
        tvChangeCotent.setOnClickListener(this);

        return buttomLeftView;
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.recyclerview, null);
        recyclerview = (RecyclerView) mainContentView.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(containerMain.getContext(), 2));
        return mainContentView;
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {
        BaseItemBean baseItemBean = beanList.get(position);
        if (null != beanList && baseItemBean instanceof RealTimeCardBean) {
            RealTimeCardBean cardBean = (RealTimeCardBean) baseItemBean;
            updateContent(cardBean);
        }
    }

    public void updateContent(final RealTimeCardBean cardBean) {
        if (null != cardBean.beanList && !cardBean.beanList.isEmpty()) {
            IItemFactory factory = new RealTimeItemFactory();
            RecycleViewCommonAdapter<RealTimeCardBean.SingeRealTimeBean> adapter = new
                    RecycleViewCommonAdapter<>(cardBean.beanList, recyclerview, factory);
            adapter.setOnItemClickListener(new RecycleViewEvent.OnItemClickListener() {
                @Override
                public void onNormalItemClick(View view, int position) {
                    RealTimeCardBean.SingeRealTimeBean bean = cardBean.beanList.get(position);
                    KinflowBrower.openUrl(view.getContext(), bean.url);
                }

                @Override
                public void onHeaderItemClick(View view, SparseArrayCompat<View> headers, int position) {

                }

                @Override
                public void onFooterItemClick(View view, SparseArrayCompat<View> footers, int position) {

                }
            });
            adapter.commitItemEvent();
            recyclerview.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if(id== R.id.textview1){//换一换
            doChange();
        }
//        switch (view.getId()) {
//            case R2.id.textview1://换一换
//                doChange();
//                break;
//        }
    }

    private void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeRealTimeCard(this);
        }
    }
}
