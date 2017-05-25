package com.kapp.kinflow.business.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.knews.base.recycler.adapter.BaseRecyclerViewAdapter;
import com.kapp.knews.base.recycler.data.NewsBaseData;
import com.kapp.knews.base.recycler.data.NewsBaseDataBean;
import com.kapp.knews.base.recycler.listener.OnItemClickListener;
import com.kapp.knews.helper.content.resource.ValuesHelper;
import com.kapp.knews.helper.java.collection.CollectionsUtils;
import com.kapp.knews.helper.measure.DimenUtil;
import com.kapp.knews.repository.bean.lejingda.klauncher.AdAdxBean;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdvertisement;
import com.kapp.knews.utils.KBannerAdUtil;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by xixionghui on 2016/11/22.
 */

public class NewsListAdapter<T extends NewsBaseData> extends BaseRecyclerViewAdapter<T> {

    public static final int TYPE_PHOTO_ITEM = 2;//图片类型----多张照片

    public static final int TYPE_NEWS_IMAGE_ZERO = 21;
    public static final int TYPE_NEWS_IMAGE_ONE = 22;
    public static final int TYPE_NEWS_IMAGE_MULTI = 23;

    public static final int TYPE_AD_BANNER_BIG_IMAGE_NO_TITLE = 31;
    public static final int TYPE_AD_BANNER_BIG_IMAGE_HAS_TITLE = 32;
    private static final int TYPE_ADBANNER_FROM_ADX_SDK = 33;

    //对外回调，新闻item是否被点击
    public interface OnNewsListItemClickListener extends OnItemClickListener {
        void onItemClick(View view, int position, boolean isPhoto);
    }

    /**
     * 构造函数
     *
     * @param list
     */
    public NewsListAdapter(LinkedList<T> list) {
        super(list);
    }

