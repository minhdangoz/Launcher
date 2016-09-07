package com.klauncher.kinflow.views.recyclerView.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.utilities.CollectionsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixionghui on 16/6/29.
 * 此类为RecyclerView的基础类,要在此类基础上实现扩展功能.置顶\刷新\左右滑动等功能.
 * 之前总结的RecyclerView中遇到的坑.
 */
//public abstract class BaseRecyclerViewAdapter<E,T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
public abstract class BaseRecyclerViewAdapter<E,T extends BaseRecyclerViewHolder<E>> extends RecyclerView.Adapter<T> {
    public Context mContext = KLauncherApplication.mKLauncherApplication;
    public LayoutInflater mInflater;
    public List<E> mElementList = new ArrayList<>();

    /**
     * 构造方法
     * 此方法需要在子类中实现
     * @param context
     * @param elementList
     */
    public BaseRecyclerViewAdapter (Context context,List<E> elementList) {
        if (null!=context) this.mContext = context;
        mInflater = LayoutInflater.from(this.mContext);
        setElementList(elementList);
    }

    public void setElementList(List<E> elementList) {
        if (!CollectionsUtils.collectionIsNull(elementList)) {
            mElementList.clear();
            mElementList.addAll(elementList);
        }
    }

    /**
     * 老方法更新adapter
     * @param elementList
     */
    public void updateAdapter (List<E> elementList) {
        setElementList(elementList);
        notifyDataSetChanged();//Notify any registered observers that the data set has changed.
    }



    @Override
    public int getItemCount() {
        if (null==mElementList) return 0;
        return mElementList.size();
    }

    //
//    View itemRootView = mInflater.inflate(mLayoutXmlId,parent,false);
//    return new BaseRecyclerViewHolder(itemRootView);


}
