package com.kapp.kinflow.view.recyclerview.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.R;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemType;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import java.util.List;



/**
 * description：上拉添加更多的apdater
 * <br>author：caowugao
 * <br>time： 2017/04/01 12:10
 */

public class PullUpRecyclerviewAdapter<DATA extends BaseItemBean> extends RecycleViewCommonAdapter {

    private boolean isShowLoadMore = false;

    private int loadMoreResId;

    private static final int VIEW_TYPE_LOAD_MORE = IItemType.TYPE_NORMAL_MAX - 1;

    public interface OnLoadMoreListener {
        void onLoadMore(RecyclerView recyclerView);
    }

    private OnLoadMoreListener listener;

    public void setLoadMoreListener(OnLoadMoreListener listener) {
        this.listener = listener;
    }

    @Override
    public IItemFactory.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (VIEW_TYPE_LOAD_MORE == viewType) {
            View view = getView(parent, loadMoreResId);
            return new LoadMoreHolder(view);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    /**
     * 获取rootVIew
     *
     * @param parent
     * @param layoutId
     * @return
     */
    protected View getView(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowLoadMore && isFooterPosition(position)) {
            return VIEW_TYPE_LOAD_MORE;
        }
        return super.getItemViewType(position);
    }

    /**
     * 是否为底部位置
     *
     * @param position
     * @return
     */
    protected boolean isFooterPosition(int position) {
        return (getItemCount() - 1) == position;
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        if (isShowLoadMore) {
            itemCount++;
        }
        return itemCount;
    }

    public void showLoadMoreView() {
        isShowLoadMore = true;
        notifyItemInserted(getItemCount());
    }

    public void hideLoadMoreView() {
        isShowLoadMore = false;
        notifyItemRemoved(getItemCount());
    }

    public PullUpRecyclerviewAdapter(List<DATA> normalDatas, RecyclerView recyclerView, IItemFactory
            factory) {
        this(normalDatas, recyclerView, factory, R.layout.load_more_view);
    }

    public PullUpRecyclerviewAdapter(List<DATA> normalDatas, RecyclerView recyclerView, IItemFactory
            factory, int loadMoreResId) {
        super(normalDatas, recyclerView, factory);
        this.loadMoreResId = loadMoreResId;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //获取LayoutManager
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                //获取可见的最后一个item的位置
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();


                //获取可见item的个数
                int visibleItemCount = layoutManager.getChildCount();
                //获取item的总数
                int totalItemCount = layoutManager.getItemCount();
                //
                if (visibleItemCount > 0
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition >= totalItemCount - 1
                        ) {
                    if (null != listener) {
                        listener.onLoadMore(recyclerView);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
    }

    private static class LoadMoreHolder extends IItemFactory.BaseViewHolder {

        public LoadMoreHolder(View itemView) {
            super(itemView);
        }
    }

}
