package com.klauncher.kinflow.views.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.manager.JRTTCardContentManager;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewAdapter;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewHolder;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by xixionghui on 16/9/1.
 */
public class Kinflow2NewsAdapter extends BaseRecyclerViewAdapter<JinRiTouTiaoArticle,Kinflow2NewsAdapter.NewsApdaterViewHolder<JinRiTouTiaoArticle>> {


    /**
     * 构造方法
     * 此方法需要在子类中实现
     *
     * @param context
     * @param elementList
     */
    public Kinflow2NewsAdapter(Context context, List<JinRiTouTiaoArticle> elementList) {
        super(context, elementList);
    }

//    public Kinflow2NewsAdapter(Context context,CardInfo cardInfo){
//        int cardSecondTypeId = cardInfo.getCardSecondTypeId();
//
//    }

    public void addData(Context context,CardInfo cardInfo) {
        int cardSecondTypeId = cardInfo.getCardSecondTypeId();
        switch (cardSecondTypeId) {
            case CardIdMap.CARD_TYPE_NEWS_TT_REDIAN://头条热点
            case CardIdMap.CARD_TYPE_NEWS_TT_SHEHUI://头条社会
            case CardIdMap.CARD_TYPE_NEWS_TT_YULE://头条娱乐
            case CardIdMap.CARD_TYPE_NEWS_TT_CAIJING://头条财经
            case CardIdMap.CARD_TYPE_NEWS_TT_TIYU://头条体育
            case CardIdMap.CARD_TYPE_NEWS_TT_KEJI://头条科技
            case CardIdMap.CARD_TYPE_NEWS_TT_JUNSHI://头条军事
            case CardIdMap.CARD_TYPE_NEWS_TT_QICHE://头条汽车
                Log.e(TAG, "addData: jinriTouTiao");
                addJinRiTouTiao(cardInfo);
                break;
            case CardInfo.CARD_TYPE_SETTING_WIFI:
                Log.e(TAG, "addData: wifi");
                break;
            case CardIdMap.ADVERTISEMENT_YOKMOB:
                Log.e(TAG, "addData: yokmob");
                break;
            case CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN:
            case CardIdMap.CARD_TYPE_NEWS_YD_REDIAN:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHEHUI:
            case CardIdMap.CARD_TYPE_NEWS_YD_YULE:
            case CardIdMap.CARD_TYPE_NEWS_YD_CAIJING:
            case CardIdMap.CARD_TYPE_NEWS_YD_TIYU:
            case CardIdMap.CARD_TYPE_NEWS_YD_KEJI:
            case CardIdMap.CARD_TYPE_NEWS_YD_JUNSHI:
            case CardIdMap.CARD_TYPE_NEWS_YD_MINSHENG:
            case CardIdMap.CARD_TYPE_NEWS_YD_MEINV:
            case CardIdMap.CARD_TYPE_NEWS_YD_DUANZI:
            case CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHISHANG:
            case CardIdMap.CARD_TYPE_NEWS_YD_QICHE:
            case CardIdMap.CARD_TYPE_NEWS_YD_GAOXIAO:
            case CardIdMap.CARD_TYPE_NEWS_YD_SHIPIN:
            case CardIdMap.CARD_TYPE_NEWS_YD_DIANYING:
            case CardIdMap.CARD_TYPE_NEWS_YD_JIANSHEN:
            case CardIdMap.CARD_TYPE_NEWS_YD_LVYOU:
                Log.e(TAG, "addData: yidianzixun");
                break;
            default:
                Log.e(TAG, "addData: 未知的view啊 ,大哥===================");
                break;
        }
    }

    CardInfo jrttCardInfo;
    private void addJinRiTouTiao(CardInfo jrttCardInfo){
        try {
            this.jrttCardInfo = jrttCardInfo;
            //底部赋值
            String footerName = jrttCardInfo.getCardFooter();
            if (TextUtils.isEmpty(footerName)) {
                footerName = "更多新闻" + jrttCardInfo.getCardSecondTypeId();
            } else {
                footerName = "更多" + footerName;
            }
//            tvMoreNews.setText(footerName);
            //中部赋值
            JRTTCardContentManager jrttCardContentManager = (JRTTCardContentManager) jrttCardInfo.getmCardContentManager();
            List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList = jrttCardContentManager.getJinRiTouTiaoArticleList();
            updateAdapter(jinRiTouTiaoArticleList);
        } catch (Exception e) {
            Log.e("Kinflow", "今日头条API版,在设置数据时setJrttCardInfo,出错" + e.getMessage());
        }
    }

