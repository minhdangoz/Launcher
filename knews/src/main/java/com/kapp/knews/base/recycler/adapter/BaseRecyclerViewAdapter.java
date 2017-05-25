/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.kapp.knews.base.recycler.adapter;

import android.support.annotation.AnimRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kapp.knews.base.recycler.listener.OnItemClickListener;
import com.kapp.knews.helper.java.collection.CollectionsUtils;

import java.util.LinkedList;
import java.util.List;


/**
* @date 创建时间: 16/11/18 下午6:24
* @author 习雄辉
* @Description
* @version
*/
public class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //默认item类型
    public static final int TYPE_DEFAULT = 0;//默认item类型

    //底部
    public static final int TYPE_FOOTER = 1;//底部item类型

    //顶部
    public static final int TYPE_HEADER = 2;//顶部item类型


    protected int mLastPosition = -1;

    //是否显示footer
    protected boolean mIsShowFooter;

    //数据
    protected LinkedList<T> mList;

    //item监听器
    protected OnItemClickListener mOnItemClickListener;

    /**
     * 构造函数
     * @param list
     */
    public BaseRecyclerViewAdapter(LinkedList<T> list) {
        mList = list;
    }

    /**
     * 设置对item的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 创建ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    /**
     * 绑定ViewHolder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (getItemViewType(position) == TYPE_FOOTER) {
            if (layoutParams != null) {
                if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView
                            .getLayoutParams();
                    params.setFullSpan(true);
                }
            }
        }
    }

/*    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isFooterPosition(position)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                params.setFullSpan(true);
            }
        }

    }*/


    /**
     * 获取rootVIew
     * @param parent
     * @param layoutId
     * @return
     */
    protected View getView(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        int itemSize = mList.size();
        if (mIsShowFooter) {
            itemSize += 1;
        }
        return itemSize;
    }

    /**
     * 设置item出现时的动画效果
     * @param holder
     * @param position
     * @param type
     */
    protected void setItemAppearAnimation(RecyclerView.ViewHolder holder, int position, @AnimRes int type) {
        if (position > mLastPosition/* && !isFooterPosition(position)*/) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), type);
            holder.itemView.startAnimation(animation);
            mLastPosition = position;
        }
    }

    /**
     * 是否为底部位置
     * @param position
     * @return
     */
    protected boolean isFooterPosition(int position) {
        return (getItemCount() - 1) == position;
    }

    /**
     * 添加一条数据
     * @param position
     * @param item
     */
    public void add(int position, T item) {
        mList.add(position, item);
        notifyItemInserted(position);
    }

    /**
     * 添加一组数据
     * @param data
     */
    public void addMore(List<T> data) {
        if (CollectionsUtils.collectionIsNull(data)) return;
        int startPosition = mList.size();
        mList.addAll(data);
        notifyItemRangeInserted(startPosition, mList.size());
    }

    /**
     * 删除某一条数据
     * @param position
     */
    public void delete(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 获取所有数据
     * @return
     */
    public List<T> getList() {
        return mList;
    }

    /***
     * 设置数据集合
     * @param items
     */
    public void setList(LinkedList<T> items) {
        mList = items;
    }

    public void addFirstAll(List<T> elem) {
        mList.addAll(0, elem);
        notifyDataSetChanged();
    }

    /**
     * 显示footer
     */
    public void showFooter() {
        mIsShowFooter = true;
        notifyItemInserted(getItemCount());
    }

    /**
     * 隐藏footer
     */
    public void hideFooter() {
        mIsShowFooter = false;
        notifyItemRemoved(getItemCount());
    }

    /**
     * FooterViewHolder
     */
    protected class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
