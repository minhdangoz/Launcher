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
import com.kapp.kinflow.business.activity.MoreSiteActivity;
import com.kapp.kinflow.business.adapter.SiteNaviItemFactory;
import com.kapp.kinflow.business.beans.SiteNavigationCardBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.common.browser.KinflowBrower;

import java.util.List;


/**
 * description：网址导航卡片
 * <br>author：caowugao
 * <br>time： 2017/04/27 11:42
 */

public class SiteNavigationCardViewHolder extends BaseCustomViewHolder {
    private TextView tvChangeCotent;
    private TextView tvMoreSite;
    private TextView textview3;
    private RecyclerView recyclerview;

    public SiteNavigationCardViewHolder(View itemView) {
        super(itemView, SiteNavigationCardBean.TYPE_SITE_NAVI);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.card_site_navi);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvChangeCotent = (TextView) buttomLeftView.findViewById(R.id.textview1);
        tvMoreSite = (TextView) buttomLeftView.findViewById(R.id.textview2);
        textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvChangeCotent.setText(resources.getString(R.string.change_content));
        tvMoreSite.setText(resources.getString(R.string.more_site_navi));

        tvChangeCotent.setOnClickListener(this);
        tvMoreSite.setOnClickListener(this);

        return buttomLeftView;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        int id = view.getId();
        if (id == R.id.textview1) {//换一换
            doChange();
        } else if (id == R.id.textview2) {//更多
            doMore(view.getContext());
        }

//        switch (view.getId()) {
//            case R2.id.textview1://换一换
//                doChange();
//                break;
//            case R2.id.textview2://更多
//                doMore(view.getContext());
//                break;
//        }
    }

    private void doMore(Context context) {
        MoreSiteActivity.launch(context);
    }

    private void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeSiteNaviCard(this);
        }
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.recyclerview, null);
        recyclerview = (RecyclerView) mainContentView.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(containerMain.getContext(), 4));
        return mainContentView;
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {
        BaseItemBean baseItemBean = beanList.get(position);
        if (null != beanList && baseItemBean instanceof SiteNavigationCardBean) {
            SiteNavigationCardBean cardBean = (SiteNavigationCardBean) baseItemBean;
            updateContent(cardBean);
        }
    }

    public void updateContent(final SiteNavigationCardBean cardBean) {
        if (null != cardBean.naviBeanList && !cardBean.naviBeanList.isEmpty()) {
            IItemFactory factory = new SiteNaviItemFactory();
            RecycleViewCommonAdapter<SiteNavigationCardBean.SingleNaviBean> adapter = new
                    RecycleViewCommonAdapter<>(cardBean.naviBeanList, recyclerview, factory);
            adapter.setOnItemClickListener(new RecycleViewEvent.OnItemClickListener() {
                @Override
                public void onNormalItemClick(View view, int position) {
                    SiteNavigationCardBean.SingleNaviBean singleNaviBean = cardBean.naviBeanList.get(position);
                    KinflowBrower.openUrl(view.getContext(), singleNaviBean.url);
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
}
