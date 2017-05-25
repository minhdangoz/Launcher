package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.activity.NewsListActivity;
import com.kapp.kinflow.business.beans.ConstellationCardBean;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.helper.content.resource.ValuesHelper;


/**
 * description：星座运势
 * <br>author：caowugao
 * <br>time： 2017/04/25 17:25
 */

public class ConstellationCardViewHolder extends DongFangCardViewHolder {
    public ConstellationCardViewHolder(View itemView) {
        super(itemView, ConstellationCardBean.TYPE_CONSTELLATION);
    }

    @Override
    protected String getMoreString(Resources resources) {
        return resources.getString(R.string.more_constellation);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.constellation_fortune);
    }

    @Override
    protected void doChange() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.changeConstellationCard(this);
        }
    }

    @Override
    protected void doMore(Context context) {
        TabInfo tabInfo = newConstellationTabInfo();
        NewsListActivity.launch(context, tabInfo, ValuesHelper.getString(R.string.constellation_fortune));
    }
    public static TabInfo newConstellationTabInfo() {
        String channelName = ValuesHelper.getString(R.string.constellation);
        String tabCategory = ValuesHelper.getString(R.string.constellation_category);
        TabInfo tabinfo = new TabInfo(channelName, tabCategory, 0);
        return tabinfo;
    }
}