    //    @Override
//    public int getItemViewType(int position) {
//        /*
//        if (mIsShowFooter && isFooterPosition(position)) {//列表底部
//            return TYPE_FOOTER;
//        } else if (mList.get(position).getmNewsBean().getSummaryImageUrlList().size()==1) {//新闻：一张图片
//            return TYPE_DEFAULT;
//        } else {//新闻：多张图片---三张图片
//            return TYPE_PHOTO_ITEM;
//        }
//        */
//        //-----
//        if (mIsShowFooter && isFooterPosition(position)) {//列表底部
//            return TYPE_FOOTER;
//        }else {//非底部，正常的item：分为广告类和新闻类
//            NewsBaseData newsBaseData = mList.get(position);
//            switch (newsBaseData.getDataType()) {
//                case NewsBaseData.TYPE_NEWS://新闻类
//                    if (newsBaseData.getmNewsBean().getSummaryImageUrlList().size()==1) {//新闻一张图
//                        return TYPE_NEWS_IMAGE_ONE;
//                    }else {//新闻多张图
//                        return TYPE_NEWS_IMAGE_MULTI;
//                    }
//                case NewsBaseData.TYPE_AD://广告类
//                    if (TextUtils.isEmpty(newsBaseData.getmNewsBean().getSummaryTitle())) {//banner-无图
//                        return TYPE_AD_BANNER_BIG_IMAGE_NO_TITLE;
//                    }else {//banner-有图
//                        return TYPE_AD_BANNER_BIG_IMAGE_HAS_TITLE;
//                    }
//
//                default:
//                    return TYPE_DEFAULT;
//            }
//        }
//    }
    @Override
    public int getItemViewType(int position) {
        /*
        if (mIsShowFooter && isFooterPosition(position)) {//列表底部
            return TYPE_FOOTER;
        } else if (mList.get(position).getmNewsBean().getSummaryImageUrlList().size()==1) {//新闻：一张图片
            return TYPE_DEFAULT;
        } else {//新闻：多张图片---三张图片
            return TYPE_PHOTO_ITEM;
        }
        */
        //-----
        if (mIsShowFooter && isFooterPosition(position)) {//列表底部
            return TYPE_FOOTER;
        } else {//非底部，正常的item：分为广告类和新闻类
            NewsBaseData newsBaseData = mList.get(position);
            switch (newsBaseData.getDataType()) {
                case NewsBaseData.TYPE_NEWS://新闻类
                    if (newsBaseData.getmNewsBean().getSummaryImageUrlList().size() == 1) {//新闻一张图
                        return TYPE_NEWS_IMAGE_ONE;
                    } else {//新闻多张图
                        return TYPE_NEWS_IMAGE_MULTI;
                    }
                case NewsBaseData.TYPE_AD://广告类

                    NewsBaseDataBean newsBean = newsBaseData.getmNewsBean();
                    if (newsBean instanceof AdAdxBean) {
                        return TYPE_ADBANNER_FROM_ADX_SDK;//banner-来自ADX-SDK的广告
                    }

                    if (TextUtils.isEmpty(newsBean.getSummaryTitle())) {//banner-无图
                        return TYPE_AD_BANNER_BIG_IMAGE_NO_TITLE;
                    } else {//banner-有图
                        return TYPE_AD_BANNER_BIG_IMAGE_HAS_TITLE;
                    }

                default:
                    return TYPE_DEFAULT;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        //获取根部局
        View view;
        switch (viewType) {
            case TYPE_FOOTER:
                view = getView(parent, R.layout.item_news_footer);
                return new FooterViewHolder(view);
            case TYPE_NEWS_IMAGE_ONE://新闻一张图
                view = getView(parent, R.layout.item_news);
                final ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                setItemOnClickEvent(itemViewHolder, false);
                return itemViewHolder;
            case TYPE_NEWS_IMAGE_MULTI://新闻多张图
                view = getView(parent, R.layout.item_news_photo);
                final PhotoViewHolder photoViewHolder = new PhotoViewHolder(view);
                setItemOnClickEvent(photoViewHolder, true);
                return photoViewHolder;
            case TYPE_AD_BANNER_BIG_IMAGE_NO_TITLE://banner大图广告无标题
                view = getView(parent, R.layout.banner_big_image_no_title);
                final AdBannerNoTitleViewHolder adBannerNoTitleViewHolder = new AdBannerNoTitleViewHolder(view);
                setItemOnClickEvent(adBannerNoTitleViewHolder, true);
                return adBannerNoTitleViewHolder;
            case TYPE_AD_BANNER_BIG_IMAGE_HAS_TITLE://banner大图广告有标题

                return null;

            case TYPE_ADBANNER_FROM_ADX_SDK://banner广告来源ADX-SDK
                view = getView(parent, R.layout.item_banner_adx);
                return new AdBannerAdxViewHolder(view);

            default:
                throw new RuntimeException("there is no type that matches the type " +
                        viewType + " + make sure your using types correctly");
        }

    }

    private void setItemOnClickEvent(final RecyclerView.ViewHolder holder, final boolean isPhoto) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OnNewsListItemClickListener) mOnItemClickListener).onItemClick(v, holder.getLayoutPosition(),
                            isPhoto);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        setValues(holder, position);
        setItemAppearAnimation(holder, position, R.anim.anim_bottom_in);
    }

    private void setValues(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            setItemValues((ItemViewHolder) holder, position);
        } else if (holder instanceof PhotoViewHolder) {
            setPhotoItemValues((PhotoViewHolder) holder, position);
        } else if (holder instanceof AdBannerNoTitleViewHolder) {
            setAdBannerBigImageNoTitleVaules((AdBannerNoTitleViewHolder) holder, position);
        } else if (holder instanceof AdBannerAdxViewHolder) {
            setAdBannerAdxValues((AdBannerAdxViewHolder) holder, position);
        }

    }

    private void setAdBannerAdxValues(AdBannerAdxViewHolder holder, int position) {
        NewsBaseData newsBaseData = mList.get(position);
        NewsBaseDataBean newsBaseDataBean = newsBaseData.getmNewsBean();
        if (newsBaseDataBean instanceof AdAdxBean) {
            holder.adxContainer.removeAllViews();
            KBannerAdUtil.addKBannerAdView(holder.adxContainer.getContext(),null,holder.itemView,holder.adxContainer);
        }
    }
    //设置普通一张图的item值
    private void setItemValues(ItemViewHolder holder, int position) {

        NewsBaseData newsBaseData = mList.get(position);

        NewsBaseDataBean newsBaseDataBean = newsBaseData.getmNewsBean();

        String summaryTitle = newsBaseDataBean.getSummaryTitle();
        String summaryPublishTime = newsBaseDataBean.getSummaryPublishTime();

        String summaryImageUrl = null;
        if (CollectionsUtils.collectionIsNull(newsBaseDataBean.getSummaryImageUrlList())) {
            summaryImageUrl = "默认图片地址";
        } else {
//            summaryImageUrl = newsBaseDataBean.getSummaryImageUrlList().get(0);
            summaryImageUrl = newsBaseDataBean.getSummaryImageUrlList().get(0);
        }

        holder.mNewsSummaryPtimeTv.setText(summaryPublishTime);
        holder.mNewsSummaryTitleTv.setText(summaryTitle);

        Glide.with(holder.mNewsSummaryPhotoIv.getContext()).load(summaryImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.image_place_holder)
                .fitCenter()
                .error(R.drawable.ic_load_fail)
                .into(holder.mNewsSummaryPhotoIv);

    }
    private void setAdBannerBigImageNoTitleVaules(AdBannerNoTitleViewHolder adBannerNoTitleViewHolder, int position) {
        NewsBaseData newsBaseData = mList.get(position);
        int contentProvider = newsBaseData.getContentProvider();
        switch (contentProvider) {
            case KlauncherAdvertisement.contentProvider:
                KlauncherAdvertisement klauncherAdvertisement = (KlauncherAdvertisement) newsBaseData;

                Glide.with(adBannerNoTitleViewHolder.mImageView.getContext()).load(klauncherAdvertisement.mNewsBean.getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.color.image_place_holder)
                        .fitCenter()
                        .error(R.drawable.ic_load_fail)
                        .into(adBannerNoTitleViewHolder.mImageView);

                break;
        }
    }


    //设置三张图的item
    private void setPhotoItemValues(PhotoViewHolder holder, int position) {
        //获取holder要绑定的数据
        NewsBaseData newsBaseData = mList.get(position);
        //绑定holder与Text
        setTextView(holder, newsBaseData);
        //绑定holder与Image
        setImageView(holder, newsBaseData);
    }

    private void setTextView(PhotoViewHolder holder, NewsBaseData newsBaseData) {

        NewsBaseDataBean newsBaseDataBean = newsBaseData.getmNewsBean();
        String title = newsBaseDataBean.getSummaryTitle();
        String ptime = newsBaseDataBean.getSummaryPublishTime();

        holder.mNewsSummaryTitleTv.setText(title);
        holder.mNewsSummaryPtimeTv.setText(ptime);
    }

    private void setImageView(PhotoViewHolder holder, NewsBaseData newsBaseData) {
        int PhotoThreeHeight = (int) DimenUtil.dp2px(90);
        int PhotoTwoHeight = (int) DimenUtil.dp2px(120);
        int PhotoOneHeight = (int) DimenUtil.dp2px(150);

        String imgSrcLeft = null;
        String imgSrcMiddle = null;
        String imgSrcRight = null;

        ViewGroup.LayoutParams layoutParams = holder.mNewsSummaryPhotoIvGroup.getLayoutParams();

        if (!newsBaseData.getmNewsBean().isNull()) {//有真实的新闻数据

            //获取新闻实体bean
            List<String> imgUrlList = newsBaseData.getmNewsBean().getSummaryImageUrlList();
            int size = imgUrlList.size();

            if (size >= 3) {
                imgSrcLeft = imgUrlList.get(0);
                imgSrcMiddle = imgUrlList.get(1);
                imgSrcRight = imgUrlList.get(2);

                layoutParams.height = PhotoThreeHeight;

                holder.mNewsSummaryTitleTv.setText(ValuesHelper.getString(
                        R.string.photo_collections,
                        newsBaseData.getmNewsBean().getSummaryTitle()
                ));

            } else if (size >= 2) {
                imgSrcLeft = imgUrlList.get(0);
                imgSrcMiddle = imgUrlList.get(1);

                layoutParams.height = PhotoTwoHeight;

            } else if (size >= 1) {
                imgSrcLeft = imgUrlList.get(0);

                layoutParams.height = PhotoOneHeight;

            }
        } else {//没有真实的新闻数据


        }

        setPhotoImageView(holder, imgSrcLeft, imgSrcMiddle, imgSrcRight);
        holder.mNewsSummaryPhotoIvGroup.setLayoutParams(layoutParams);

    }


    private void setPhotoImageView(PhotoViewHolder holder, String imgSrcLeft, String imgSrcMiddle, String imgSrcRight) {
        if (imgSrcLeft != null) {
            showAndSetPhoto(holder.mNewsSummaryPhotoIvLeft, imgSrcLeft);
        } else {
            hidePhoto(holder.mNewsSummaryPhotoIvLeft);
        }

        if (imgSrcMiddle != null) {
            showAndSetPhoto(holder.mNewsSummaryPhotoIvMiddle, imgSrcMiddle);
        } else {
            hidePhoto(holder.mNewsSummaryPhotoIvMiddle);
        }

        if (imgSrcRight != null) {
            showAndSetPhoto(holder.mNewsSummaryPhotoIvRight, imgSrcRight);
        } else {
            hidePhoto(holder.mNewsSummaryPhotoIvRight);
        }
    }
    private void showAndSetPhoto(ImageView imageView, String imgSrc) {
        imageView.setVisibility(View.VISIBLE);
        Glide.with(imageView.getContext())
                .load(imgSrc)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.image_place_holder)
                .fitCenter()
                .error(R.drawable.ic_load_fail)
                .into(imageView);
    }

    private void hidePhoto(ImageView imageView) {
        imageView.setVisibility(View.GONE);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (isShowingAnimation(holder)) {
            holder.itemView.clearAnimation();
        }
    }

    private boolean isShowingAnimation(RecyclerView.ViewHolder holder) {
        return holder.itemView.getAnimation() != null && holder.itemView
                .getAnimation().hasStarted();
    }


    /**
     * 新闻一张图
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.news_summary_photo_iv)
        ImageView mNewsSummaryPhotoIv;

        @BindView(R2.id.news_summary_title_tv)
        TextView mNewsSummaryTitleTv;

        @BindView(R2.id.news_summary_ptime_tv)
        TextView mNewsSummaryPtimeTv;

        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * 新闻多张图
     */
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.news_summary_title_tv)
        TextView mNewsSummaryTitleTv;
        @BindView(R2.id.news_summary_photo_iv_group)
        LinearLayout mNewsSummaryPhotoIvGroup;
        @BindView(R2.id.news_summary_photo_iv_left)
        ImageView mNewsSummaryPhotoIvLeft;
        @BindView(R2.id.news_summary_photo_iv_middle)
        ImageView mNewsSummaryPhotoIvMiddle;
        @BindView(R2.id.news_summary_photo_iv_right)
        ImageView mNewsSummaryPhotoIvRight;
        @BindView(R2.id.news_summary_ptime_tv)
        TextView mNewsSummaryPtimeTv;

        public PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    static class AdBannerNoTitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.banner_big_image_no_title)
        ImageView mImageView;

        public AdBannerNoTitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class AdBannerAdxViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.adx_container)
        RelativeLayout adxContainer;

        public AdBannerAdxViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
