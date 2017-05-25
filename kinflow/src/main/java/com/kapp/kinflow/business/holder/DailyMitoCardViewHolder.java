package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.beans.DailyMitoCardBean;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;
import com.kapp.knews.common.browser.KinflowBrower;

import java.util.List;


/**
 * description：每日美图卡片
 * <br>author：caowugao
 * <br>time： 2017/05/18 11:30
 */

public class DailyMitoCardViewHolder extends BaseCustomViewHolder {
    private ImageView ivImage;
    private TextView tvNext;
    private TextView tvPre;
    private TextView tvMore;

    public DailyMitoCardViewHolder(View itemView) {
        super(itemView, DailyMitoCardBean.TYPE_DAILY_MITO);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.daily_mito);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {

        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvNext = (TextView) buttomLeftView.findViewById(R.id.textview1);
        tvPre = (TextView) buttomLeftView.findViewById(R.id.textview2);
        tvMore = (TextView) buttomLeftView.findViewById(R.id.textview3);

        Resources resources = itemView.getResources();
        tvNext.setText(resources.getString(R.string.next_pic));
        tvPre.setText(resources.getString(R.string.previous_pic));
        tvMore.setText(resources.getString(R.string.more));

        tvNext.setOnClickListener(this);
        tvPre.setOnClickListener(this);
        tvMore.setOnClickListener(this);
        return buttomLeftView;
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.item_daily_mito_card_content, null);
        ivImage = (ImageView) mainContentView.findViewById(R.id.iv_image);
        return mainContentView;
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {
        BaseItemBean baseItemBean = beanList.get(position);
        if (null != baseItemBean && baseItemBean instanceof DailyMitoCardBean) {
            final DailyMitoCardBean dailyMitoCardBean = (DailyMitoCardBean) baseItemBean;
            update(dailyMitoCardBean);
        }
    }

    private void update(final DailyMitoCardBean dailyMitoCardBean) {
        GlideDisplay.getInstance().display(itemView.getContext(), ivImage, dailyMitoCardBean.picUrl);
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KinflowBrower.openUrl(view.getContext(), dailyMitoCardBean.clickUrl);
            }
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview1) {//next
            doNext();
        } else if (id == R.id.textview2) {//pre
            doPre();
        } else if (id == R.id.textview3) {//more
            doMore();
        }
//        switch (view.getId()) {
//            case R2.id.textview1://next
//                doNext();
//                break;
//            case R2.id.textview2://pre
//                doPre();
//                break;
//            case R2.id.textview3://more
//                doMore();
//                break;
//        }
    }

    private void doMore() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            String dailyMitoMoreUrl = callback.getDailyMitoMoreUrl();
            if (null != dailyMitoMoreUrl && !"".equals(dailyMitoMoreUrl)) {
                KinflowBrower.openUrl(context, dailyMitoMoreUrl);
            }

        }
    }

    private void doPre() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.preDailyMitoCard(this);
        }
    }

    private void doNext() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.nextDailyMitoCard(this);
        }
    }

    public void updateContent(DailyMitoCardBean cardBean) {
        update(cardBean);
    }
}
