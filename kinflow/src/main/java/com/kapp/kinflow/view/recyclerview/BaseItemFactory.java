package com.kapp.kinflow.view.recyclerview;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;


/**
 * description：item工厂基类
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public abstract class BaseItemFactory<T extends BaseItemBean, VIEW_HOLDER extends IItemFactory.BaseViewHolder>
        implements IItemFactory<T, VIEW_HOLDER> {

    private int lastPos = -1;
    private RecyclerView.Adapter adapter;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {

    }

    @Override
    public void onViewDetachedFromWindow(VIEW_HOLDER holder) {
        clearItemAnimationWhenDetachedFromWindow(holder);
    }

    /**
     * 获取rootVIew
     *
     * @param parent
     * @param layoutId
     * @return
     */
    protected View getView(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, null, false);
    }

    @Override
    public void bindAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    protected String getString(Context context, int resId) {
        return context.getString(resId);
    }

    private boolean isShowingAnimation(RecyclerView.ViewHolder holder) {
        return holder.itemView.getAnimation() != null && holder.itemView
                .getAnimation().hasStarted();
    }

    /**
     * 绑定头部数据
     *
     * @param holder
     * @param headers
     * @param key
     */
    @Override
    public void onBindHeaderViewHolder(VIEW_HOLDER holder, SparseArrayCompat<View> headers, int key) {

    }

    /**
     * 绑定尾部数据
     *
     * @param holder
     * @param footers
     * @param key
     */
    @Override
    public void onBindFooterViewHolder(VIEW_HOLDER holder, SparseArrayCompat<View> footers, int key) {

    }

    /**
     * 设置item动画
     *
     * @param holder
     * @param position
     */
    @Override
    public void setItemAnimation(VIEW_HOLDER holder, int position) {
        if (position > lastPos) {
            setItemAppearAnimation(holder.itemView);
            lastPos = position;
        }
    }

    /**
     * item出现时的item
     *
     * @param itemView
     */
    protected abstract void setItemAppearAnimation(View itemView);

    /**
     * 清除item动画当Detached的时候
     *
     * @param holder
     */
    public void clearItemAnimationWhenDetachedFromWindow(VIEW_HOLDER holder) {
//        if (null != holder.itemView.getAnimation()) {
//            holder.itemView.clearAnimation();
//        }
        if (isShowingAnimation(holder)) {
            holder.itemView.clearAnimation();
        }
    }
}
