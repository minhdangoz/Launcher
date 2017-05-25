package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.activity.NewsListActivity;
import com.kapp.kinflow.business.beans.HeadlineCardBean;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.helper.content.resource.ValuesHelper;


/**
 * description：头条资讯
 * <br>author：caowugao
 * <br>time： 2017/04/19 16:04
 */
public class HeadlinesCardViewHolder extends DongFangCardViewHolder {


    public HeadlinesCardViewHolder(View itemView) {
        super(itemView, HeadlineCardBean.TYPE_HEADER_LINE);
    }

    @Override
    protected String getMoreString(Resources resources) {
        return resources.getString(R.string.more_news);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.card_headlines);
    }

    @Override
    protected void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeHeaderLineCard(this);
        }
    }

    @Override
    protected void doMore(Context context) {
        TabInfo tabInfo = newHeadlineTabInfo();
        NewsListActivity.launch(context, tabInfo, ValuesHelper.getString(R.string.card_headlines));
    }

    public static TabInfo newHeadlineTabInfo() {
        String channelName = ValuesHelper.getString(R.string.toutiao);
        String tabCategory = ValuesHelper.getString(R.string.toutiao_category);
        TabInfo tabinfo = new TabInfo(channelName, tabCategory, 0);
        return tabinfo;
    }
}
