package com.klauncher.kinflow.cards.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xixionghui on 2016/3/27.
 */
public class JRTTNewsAdapter extends RecyclerView.Adapter<JRTTNewsAdapter.NewsAdapterViewHolder> {
    private List<JinRiTouTiaoArticle> mJrttArticleList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private CardInfo mCardInfo;
    public JRTTNewsAdapter(Context context, CardInfo cardInfo) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mCardInfo = cardInfo;
        //初次构建我们传入null,后面已经做了非空判断.不会出问题.我们传入null的目的是因为后面还会使用updateYDnewsRecycler()
        //来重新填充数据,为了防止重复填充绘制view.减小内存压力.第一次传入null即可.
        setArticleList(null);
    }

    public void setArticleList(List<JinRiTouTiaoArticle> articleList) {
        if (articleList != null) {
            mJrttArticleList.clear();
            mJrttArticleList.addAll(articleList);
        }else {
            this.mJrttArticleList = new ArrayList<>();
        }
    }

    public void updateYDnewsRecycler(List<JinRiTouTiaoArticle> yiDianModelList) {
        setArticleList(yiDianModelList);
        notifyDataSetChanged();
    }

    @Override
    public NewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = mInflater.inflate(R.layout.one_news, parent, false);
        return new NewsAdapterViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(NewsAdapterViewHolder holder, int position) {
        JinRiTouTiaoArticle jinRiTouTiaoArticle = mJrttArticleList.get(position);
        holder.tv_title.setText(jinRiTouTiaoArticle.getTitle());
        holder.tv_publishTime.setText(DateUtils.getInstance().second2TimeOrDate(jinRiTouTiaoArticle.getPublish_time()));
//        try {
//            Date date = DateUtils.getInstance().parsePattern(DateUtils.PATTERN_TYPE_yyyy_MM_dd_HH_mm_ss, JinRiTouTiaoArticle.getDate());
//            holder.tv_publishTime.setText(DateUtils.getInstance().getHourMinute(date));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public int getItemCount() {
        if (null == mJrttArticleList || mJrttArticleList.size() == 0) {
            return 0;
        }
        return mJrttArticleList.size();
    }

    public class NewsAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private TextView tv_publishTime;
        public NewsAdapterViewHolder(View itemView) {
            super(itemView);
            tv_publishTime = (TextView) itemView.findViewById(R.id.news_publish_time);
            tv_title = (TextView) itemView.findViewById(R.id.news_title);

            tv_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JinRiTouTiaoArticle jinRiTouTiaoArticle = mJrttArticleList.get(getPosition());
//                    CommonUtils.getInstance().openDetail(mContext,mCardInfo.getCardOpenOptionList(),JinRiTouTiaoArticle.getUrl());
                    Bundle extras = new Bundle();
                    String articleUrl = TextUtils.isEmpty(jinRiTouTiaoArticle.getArticle_url())?jinRiTouTiaoArticle.getUrl():jinRiTouTiaoArticle.getArticle_url();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String openModel :
                            mCardInfo.getCardOpenOptionList()) {
                        stringBuilder.append(openModel).append(" | ");
                    }
                    extras.putString(OpenMode.OPEN_URL_KEY, articleUrl);
                    extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL+jinRiTouTiaoArticle.getGroup_id());
                    if (null!=mCardInfo) {
                        String finalOpenModel = mCardInfo.open(mContext, extras);
                        if (!TextUtils.isEmpty(finalOpenModel))
                        PingManager.getInstance().reportUserAction4cardNewsOpen(PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO, String.valueOf(mCardInfo.getCardSecondTypeId()));
                    }
                }
            });
        }
    }
}
