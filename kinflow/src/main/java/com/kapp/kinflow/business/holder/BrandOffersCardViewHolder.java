package com.kapp.kinflow.business.holder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.BrandOffersCardBean;
import com.kapp.kinflow.business.constant.Constant;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;
import com.kapp.knews.common.browser.KinflowBrower;

import java.util.List;

import butterknife.BindView;

/**
 * description：品牌优惠卡片
 * <br>author：caowugao
 * <br>time： 2017/05/22 15:14
 */
public class BrandOffersCardViewHolder extends BaseCustomViewHolder {
    @BindView(R2.id.iv_one)
    ImageView ivOne;
    @BindView(R2.id.iv_two)
    ImageView ivTwo;
    @BindView(R2.id.iv_three)
    ImageView ivThree;
    @BindView(R2.id.iv_four)
    ImageView ivFour;
    @BindView(R2.id.iv_five)
    ImageView ivFive;
    private TextView tvMore;

    public BrandOffersCardViewHolder(View itemView) {
        super(itemView, BrandOffersCardBean.TYPE_BRAND_OFFERS);
    }

    @Override
    protected String getTittle(Resources resources) {
        return resources.getString(R.string.brand_offers);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvMore = (TextView) buttomLeftView.findViewById(R.id.textview1);
        TextView textview2 = (TextView) buttomLeftView.findViewById(R.id.textview2);
        TextView textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);

        textview2.setVisibility(View.GONE);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvMore.setText(resources.getString(R.string.more_brand_offers));
        tvMore.setOnClickListener(this);

        return buttomLeftView;
    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        View mainContentView = inflater.inflate(R.layout.item_brand_offers_card_content, containerMain, false);
        return mainContentView;
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int viewType) {
        BaseItemBean baseItemBean = beanList.get(position);
        if (null != baseItemBean && baseItemBean instanceof BrandOffersCardBean) {
            BrandOffersCardBean cardBean = (BrandOffersCardBean) baseItemBean;
            List<BrandOffersCardBean.SingleBrandOffersBean> brandOffersBeanList = cardBean.brandOffersBeanList;
            int size = brandOffersBeanList.size();
            for (int i = 0; i < size; i++) {
                BrandOffersCardBean.SingleBrandOffersBean bean = brandOffersBeanList.get(i);
                if (0 == i) {
                    GlideDisplay.getInstance().display(itemView.getContext(), ivOne, bean.imageUrl);
                    ivOne.setOnClickListener(new OnBrandClickListener(bean.clickUrl));
                } else if (1 == i) {
                    GlideDisplay.getInstance().display(itemView.getContext(), ivTwo, bean.imageUrl);
                    ivTwo.setOnClickListener(new OnBrandClickListener(bean.clickUrl));
                } else if (2 == i) {
                    GlideDisplay.getInstance().display(itemView.getContext(), ivThree, bean.imageUrl);
                    ivThree.setOnClickListener(new OnBrandClickListener(bean.clickUrl));
                } else if (3 == i) {
                    GlideDisplay.getInstance().display(itemView.getContext(), ivFour, bean.imageUrl);
                    ivFour.setOnClickListener(new OnBrandClickListener(bean.clickUrl));
                } else if (4 == i) {
                    GlideDisplay.getInstance().display(itemView.getContext(), ivFive, bean.imageUrl);
                    ivFive.setOnClickListener(new OnBrandClickListener(bean.clickUrl));
                } else {
                    break;
                }
            }
        }
    }

    private static class OnBrandClickListener implements View.OnClickListener {
        private String clickUrl;

        public OnBrandClickListener(String clickUrl) {
            this.clickUrl = clickUrl;
        }

        @Override
        public void onClick(View view) {
            KinflowBrower.openUrl(view.getContext(), clickUrl);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview1) {//更多精品
            doMore();
        }
//        switch (view.getId()) {
//            case R2.id.textview1://更多精品
//                doMore();
//                break;
//        }
    }

    private void doMore() {
        KinflowBrower.openUrl(itemView.getContext(), Constant.BAND_OFFERS_MORE_URL);
    }
}
