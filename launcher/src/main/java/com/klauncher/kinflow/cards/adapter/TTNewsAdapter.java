package com.klauncher.kinflow.cards.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klauncher.launcher.R;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.ss.android.sdk.minusscreen.model.Article;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xixionghui on 2016/3/27.
 */
public class TTNewsAdapter extends RecyclerView.Adapter<TTNewsAdapter.NewsAdapterViewHolder> {
    private List<Article> mArticleList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private CardInfo mCardInfo;

    public TTNewsAdapter(List<Article> articleList, Context context, CardInfo cardInfo) {
        setArticleList(articleList);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mCardInfo = cardInfo;
    }

    public void updateAdapter(List<Article> articleList, CardInfo cardInfo) {
        this.mArticleList = articleList;
        this.mCardInfo = cardInfo;
        this.notifyDataSetChanged();
    }

    public void setArticleList(List<Article> articleList) {
        if (articleList != null) {
            mArticleList.clear();
            mArticleList.addAll(articleList);
        } else {
            mArticleList = new ArrayList<>();
        }
    }

    @Override
    public NewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = mInflater.inflate(R.layout.one_news, parent, false);
        return new NewsAdapterViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(NewsAdapterViewHolder holder, int position) {
        Article article = mArticleList.get(position);
        holder.tv_title.setText(article.mTitle);
//        holder.tv_commentCount.setText(String.valueOf(article.mPublishTime));
        Date date = null;
        try {
            date = DateUtils.getInstance().timestamp2Date(article.mPublishTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (null != date)
            holder.tv_commentCount.setText(DateUtils.getInstance().getHourMinute(date));
    }

    @Override
    public int getItemCount() {
        if (null == mArticleList || mArticleList.size() == 0) {
            return 0;
        }
        return mArticleList.size();
    }

    public class NewsAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private TextView tv_commentCount;

        public NewsAdapterViewHolder(View itemView) {
            super(itemView);
            tv_commentCount = (TextView) itemView.findViewById(R.id.news_publish_time);
            tv_title = (TextView) itemView.findViewById(R.id.news_title);

            tv_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Article article = mArticleList.get(getPosition());
                    Bundle extras = new Bundle();
                    extras.putString(OpenMode.OPEN_URL_KEY, article.mSrcUrl);
                    extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL + article.mGroupId);
                    mCardInfo.open(mContext, extras);
                }
            });
        }
    }
}
