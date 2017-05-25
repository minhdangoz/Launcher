package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.DongFangCardBean;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.repository.bean.DongFangTouTiao;

import java.util.List;

import butterknife.BindView;

/**
 * description：东方数据的CardViewHolder
 * <br>author：caowugao
 * <br>time： 2017/04/25 18:01
 */

public abstract class DongFangCardViewHolder extends BaseCustomViewHolder {
    @BindView(R2.id.iv_image)
    ImageView ivImage;
    @BindView(R2.id.tv_content_tittle)
    TextView tvContentTittle;
    @BindView(R2.id.tv_content)
    TextView tvContent;
    @BindView(R2.id.tv_sub_content_tittle)
    TextView tvSubContentTittle;
    @BindView(R2.id.tv_third_content_tittle)
    TextView tvThirdContentTittle;

    TextView tvChangeCotent;
    TextView tvMoreNews;
    TextView textview3;
    @BindView(R2.id.container_main_content)
    LinearLayout containerMainContent;

    public DongFangCardViewHolder(View itemView, int viewType) {
        super(itemView, viewType);
    }

    @Override
    protected View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater inflater) {
        View buttomLeftView = inflater.inflate(R.layout.item_custom_buttom_left, null);
        tvChangeCotent = (TextView) buttomLeftView.findViewById(R.id.textview1);
        tvMoreNews = (TextView) buttomLeftView.findViewById(R.id.textview2);
        textview3 = (TextView) buttomLeftView.findViewById(R.id.textview3);
        textview3.setVisibility(View.GONE);

        Resources resources = itemView.getResources();
        tvChangeCotent.setText(resources.getString(R.string.change_content));
        tvMoreNews.setText(getMoreString(resources));

        tvChangeCotent.setOnClickListener(this);
        tvMoreNews.setOnClickListener(this);

        return buttomLeftView;
    }

    protected abstract String getMoreString(Resources resources);

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

    protected void doChange() {

    }

    protected void doMore(Context context) {

    }

    @Override
    protected View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_head_lines_content, null);
    }

    @Override
    public void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int
            viewType) {

        BaseItemBean baseBean = beanList.get(position);
        if (null != baseBean && baseBean instanceof DongFangCardBean) {
            DongFangCardBean card = (DongFangCardBean) baseBean;
//            if (null != card) {
//                tvContentTittle.setText(card.contentTittle);
//                tvContent.setText(card.content);
//                tvSubContentTittle.setText(card.secondTittle);
//                tvThirdContentTittle.setText(card.thirdTittle);
//
//
//                GlideDisplay.getInstance().display(ivImage.getContext(), ivImage, card.imageUrl);
//                containerMainContent.setOnClickListener(new JumpClickListener(card.main));
//                tvSubContentTittle.setOnClickListener(new JumpClickListener(card.second));
//                tvThirdContentTittle.setOnClickListener(new JumpClickListener(card.third));
//
//            }
            updateNewsContent(card);
        }
    }

    public void updateNewsContent(DongFangCardBean cardBean) {
        if (null != cardBean) {
            tvContentTittle.setText(cardBean.contentTittle);
            tvContent.setText(cardBean.content);
            tvSubContentTittle.setText(cardBean.secondTittle);
            tvThirdContentTittle.setText(cardBean.thirdTittle);


            GlideDisplay.getInstance().display(ivImage.getContext(), ivImage, cardBean.imageUrl);
            containerMainContent.setOnClickListener(new JumpClickListener(cardBean.main));
            tvSubContentTittle.setOnClickListener(new JumpClickListener(cardBean.second));
            tvThirdContentTittle.setOnClickListener(new JumpClickListener(cardBean.third));

        }
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
