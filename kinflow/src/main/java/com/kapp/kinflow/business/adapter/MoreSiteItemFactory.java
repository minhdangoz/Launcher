package com.kapp.kinflow.business.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.MoreSiteContentItemBean;
import com.kapp.kinflow.view.DetailRightLayout;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：更多网址下面链接的item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/04/27 20:16
 */

public class MoreSiteItemFactory extends BaseItemFactory<MoreSiteContentItemBean, MoreSiteItemFactory
        .MoreSiteContentHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public MoreSiteContentHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_more_site_content);
        return new MoreSiteContentHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(MoreSiteContentHolder holder, List<MoreSiteContentItemBean>
            beanList, int position, int viewType) {
        final MoreSiteContentItemBean bean = beanList.get(position);
        if (null != bean) {
            if (null != bean.uiData) {

//                int screenH = ScreenUtil.getScreenH(holder.itemView.getContext());
//                int height= ScreenUnitUtil.asDp(180);
                int height= FrameLayout.LayoutParams.WRAP_CONTENT;
                FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,height);
                holder.detailRightLayout.setLayoutParams(layoutParams);

                holder.detailRightLayout.setData(bean.uiData);
                holder.detailRightLayout.setOnDetailRightItemClickListener(new DetailRightLayout
                        .OnDetailRightItemClickListener() {


                    @Override
                    public void onBigItemClick(View view) {

                    }

                    @Override
                    public void onRightItemClick(ViewGroup parent, int position, int itemCount, View view) {
                        List<String> rightClickUrls = bean.rightClickUrls;
                        if (null != rightClickUrls) {
                            showShortToast(view.getContext(), rightClickUrls.get(position));
                        }
                    }

                    @Override
                    public void onExtraItemClick(ViewGroup parent, int position, int itemCount, View view) {
                        List<String> extraClickUrls = bean.extraClickUrls;
                        if (null != extraClickUrls) {
                            showShortToast(view.getContext(), extraClickUrls.get(position));
                        }
                    }
                });
            }
        }
    }

    private void showShortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    static class MoreSiteContentHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.detailRightLayout)
        DetailRightLayout detailRightLayout;

        public MoreSiteContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
