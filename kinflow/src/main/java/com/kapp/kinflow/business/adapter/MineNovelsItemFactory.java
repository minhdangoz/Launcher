package com.kapp.kinflow.business.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.beans.MineNovelsBean;
import com.kapp.kinflow.view.recyclerview.BaseItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * description：我的小说页面item创建工厂
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:49
 */

public class MineNovelsItemFactory extends BaseItemFactory<MineNovelsBean, MineNovelsItemFactory.MineNovelsHolder> {


    @Override
    protected void setItemAppearAnimation(View itemView) {

    }

    @Override
    public MineNovelsHolder onCreateNormalViewHolder(ViewGroup parent, int viewType) {
//        View view = getView(parent, R.layout.item_mine_novels);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mine_novels, parent, false);
        return new MineNovelsHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(final MineNovelsHolder holder, List<MineNovelsBean> beanList, final int position,
                                       int viewType) {
        MineNovelsBean bean = beanList.get(position);
        if (null != bean) {
            GlideDisplay.getInstance().display(holder.itemView.getContext(), holder.ivImage, bean.imageUrl);
            holder.tvTitle.setText(bean.title);
            String finished = getString(holder.itemView.getContext(), R.string.finished) + ";";
            String unfinished = getString(holder.itemView.getContext(), R.string.unfinished) + ";";
            holder.tvStatus.setText(bean.isEnd ? finished : unfinished);
            holder.tvTotal.setText("共" + bean.total + "章");
            holder.ivDelete.setVisibility(bean.isEdite ? View.VISIBLE : View.GONE);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecyclerView.Adapter adapter = getAdapter();
                    if (adapter instanceof RecycleViewCommonAdapter) {
                        RecycleViewCommonAdapter commonAdapter = (RecycleViewCommonAdapter) adapter;
//                        commonAdapter.removeItem(position);
                        commonAdapter.removeItem(holder.getLayoutPosition());
                    }
                }
            });
//            holder.ivDelete.setOnClickListener(new OnDeleteClickListener(holder.getLayoutPosition(), this));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: 2017/5/19 0019 这里跳到阅读h5页面
                }
            });
        }
    }

//    private static class OnDeleteClickListener implements View.OnClickListener {
//        private int position;
//        private RecyclerView.Adapter adapter;
//
//        public OnDeleteClickListener(int position, BaseItemFactory factory) {
//            this.position = position;
//            adapter = factory.getAdapter();
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (adapter instanceof RecycleViewCommonAdapter) {
//                RecycleViewCommonAdapter commonAdapter = (RecycleViewCommonAdapter) adapter;
//                commonAdapter.removeItem(position);
//            }
//        }
//    }

    static class MineNovelsHolder extends IItemFactory.BaseViewHolder {
        @BindView(R2.id.iv_image)
        ImageView ivImage;
        @BindView(R2.id.tv_title)
        TextView tvTitle;
        @BindView(R2.id.tv_status)
        TextView tvStatus;
        @BindView(R2.id.tv_total)
        TextView tvTotal;
        @BindView(R2.id.iv_delete)
        ImageView ivDelete;

        public MineNovelsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
