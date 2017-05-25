package com.kapp.kinflow.business.holder;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kapp.kinflow.IKinflowCardCallback;
import com.kapp.kinflow.R;
import com.kapp.kinflow.business.util.ScreenUnitUtil;
import com.kapp.kinflow.business.util.TextViewUtil;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemType;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import java.util.List;

import butterknife.ButterKnife;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/18 20:10
 */
public abstract class BaseCustomViewHolder extends IItemFactory.BaseViewHolder implements View.OnClickListener {

    protected TextView tvTittle;
    protected FrameLayout containerMain;
    protected LinearLayout containerButtomLeft;
    protected ImageView ivButtomRight;

    protected int viewType = IItemType.TYPE_ILLEGAL;
    protected LinearLayout containerCardOp;
    protected TextView tvCardDelete;
    protected TextView tvCardTop;
    protected TextView tvCardShare;

    public BaseCustomViewHolder(View itemView, int viewType) {
        super(itemView);
        findBaseViews(itemView);
        this.viewType = viewType;
        Resources resources = itemView.getResources();
        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View mainContentView = onCreateMainContentView(itemView, containerMain, inflater);
        View buttomLeftView = onCreateButtomLeftView(itemView, containerButtomLeft, inflater);
        containerMain.addView(mainContentView);
        containerButtomLeft.addView(buttomLeftView);
        tvTittle.setText(getTittle(resources));
        ButterKnife.bind(this, itemView);
    }

    private void findBaseViews(View itemView) {
        tvTittle = (TextView) itemView.findViewById(R.id.tv_tittle);
        containerMain = (FrameLayout) itemView.findViewById(R.id.container_main);
        containerButtomLeft = (LinearLayout) itemView.findViewById(R.id.container_buttom_left);
        ivButtomRight = (ImageView) itemView.findViewById(R.id.iv_buttom_right);
        containerCardOp = (LinearLayout) itemView.findViewById(R.id.container_card_op);
        tvCardDelete = (TextView) itemView.findViewById(R.id.tv_card_delete);
        tvCardTop = (TextView) itemView.findViewById(R.id.tv_card_top);
        tvCardShare = (TextView) itemView.findViewById(R.id.tv_card_share);

        int drawableLeftWidth = ScreenUnitUtil.asDp(20);
        int drawableLeftHeight = ScreenUnitUtil.asDp(20);
        TextViewUtil.setDrawableLeft(tvCardDelete, R.drawable.navi_card_op_del, drawableLeftWidth, drawableLeftHeight);
        TextViewUtil.setDrawableLeft(tvCardTop, R.drawable.navi_card_op_top, drawableLeftWidth, drawableLeftHeight);
        TextViewUtil.setDrawableLeft(tvCardShare, R.drawable.navi_card_op_share, drawableLeftWidth, drawableLeftHeight);

        ivButtomRight.setOnClickListener(this);
        tvCardDelete.setOnClickListener(this);
        tvCardTop.setOnClickListener(this);
        tvCardShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_buttom_right) {
            toggleCardOpViews();
        } else if (id == R.id.tv_card_delete) {

        } else if (id == R.id.tv_card_top) {
            stickItemToTop();
        } else if (id == R.id.tv_card_share) {
            openShareDialog();
        }
//        switch (view.getId()) {
//            case R.id.iv_buttom_right:
//                toggleCardOpViews();
////                showShortToast("弹框");
//                break;
//            case R.id.tv_card_delete:
//                break;
//            case R.id.tv_card_top:
//                stickCardItemToTop();
//                break;
//            case R.id.tv_card_share:
//                openShareDialog();
//                break;
//
//        }
    }

    private void showShortToast(String msg) {
        Toast.makeText(itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void stickItemToTop() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.stickCardItemToTop(getAdapterPosition());
        }
    }

    protected void openShareDialog() {
        Context context = itemView.getContext();
        if (context instanceof IKinflowCardCallback) {
            IKinflowCardCallback callback = (IKinflowCardCallback) context;
            callback.openShareDialog();
        }
    }

    protected void toggleCardOpViews() {
        if (containerCardOp.getVisibility() == View.GONE) {
            Animation show = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.card_manage_show);
            containerCardOp.setVisibility(View.VISIBLE);
            containerCardOp.startAnimation(show);
        } else if (containerCardOp.getVisibility() == View.VISIBLE) {
            Animation hide = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.card_manage_hide);
            hide.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    containerCardOp.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            containerCardOp.startAnimation(hide);

        }
    }


    protected abstract String getTittle(Resources resources);

    protected abstract View onCreateButtomLeftView(View itemView, LinearLayout containerButtomLeft, LayoutInflater
            inflater);

    protected abstract View onCreateMainContentView(View itemView, FrameLayout containerMain, LayoutInflater inflater);

    public static View getBaseItemView(ViewGroup parent) {
        View baseItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_base_custom, parent,
                false);
        return baseItemView;
    }

    public abstract void onBindViewHolder(BaseCustomViewHolder holder, List<BaseItemBean> beanList, int position, int
            viewType);


}
