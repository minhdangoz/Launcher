package com.kapp.kinflow.view.recyclerview.adapter;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.IItemType;
import com.kapp.kinflow.view.recyclerview.event.OnItemEventListener;
import com.kapp.kinflow.view.recyclerview.event.RecycleViewEvent;
import com.kapp.kinflow.view.recyclerview.model.BaseItemBean;

import java.util.LinkedList;
import java.util.List;



/**
 * description：recycleview的adpapter，封装了头部和尾部的操作，具体item的初始化和绑定数据在IItemFactory中
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public class RecycleViewCommonAdapter<DATA extends BaseItemBean>
        extends RecyclerView.Adapter<IItemFactory.BaseViewHolder> {
    private static final String TAG = RecycleViewCommonAdapter.class.getSimpleName();
    private static final boolean DEBUG = true;
    /**
     * 说明：
     * 为recyclerview添加item事件
     * 1.先调用setOntemXXX事件
     * 2.调用完后，调用initItemEvent();
     * 例：
     * adapter.setOnItemClickListener(listener);
     * adapter.setOnItemLongClickListener(listener);
     * adapter.commitItemEvent();//注意调用先后顺序
     */


    protected List<DATA> normalDatas;
    private SparseArrayCompat<View> headerViews;
    private SparseArrayCompat<View> footerViews;

    /**
     * 存放头部添加进来的索引，即在headerViews中的第几个位置
     */
    private LinkedList<Integer> headerIndexs;
    /**
     * 存放尾部部添加进来的索引，即在footerViews中的第几个位置
     */
    private LinkedList<Integer> footerIndexs;

    protected Context context;
    protected IItemFactory factory;
    protected RecycleViewEvent.OnItemClickListener onItemClickListener;
    protected RecycleViewEvent.OnItemLongClickListener onItemLongClickListener;
    protected RecyclerView recyclerView;


    public RecycleViewCommonAdapter(List<DATA> normalDatas, RecyclerView recyclerView, IItemFactory
            factory) {
        this.normalDatas = normalDatas;
        this.context = recyclerView.getContext();
        this.recyclerView = recyclerView;
        this.factory = factory;
        this.factory.bindAdapter(this);
        headerViews = new SparseArrayCompat<>();
        footerViews = new SparseArrayCompat<>();
        headerIndexs = new LinkedList<>();
        footerIndexs = new LinkedList<>();
    }


    /**
     * 添加点击事件，注意添加完所有item事件后要调用initItemEvent()
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(RecycleViewEvent.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 添加长按事件，注意添加完所有item事件后要调用initItemEvent()
     *
     * @param onItemLongClickListener
     */
    public void setOnItemLongClickListener(RecycleViewEvent.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 初始化item事件，该方法放在添加了各种item事件之后
     */
    public void commitItemEvent() {
        OnItemEventListener onItemEventListener = new OnItemEventListener(context, recyclerView);
        onItemEventListener.setOnItemClickListener(onItemClickListener);
        onItemEventListener.setOnItemLongClickListener(onItemLongClickListener);
        onItemEventListener.commitEvent();
        this.recyclerView.addOnItemTouchListener(onItemEventListener);
    }

    /**
     * 添加头部,添加到头部部分最底部
     *
     * @param view
     */
    public void addHeader(View view) {
        int headerSize = headerViews.size();
        headerIndexs.add(headerSize);
        headerViews.put(getHeaderKeyInHeader(headerSize), view);
        notifyItemInserted(headerSize);
    }


    /**
     * 移除头部部分最底部的头部
     */
    public void removeHeader() {
        removeHeader(getHeaderTopIndex());
    }

    private int getHeaderTopIndex() {
        if (headerIndexs.isEmpty()) {
            return -1;
        }
        return headerIndexs.get(headerIndexs.size() - 1);
    }

    /**
     * 移除头部
     *
     * @param position 从最顶部的位置算0数起
     */
    public void removeHeader(int position) {
        if (position < 0 || position >= headerViews.size()) {
            return;
        }
        headerIndexs.remove(position);
        headerViews.remove(getHeaderKeyInHeader(position));
        notifyItemRemoved(position);
    }

    /**
     * 添加尾部，添加到尾部部分最底部
     *
     * @param view
     */
    public void addFooter(View view) {
        footerIndexs.add(footerViews.size());
        footerViews.put(getFooterKeyInFooter(footerViews.size()), view);

        int insertdPos = getItemCount();
        notifyItemInserted(insertdPos);
    }

    private int getFooterTopIndex() {
        if (footerIndexs.isEmpty()) {
            return -1;
        }
        return footerIndexs.get(footerIndexs.size() - 1);
    }


    /**
     * 追加集合
     *
     * @param datas
     */
    public void addAll(List<DATA> datas) {
        addAll(datas, true);
    }

    /**
     * 移除头部
     *
     * @param position 从最低端的算0数起
     */
    public void removeFooter(int position) {
        if (position < 0 || position > footerViews.size()) {
            return;
        }
        footerIndexs.remove(position);
        footerViews.remove(getFooterKeyInFooter(position));
        int posStart = getHeaderSize() + getNormalItemCount() + position;
        notifyItemRemoved(posStart);
    }

    /**
     * @param position 在header部分的索引
     * @return
     */
    private int getHeaderKeyInHeader(int position) {
        return IItemType.TYPE_HEADER_MIN + position;
    }

    /**
     * @param position 在footer部分的索引
     * @return
     */
    private int getFooterKeyInFooter(int position) {
        return IItemType.TYPE_FOOTER_MIN + position;
    }


    /**
     * 移除最底部的头部
     */
    public void removeFooter() {
        removeFooter(getFooterTopIndex());
    }

    /**
     * 更新全部数据，将已经存在的数据清空
     *
     * @param datas
     */
    public void updateAll(List<DATA> datas) {
        addAll(datas, false);
    }

    /**
     * 添加单个数据
     *
     * @param data
     * @return
     */
    public void insertItem(DATA data) {
        insertItem(normalDatas.size(), data);
    }

    /**
     * 返回正常item部分插入的位置
     *
     * @param position 正常item的第一个算0
     * @param data
     */
    public void insertItem(int position, DATA data) {
        if (position < 0 || position > getNormalItemCount()) {
            return;
        }
        if (null != data) {
            normalDatas.add(position, data);
            int insertedPos = getNormalPositiionInRecyclerView(position);
            notifyItemInserted(insertedPos);
        }
    }

    /**
     * 插入正常item部分的第一个
     *
     * @param data
     */
    public void insertItemFirst(DATA data) {
        insertItem(0, data);
    }

    /**
     * 插入正常部分的最后一个
     *
     * @param data
     */
    public void insertItemLast(DATA data) {
        insertItem(getNormalItemCount(), data);
    }

    /**
     * 在正常item部分的指定位置插入集合
     *
     * @param position
     * @param datas
     */
    public void insertItems(int position, List<DATA> datas) {
        if (position < 0 || position >= getNormalItemCount()) {
            return;
        }
        if (null != datas) {
            normalDatas.addAll(position, datas);
            int startPos = getNormalPositiionInRecyclerView(position);
            notifyItemRangeInserted(startPos, datas.size());
        }
    }

    /**
     * 返回在整个recyclerview中正常item部分的位置
     *
     * @param positionInNormal 在正常item中的下标位置
     * @return
     */
    private int getNormalPositiionInRecyclerView(int positionInNormal) {
        return getHeaderSize() + positionInNormal;
    }

    /**
     * 正常item部分,更新指定位置的item
     *
     * @param position
     */
    public void updateItem(int position) {
        if (position < 0 || position >= getNormalItemCount()) {
            return;
        }
        int changeIndex = getNormalPositiionInRecyclerView(position);
        notifyItemChanged(changeIndex);
    }

    /**
     * 互换位置
     *
     * @param startIndex
     * @param endIndex
     */
    public void exchange(int startIndex, int endIndex) {
        int normalItemCount = getNormalItemCount();
        if (-1 == startIndex || -1 == endIndex || startIndex >= normalItemCount || endIndex >= normalItemCount) {
            return;
        }
        if (startIndex == endIndex) {
            updateItem(startIndex);
            return;
        }

        DATA endObject = normalDatas.get(endIndex);
        DATA startObject = normalDatas.get(startIndex);
        normalDatas.add(startIndex, endObject);
        normalDatas.remove(startIndex + 1);
        normalDatas.add(endIndex, startObject);
        normalDatas.remove(endIndex + 1);
        updateItem(startIndex);
        updateItem(endIndex);
    }

    /**
     * 正常item部分，更新指定对象对应的位置
     *
     * @param data
     */
    public void updateItem(DATA data) {
        if (null != data) {
            int index = normalDatas.indexOf(data);
            if (index != -1) {
                int changeIndex = getNormalPositiionInRecyclerView(index);
                notifyItemChanged(changeIndex);
            }
        }
    }

    /**
     * 正常item部分，指定位置上更新指定数量的item
     *
     * @param position
     * @param count
     */
    public void updateItems(int position, int count) {
        int normalItemCount = getNormalItemCount();
        if (position < 0 || position >= normalItemCount || count <= 0) {
            return;
        }
        int realCount = position + count > normalItemCount ? normalItemCount - position : count;
        realCount = Math.min(realCount, normalItemCount);
        int startPos = getNormalPositiionInRecyclerView(position);
        notifyItemRangeChanged(startPos, realCount);
    }

    private void addAll(List<DATA> datas, boolean append) {
        if (!append) {
            int oldNormalSize = getNormalItemCount();
            normalDatas.clear();
            int preSize = getHeaderSize();
            normalDatas.addAll(datas);
            notifyItemRangeChanged(preSize, oldNormalSize);
        } else {
            int preSize = getHeaderSize() + getNormalItemCount();
            normalDatas.addAll(datas);
            notifyItemRangeInserted(preSize, datas.size());
        }
    }

    /**
     * 移除正常item
     *
     * @param position 所在的正常部分的位置
     */
    public void removeItem(int position) {
        if (position < 0 || position >= getNormalItemCount()) {
            return;
        }
        normalDatas.remove(position);
        int removePos = getNormalPositiionInRecyclerView(position);
        notifyItemRemoved(removePos);
    }

    /**
     * 移除指定数量的正常部分的item
     *
     * @param startPosition
     * @param count
     */
    public void removeItems(int startPosition, int count) {
        int normalCount = getNormalItemCount();
        if (startPosition < 0 || startPosition >= normalCount) {
            return;
        }
        int realRemoveCount = startPosition + count > normalCount ? normalCount - startPosition : count;
        realRemoveCount = Math.min(realRemoveCount, normalCount);
        logDebug("removeItems(startPosition,count) startPosition=" + startPosition + " ,count=" + count + " ," +
                "realRemoveCount=" + realRemoveCount + " ,normalCount=" + normalCount);
        for (int i = realRemoveCount - 1; i >= 0; i--) {
            int removeIndex = startPosition + i;
//            logDebug("removeItems(startPosition,count) removeIndex=" + removeIndex);
            normalDatas.remove(removeIndex);
        }
        notifyItemRangeRemoved(startPosition, realRemoveCount);
    }

    private void logDebug(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

    /**
     * 清空头部
     */
    public void clearHeaders() {
        int headerSize = getHeaderSize();
        if (headerSize == 0) {
            return;
        }
        headerIndexs.clear();
        headerViews.clear();
        notifyItemRangeRemoved(0, headerSize);
    }

    /**
     * 清空尾部
     */
    public void clearFooters() {
        int footerSize = getFooterSize();
        if (footerSize == 0) {
            return;
        }
        footerIndexs.clear();
        footerViews.clear();
        int fromPosition = getHeaderSize() + getNormalItemCount();
        notifyItemRangeRemoved(fromPosition, footerSize);
    }

    /**
     * 清空除了头部和尾部的所有数据
     */
    public void clearNormal() {
        int normalItemCount = getNormalItemCount();
        if (normalItemCount == 0) {
            return;
        }
        normalDatas.clear();
        int startPos = getHeaderSize();
        notifyItemRangeRemoved(startPos, normalItemCount);
    }

    public List<DATA> getNormalDatas() {
        return normalDatas;
    }

    /**
     * 返回头部个数
     *
     * @return
     */
    public int getHeaderSize() {
        return headerViews.size();
    }

    /**
     * 返回尾部个数
     *
     * @return
     */
    public int getFooterSize() {
        return footerViews.size();
    }

    /**
     * @param position
     * @return
     */
    public boolean isHeaderPos(int position) {
        return position < getHeaderSize();
    }

    /**
     * 返回除了头部和尾部的所有item的数量
     *
     * @return
     */
    public int getNormalItemCount() {
        return normalDatas.size();
    }

    public boolean isFooterPos(int position) {
        return position >= getHeaderSize() + getNormalItemCount();
    }

    @Override
    public int getItemViewType(int position) {

        //头部类型
        if (isHeaderPos(position)) {
            return headerViews.keyAt(position);
        }

        //尾部类型
        if (isFooterPos(position)) {
            return footerViews.keyAt(position - getHeaderSize() - getNormalItemCount());
        }

        //正常item类型
        return normalDatas.get(position - getHeaderSize()).getItemType();
    }

    @Override
    public IItemFactory.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (isHeader(viewType)) {
            return new IItemFactory.CommonViewHolder(headerViews.get(viewType));
        } else if (isFooter(viewType)) {
            return new IItemFactory.CommonViewHolder(footerViews.get(viewType));
        }
        return factory.onCreateNormalViewHolder(parent, viewType);
    }


    /**
     * 是否是头部
     *
     * @param viewType
     * @return
     */
    public boolean isHeader(int viewType) {
        return viewType >= IItemType.TYPE_HEADER_MIN && viewType < IItemType.TYPE_HEADER_MAX;
    }

    /**
     * 是否是尾部
     *
     * @param viewType
     * @return
     */
    public boolean isFooter(int viewType) {
        return viewType >= IItemType.TYPE_FOOTER_MIN && viewType < IItemType.TYPE_FOOTER_MAX;
    }

    public SparseArrayCompat<View> getHeaders() {
        return headerViews;
    }

    public SparseArrayCompat<View> getFooters() {
        return footerViews;
    }

    /**
     * @param position 在RecyclerVew中的索引位置
     * @return
     */
    public int getHeaderKeyInRecycleView(int position) {
        return getHeaderKeyInHeader(position);
    }

    /**
     * @param position 在RecyclerVew中的索引位置
     * @return
     */
    public int getFooterKeyInRecycleView(int position) {
        int footerPos = position - getHeaderSize() - getNormalItemCount();
        return getFooterKeyInFooter(footerPos);
    }

    @Override
    public void onBindViewHolder(IItemFactory.BaseViewHolder holder, int position) {

        if (isHeaderPos(position)) {
            factory.onBindHeaderViewHolder(holder, headerViews, getHeaderKeyInHeader(position));
        } else if (isFooterPos(position)) {
            int footerPos = getFooterSize() - (getItemCount() - position);
            factory.onBindFooterViewHolder(holder, footerViews, getFooterKeyInFooter(footerPos));
        } else {
            int itemViewType = getItemViewType(position);
            factory.onBindNormalViewHolder(holder, normalDatas, position - getHeaderSize(), itemViewType);
        }
        factory.setItemAnimation(holder, position);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        factory.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        factory.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewDetachedFromWindow(IItemFactory.BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        factory.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return getHeaderSize() + getFooterSize() + getNormalItemCount();
    }
}
