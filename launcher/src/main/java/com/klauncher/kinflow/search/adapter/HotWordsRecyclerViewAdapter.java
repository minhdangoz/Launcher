package com.klauncher.kinflow.search.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.klauncher.launcher.R;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.search.model.HotWord;

import java.util.List;
import java.util.Random;

/**
 * Created by xixionghui on 2016/3/15.
 */
public class HotWordsRecyclerViewAdapter extends RecyclerView.Adapter<HotWordsRecyclerViewAdapter.HotWordsViewHolder> {

    private final Context context;
    private LayoutInflater inflater;
    List<HotWord> hotWordList;
    String[] allColors;

    public HotWordsRecyclerViewAdapter(Context context, List<HotWord> hotWordList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.hotWordList = hotWordList;
        this.allColors = context.getResources().getStringArray(R.array.color_list);
    }


    @Override
    public HotWordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return null;
        View view = inflater.inflate(R.layout.item_recycler_view, parent, false);
        return new HotWordsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HotWordsViewHolder holder, int position) {
        holder.tv_hotWord.setText(hotWordList.get(position).getWord());
        //设置color
        int colorArrayLength = allColors.length;
        if (position < colorArrayLength) {
            holder.getRootItem().setBackgroundColor(Color.parseColor(allColors[position]));
        } else {//
            int index = 0;
            do {
                index = position - colorArrayLength;
            } while (position < colorArrayLength);
            holder.getRootItem().setBackgroundColor(Color.parseColor(allColors[index]));
        }
    }

    @Override
    public int getItemCount() {
        return hotWordList == null ? 0 : hotWordList.size();
    }

    public class HotWordsViewHolder extends RecyclerView.ViewHolder {

        TextView tv_hotWord;
        View rootItem;

        public HotWordsViewHolder(View itemView) {
            super(itemView);
            rootItem = itemView;
            tv_hotWord = (TextView) itemView.findViewById(R.id.hot_word);

//            //设置color
//            int itemId = (int) getItemId();
//            String[] allColors = context.getResources().getStringArray(R.array.color_list);
//            int index = new Random().nextInt(allColors.length - 1);
//            itemView.setBackgroundColor(Color.parseColor(allColors[index]));
        }

        public TextView getTv_hotWord() {
            return tv_hotWord;
        }

        public void setTv_hotWord(TextView tv_hotWord) {
            this.tv_hotWord = tv_hotWord;
        }

        public View getRootItem() {
            return rootItem;
        }

        public void setRootItem(View rootItem) {
            this.rootItem = rootItem;
        }
    }


}