    private static final String TAG = "Kinflow";
    @Override
    public NewsApdaterViewHolder<JinRiTouTiaoArticle> onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder: ");
        View itemRootView = LayoutInflater.from(mContext).inflate(R.layout.kinflow2_news_item_layout,parent,false);
        return new NewsApdaterViewHolder<>(itemRootView);
    }

    @Override
    public void updateAdapter(List<JinRiTouTiaoArticle> elementList) {
        Toast.makeText(mContext, "开始更新数据", Toast.LENGTH_SHORT).show();
        super.updateAdapter(elementList);
    }

    @Override
    public void onBindViewHolder(NewsApdaterViewHolder<JinRiTouTiaoArticle> holder, int position) {
        Log.e(TAG, "onBindViewHolder: ");
        holder.bundData2View(mElementList.get(position));
    }

    public class NewsApdaterViewHolder<T> extends BaseRecyclerViewHolder<JinRiTouTiaoArticle> {
        RelativeLayout mBigImageLayout,m3ImageLayout;
        LinearLayout m1ImageLayout;
        //bigImage
        TextView mBigNewsHeader,mBigNewsFooter;
        ImageView mBigImage;
        //1Image
        TextView m1ImageFooter,m1ImageTitle;
        ImageView m1ImageView;
        //3Image
        TextView m3ImageHeader,m3ImageFooter;
        ImageView m3ImageLeft,m3ImageMiddle,m3ImageRight;


        public NewsApdaterViewHolder(View itemView) {
            super(itemView);
        }

        public static final String TAG = "Kinflow";
        @Override
        public void initView(View itemRootView) {
            Log.e(TAG, "initView: ");
            //大图
            mBigImageLayout = (RelativeLayout) itemRootView.findViewById(R.id.kinflow_big_image);
            mBigNewsHeader = (TextView) mBigImageLayout.findViewById(R.id.yidian_news_title);
            mBigNewsFooter = (TextView) mBigImageLayout.findViewById(R.id.yidian_news_publish_time);
            mBigImage = (ImageView) mBigImageLayout.findViewById(R.id.big_image);
            //一张图片
            m1ImageLayout = (LinearLayout) itemRootView.findViewById(R.id.kinflow_news_1image);
            m1ImageFooter = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_publish_time);
            m1ImageTitle = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_title);
            m1ImageView = (ImageView) m1ImageLayout.findViewById(R.id.yidian_image);
            m1ImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JinRiTouTiaoArticle jinRiTouTiaoArticle = mElementList.get(getPosition());
                    Bundle extras = new Bundle();
                    String articleUrl = TextUtils.isEmpty(jinRiTouTiaoArticle.getArticle_url()) ? jinRiTouTiaoArticle.getUrl() : jinRiTouTiaoArticle.getArticle_url();

                    extras.putString(OpenMode.OPEN_URL_KEY, articleUrl);
                    extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL + jinRiTouTiaoArticle.getGroup_id());
                    String finalOpenComponent_Head1Imapge = jrttCardInfo.open(mContext, extras);
                    if (!TextUtils.isEmpty(finalOpenComponent_Head1Imapge))
                        PingManager.getInstance().reportUserAction4cardNewsOpen(PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO, String.valueOf(jrttCardInfo.getCardSecondTypeId()));
                }
            });
            //三张图片
            m3ImageLayout = (RelativeLayout) itemRootView.findViewById(R.id.kinflow_news_3image);
            m3ImageHeader = (TextView) m3ImageLayout.findViewById(R.id.yidian_news_title);
            m3ImageFooter = (TextView) m3ImageLayout.findViewById(R.id.yidian_news_publish_time);
            m3ImageLeft = (ImageView) m3ImageLayout.findViewById(R.id.yidian_image_left);
            m3ImageMiddle = (ImageView) m3ImageLayout.findViewById(R.id.yidian_image_middle);
            m3ImageRight = (ImageView) m3ImageLayout.findViewById(R.id.yidian_image_right);
            m3ImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JinRiTouTiaoArticle jinRiTouTiaoArticle = mElementList.get(getPosition());
                    Bundle extras = new Bundle();
                    String articleUrl = TextUtils.isEmpty(jinRiTouTiaoArticle.getArticle_url()) ? jinRiTouTiaoArticle.getUrl() : jinRiTouTiaoArticle.getArticle_url();
                    extras.putString(OpenMode.OPEN_URL_KEY, articleUrl);
                    extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL + jinRiTouTiaoArticle.getGroup_id());
                    String finalOpenComponent_Head3Imapge = jrttCardInfo.open(mContext, extras);
                    if (!TextUtils.isEmpty(finalOpenComponent_Head3Imapge))
                        PingManager.getInstance().reportUserAction4cardNewsOpen(PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO, String.valueOf(jrttCardInfo.getCardSecondTypeId()));
                }
            });
        }

        @Override
        public void bundData2View(JinRiTouTiaoArticle modelData) {
            Log.e(TAG, "bundData2View: ");
            //根据图片张数,提供展现方式
            List<JinRiTouTiaoArticle.ImageInfo> threeImageList = modelData.getImage_list();
            if (null != threeImageList && threeImageList.size() >= 3) {//三图模式有数据&&确实有三张图
                m3ImageLayout.setVisibility(View.VISIBLE);
                addJrttHeadWith3Image(modelData);
            } else {
                m1ImageLayout.setVisibility(View.VISIBLE);
                addJrttHeadWith1Image(modelData);
            }
        }

        void addJrttHeadWith3Image(JinRiTouTiaoArticle mFirstJrttArticle) {
            Log.e("Kinflow", "addJrttHeadWith3Image: 开始添加三张图片的今日头条Card");
//            final TextView tv_title = (TextView) mJrttHeadWith3ImageLayout.findViewById(R.id.yidian_news_title);
//            final TextView tv_publishTime = (TextView) mJrttHeadWith3ImageLayout.findViewById(R.id.yidian_news_publish_time);
//            final ImageView iv_yidianImageLeft = (ImageView) mJrttHeadWith3ImageLayout.findViewById(R.id.yidian_image_left);
//            final ImageView iv_yidianImageMiddle = (ImageView) mJrttHeadWith3ImageLayout.findViewById(R.id.yidian_image_middle);
//            final ImageView iv_yidianImageRight = (ImageView) mJrttHeadWith3ImageLayout.findViewById(R.id.yidian_image_right);
            m3ImageHeader.setText(mFirstJrttArticle.getTitle());
            m3ImageFooter.setText(DateUtils.getInstance().second2TimeOrDate(mFirstJrttArticle.getPublish_time()));
//        int length = mFirstYiDianModel.getImages().length;
            int length = mFirstJrttArticle.getImage_list().size();
            for (int i = 0; i < length; i++) {
                final int finalI = i;
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        switch (finalI) {
                            case 0:
                                m3ImageLeft.setImageBitmap(bitmap);
                                break;
                            case 1:
                                m3ImageMiddle.setImageBitmap(bitmap);
                                break;
                            case 2:
                                m3ImageRight.setImageBitmap(bitmap);
                                break;
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                JinRiTouTiaoArticle.ImageInfo jrttArticleImageInfo = mFirstJrttArticle.getImage_list().get(i);
                switch (finalI) {
                    case 0:
                        m3ImageLeft.setTag(target);
                        //获取到第I张图片
                        loadImage(jrttArticleImageInfo, m3ImageLeft);
                        break;
                    case 1:
                        m3ImageMiddle.setTag(target);
                        loadImage(jrttArticleImageInfo, m3ImageMiddle);
                        break;
                    case 2:
                        m3ImageRight.setTag(target);
                        loadImage(jrttArticleImageInfo, m3ImageRight);
                        break;
                }
            }
        }

        void addJrttHeadWith1Image(JinRiTouTiaoArticle mFirstJrttArticle) {
            Log.e("Kinflow", "addJrttHeadWith1Image: 开始添加一张图片的今日头条Card");
            //布局
//            final TextView tv_title = (TextView) mJrttHeadWith1ImageLayout.findViewById(R.id.yidian_news_title);
//            final TextView tv_publishTime = (TextView) mJrttHeadWith1ImageLayout.findViewById(R.id.yidian_news_publish_time);
//            final ImageView iv_yidianImage = (ImageView) mJrttHeadWith1ImageLayout.findViewById(R.id.yidian_image);
            //数据
            m1ImageTitle.setText(mFirstJrttArticle.getTitle());
            m1ImageFooter.setText(DateUtils.getInstance().second2TimeOrDate(mFirstJrttArticle.getPublish_time()));
//        int imageCount = mFirstYiDianModel.getImages().length;
            JinRiTouTiaoArticle.ImageInfo rightImageInfo = mFirstJrttArticle.getMiddle_image();

            if (null != rightImageInfo) {//1张图
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        m1ImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                m1ImageView.setTag(target);
                loadImage(rightImageInfo, m1ImageView);
            } else {//0张图
                Log.e("Kinflow", "addJrttHeadWith1Image: 一张图片也没有,添加默认图片");
                m1ImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image_show));
            }
        }

        void loadImage(JinRiTouTiaoArticle.ImageInfo imageInfo, ImageView toutiaoImage) {
            List<JinRiTouTiaoArticle.ImageUrl> imageUrlList = imageInfo.getUrl_list();
            if (null != imageUrlList && imageUrlList.size() != 0) {
                Log.e("Kinflow", "今日头条Card加载图片loadImage,一张图片的url = " + imageUrlList.get(0).getUrl());
                Picasso.with(mContext).load(imageUrlList.get(0).getUrl()).fit().centerCrop().into(toutiaoImage);
            } else {//加载默认
                Log.e("Kinflow", "今日头条Card加载图片loadImage,一张图片也没有,添加默认图片");
                toutiaoImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image_show));
            }
        }
    }
}
