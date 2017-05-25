package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.activity.NewsListActivity;
import com.kapp.kinflow.business.beans.HappyCardBean;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.helper.content.resource.ValuesHelper;


/**
 * description：开心一刻
 * <br>author：caowugao
 * <br>time： 2017/04/25 19:48
 */

public class HappyCardViewHolder extends DongFangCardViewHolder {
    public HappyCardViewHolder(View itemView) {
        super(itemView, HappyCardBean.TYPE_HAPPY);
    }

    @Override
    protected String getMoreString(Resources resources) {
        return resources.getString(R.string.more_happy);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.card_happy_moment);
    }
    @Override
    protected void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeHappyCard(this);
        }
    }
    @Override
    protected void doMore(Context context) {
        TabInfo tabInfo = newHappyTabInfo();
        NewsListActivity.launch(context, tabInfo, ValuesHelper.getString(R.string.card_happy_moment));
    }

    public static TabInfo newHappyTabInfo() {
        String channelName = ValuesHelper.getString(R.string.happy);
        String tabCategory = ValuesHelper.getString(R.string.happy_category);
        TabInfo tabinfo = new TabInfo(channelName, tabCategory, 0);
        return tabinfo;
    }
}
