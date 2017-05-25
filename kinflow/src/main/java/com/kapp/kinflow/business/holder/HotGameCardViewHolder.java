package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.activity.NewsListActivity;
import com.kapp.kinflow.business.adapter.SingleHotGameItemFactory;
import com.kapp.kinflow.business.beans.HotGameCardBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.helper.content.resource.ValuesHelper;
import com.kapp.knews.repository.bean.DongFangTouTiao;

import java.util.List;

import butterknife.BindView;

/**
 * description：热门游戏
 * <br>author：caowugao
 * <br>time： 2017/04/20 17:04
 */

public class HotGameCardViewHolder extends BaseCustomViewHolder {

    @BindView(R2.id.tv_sub_content_tittle)
    TextView tvSubContentTittle;
    @BindView(R2.id.tv_third_content_tittle)
    TextView tvThirdContentTittle;
    private RecyclerView recyclerview;
    private TextView tvChangeCotent;
    private TextView tvMoreGameNews;
    private TextView textview3;

    public HotGameCardViewHolder(View itemView) {
        super(itemView, HotGameCardBean.TYPE_HOT_GAME);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.card_top_games);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvChangeCotent = (TextView) buttomLeftView.findViewById(R.id.textview1);
        tvMoreGameNews = (TextView) buttomLeftView.findViewById(R.id.textview2);
        textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvChangeCotent.setText(resources.getString(R.string.change_content));
        tvMoreGameNews.setText(resources.getString(R.string.more_game_news));

        tvChangeCotent.setOnClickListener(this);
        tvMoreGameNews.setOnClickListener(this);
        return buttomLeftView;
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.item_hot_game_card_content, null);
        recyclerview = (RecyclerView) mainContentView.findViewById(R.id.recyclerview);
//        recyclerview.setLayoutManager(new FullyGridLayoutManager(containerMain.getContext(), 4));
        recyclerview.setLayoutManager(new GridLayoutManager(containerMain.getContext(), 4));
        return mainContentView;
    }

    public static TabInfo newGameNewsTabInfo() {
        String channelName = ValuesHelper.getString(R.string.game);
        String tabCategory = ValuesHelper.getString(R.string.game_category);
        TabInfo tabinfo = new TabInfo(channelName, tabCategory, 0);
        return tabinfo;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if(id==R.id.textview1){//换一换
            doChange();
        }
        else if(id==R.id.textview2){//更多
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

    private void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeGameNewsCard(this);
        }
    }

    private void doMore(Context context) {
        TabInfo tabInfo = newGameNewsTabInfo();
        NewsListActivity.launch(context, tabInfo, ValuesHelper.getString(R.string.card_top_games));
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {

        BaseItemBean baseItemBean = beanList.get(position);
        if (null != baseItemBean && baseItemBean instanceof HotGameCardBean) {
            HotGameCardBean hotGameCardBean = (HotGameCardBean) baseItemBean;
            List<HotGameCardBean.SingleHotGameBean> singleBeanList = hotGameCardBean.gameBeanList;
            if (null != singleBeanList && !singleBeanList.isEmpty()) {
                IItemFactory factory = new SingleHotGameItemFactory();
                RecycleViewCommonAdapter<HotGameCardBean.SingleHotGameBean> adapter = new RecycleViewCommonAdapter<>
                        (singleBeanList, recyclerview, factory);
                recyclerview.setAdapter(adapter);
            }
            tvSubContentTittle.setText(hotGameCardBean.secondTittle);
            tvThirdContentTittle.setText(hotGameCardBean.thirdTittle);
            tvSubContentTittle.setOnClickListener(new JumpClickListener(hotGameCardBean.second));
            tvThirdContentTittle.setOnClickListener(new JumpClickListener(hotGameCardBean.third));
        }
    }

    public void updateNewsContent(HotGameCardBean cardBean) {
        tvSubContentTittle.setText(cardBean.secondTittle);
        tvThirdContentTittle.setText(cardBean.thirdTittle);
        tvSubContentTittle.setOnClickListener(new JumpClickListener(cardBean.second));
        tvThirdContentTittle.setOnClickListener(new JumpClickListener(cardBean.third));
    }

    private class JumpClickListener implements View.OnClickListener {
        private DongFangTouTiao touTiao;

        public JumpClickListener(DongFangTouTiao touTiao) {
            this.touTiao = touTiao;
        }

        @Override
        public void onClick(View view) {
            if (null != touTiao) {
                String openUrl = touTiao.getmNewsBean().getUrl();
                KinflowBrower.openUrl(view.getContext(), openUrl);
            }
        }
    }

}
