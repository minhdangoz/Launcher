package com.klauncher.kinflow.views.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.control.AmapSkipManager;
import com.klauncher.kinflow.cards.model.sougou.SougouSearchArticle;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.common.task.JRTTAsynchronousTask;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.utilities.KBannerAdUtil;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewAdapter;
import com.klauncher.kinflow.views.recyclerView.data.AdxSdkBanner;
import com.klauncher.kinflow.views.recyclerView.data.BaseRecyclerViewAdapterData;
import com.klauncher.kinflow.views.recyclerView.data.YokmobBanner;
import com.klauncher.kinflow.views.recyclerView.viewHolder.BaseRecyclerViewHolder;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by xixionghui on 16/9/21.
 */
public class Kinflow2NewsAdapter2 extends BaseRecyclerViewAdapter<BaseRecyclerViewAdapterData, BaseRecyclerViewHolder<BaseRecyclerViewAdapterData>> {

    AmapSkipManager amapSkipManager = new AmapSkipManager(mContext);//高德跳转管理器

    /**
     * 决定ITEM类型
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        try {
            return mElementList.get(position).getKinflowConentType();
        } catch (Exception e) {
            return -999;//此处应该加入默认adapter,holder
        }
    }

    /**
     * 构造方法
     * 此方法需要在子类中实现
     *
     * @param context
     * @param elementList
     */
    public Kinflow2NewsAdapter2(Context context, List<BaseRecyclerViewAdapterData> elementList) {
        super(context, elementList);
    }

    @Override
    public BaseRecyclerViewHolder<BaseRecyclerViewAdapterData> onCreateViewHolder(ViewGroup parent, int viewType) {
        try {
            View itemRootView = null;
            if (viewType == BaseRecyclerViewAdapterData.TYPE_NEWS_JINRITOUTIAO) {
                itemRootView = LayoutInflater.from(mContext).inflate(R.layout.kinflow2_news_item_layout,parent,false);
                return new JrttNewsApdaterViewHolder(itemRootView);
            } else if (viewType == BaseRecyclerViewAdapterData.TYPE_NEWS_SOUGOU) {
                itemRootView = LayoutInflater.from(mContext).inflate(R.layout.kinflow2_news_item_layout,parent,false);
                return new SouGouNewsApdaterViewHolder(itemRootView);

            } else if (viewType == BaseRecyclerViewAdapterData.TYPE_BANNER) {
                itemRootView = LayoutInflater.from(mContext).inflate(R.layout.kinflow_banner_big_image, parent, false);
                return new BannerAdaperHolder(itemRootView);
            }
            else if(viewType == BaseRecyclerViewAdapterData.TYPE_ADX_BANNER){ //来自ADX-SDK的广告
                itemRootView = LayoutInflater.from(mContext).inflate(R.layout.item_banner_adx, parent, false);
                return new AdBannerAdxViewHolder(itemRootView);
            }
            else {
                KinflowLog.i("未知的数据类型");
                return null;
            }
        } catch (Exception e) {
            KinflowLog.w("Kinflow2NewsAdapter2.onCreateViewHolder时出错:"+e.getMessage());
            return null;
        }

    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder<BaseRecyclerViewAdapterData> holder, int position) {
        //是否需要根绝type类型判断,然后再绑定数据
        try {
            holder.bundData2View(mElementList.get(position));
        } catch (Exception e) {
            KinflowLog.w("Kinflow2NewsAdapter2.onBindViewHolder时出错:"+e.getMessage());
        }

    }

    //==============================================================================================================================

    public class BannerAdaperHolder extends BaseRecyclerViewHolder<BaseRecyclerViewAdapterData>{
        public ImageView bannerImage;//view

        public BannerAdaperHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemRootView) {
            try {
                bannerImage = (ImageView) itemView.findViewById(R.id.banner_iv_image);
                itemRootView.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        //区分不同的banner大图
                        YokmobBanner yokmobBanner = (YokmobBanner) mElementList.get(getPosition());
                        KinflowLog.e("被点击的yokmob信息如下:\n"+yokmobBanner.toString());

                        try {
                            String clickType = "";
                            String imageUrl = "";
                            switch (yokmobBanner.getBannerType()) {
                                case YokmobBanner.BANNER_TYPE_GAODE:
                                    Uri arroundPoi_yuLe = amapSkipManager.getArroundPoi("娱乐");
                                    amapSkipManager.skip2Amap(arroundPoi_yuLe);
                                    clickType = PingManager.VALUE_BIG_IMAGE_CLICK_TYPE_GAODE_YULE;
                                    imageUrl = arroundPoi_yuLe.toString();
                                    break;
                                case YokmobBanner.BANNER_TYPE_COMMON:
                                    yokmobBanner.openByOrder(mContext);
                                    clickType = PingManager.VALUE_BIG_IMAGE_CLICK_TYPE_YOKMOB;
                                    imageUrl = yokmobBanner.getImageUrl();
                                    break;
                                default:
                                    break;
                            }
                            PingManager.getInstance().reportUserAction4BigImage(clickType,imageUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void bundData2View(BaseRecyclerViewAdapterData modelData) {
            try {
                YokmobBanner yokmobBanner = (YokmobBanner) modelData;
                Picasso.with(mContext).load(yokmobBanner.getImageUrl()).into(bannerImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public  class AdBannerAdxViewHolder extends  BaseRecyclerViewHolder<BaseRecyclerViewAdapterData> {

        RelativeLayout adxContainer;

        public AdBannerAdxViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemRootView) {
//            adxContainer= (RelativeLayout) itemRootView.findViewById(R.id.adx_container);
            adxContainer= (RelativeLayout) itemRootView;
        }

        @Override
        public void bundData2View(BaseRecyclerViewAdapterData modelData) {

            if(modelData instanceof AdxSdkBanner){
                adxContainer.removeAllViews();
                KBannerAdUtil.addKBannerAdView(mContext,null,adxContainer);

            }
        }
    }

    /**
     * 今日头条新闻adapter
     */
    public class JrttNewsApdaterViewHolder extends BaseRecyclerViewHolder<BaseRecyclerViewAdapterData> {
        //4个根布局:大图,三图,0图,1图
        RelativeLayout m0ImageLayout,m3ImageLayout;
        LinearLayout m1ImageLayout,mBigImageLayout;
        //0Image
        TextView m0ImageFooter,m0ImageTitle;
        //bigImage
        TextView mBigNewsHeader,mBigNewsFooter;
        ImageView mBigImage;
        //1Image
        TextView m1ImageFooter,m1ImageTitle;
        ImageView m1ImageView;
        //3Image
        TextView m3ImageHeader,m3ImageFooter;
        ImageView m3ImageLeft,m3ImageMiddle,m3ImageRight;


        public JrttNewsApdaterViewHolder(View itemView) {
            super(itemView);
        }

        public static final String TAG = "Kinflow";
        @Override
        public void initView(View itemRootView) {
            try {
                Log.e(TAG, "initView: ");
                //无图
                m0ImageLayout = (RelativeLayout) itemRootView.findViewById(R.id.kinflow_news_0image);
                m0ImageFooter = (TextView) m0ImageLayout.findViewById(R.id.yidian_news_publish_time);
                m0ImageTitle = (TextView) m0ImageLayout.findViewById(R.id.yidian_news_title);
                m0ImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    Toast.makeText(mContext, "无图被点击", Toast.LENGTH_SHORT).show();
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle) mElementList.get(getPosition());
                        String finalOpenComponent_Head0Imapge =  jinRiTouTiaoArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head0Imapge))
                        PingManager.getInstance().reportUserAction4NewsOpen(
                                PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO,
                                String.valueOf(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN),
                                finalOpenComponent_Head0Imapge,
                                getPosition()
                        );

                        try {
                            new JRTTAsynchronousTask(mContext).reportUserAppLog(jinRiTouTiaoArticle.getGroup_id());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                //大图
                mBigImageLayout = (LinearLayout) itemRootView.findViewById(R.id.kinflow_big_image);
                mBigNewsHeader = (TextView) mBigImageLayout.findViewById(R.id.yidian_news_title);
                mBigNewsFooter = (TextView) mBigImageLayout.findViewById(R.id.yidian_news_publish_time);
                mBigImage = (ImageView) mBigImageLayout.findViewById(R.id.big_image);
                mBigImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    Toast.makeText(mContext, "大图布局被点击", Toast.LENGTH_SHORT).show();
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle)mElementList.get(getPosition());
                        String finalOpenComponent_Head3Imapge = jinRiTouTiaoArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head3Imapge))
                            PingManager.getInstance().reportUserAction4NewsOpen(
                                    PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO,
                                    String.valueOf(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN),
                                    finalOpenComponent_Head3Imapge,
                                    getPosition()
                            );
                        try {
                            new JRTTAsynchronousTask(mContext).reportUserAppLog(jinRiTouTiaoArticle.getGroup_id());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                //一张图片
                m1ImageLayout = (LinearLayout) itemRootView.findViewById(R.id.kinflow_news_1image);
                m1ImageFooter = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_publish_time);
                m1ImageTitle = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_title);
                m1ImageView = (ImageView) m1ImageLayout.findViewById(R.id.yidian_image);
                m1ImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    Toast.makeText(mContext, "一张图被点击", Toast.LENGTH_SHORT).show();
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle)mElementList.get(getPosition());
                        String finalOpenComponent_Head1Imapge = jinRiTouTiaoArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head1Imapge))
                            PingManager.getInstance().reportUserAction4NewsOpen(
                                    PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO,
                                    String.valueOf(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN),
                                    finalOpenComponent_Head1Imapge,
                                    getPosition()
                            );

                        try {
                            new JRTTAsynchronousTask(mContext).reportUserAppLog(jinRiTouTiaoArticle.getGroup_id());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
    //                    Toast.makeText(mContext, "三张图被点击", Toast.LENGTH_SHORT).show();
                        JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle)mElementList.get(getPosition());
                        String finalOpenComponent_Head3Imapge = jinRiTouTiaoArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head3Imapge))
                            PingManager.getInstance().reportUserAction4NewsOpen(
                                    PingManager.VALUE_CARD_CONTENT_FROM_JINRITOUTIAO,
                                    String.valueOf(CardIdMap.CARD_TYPE_NEWS_TT_REDIAN),
                                    finalOpenComponent_Head3Imapge,
                                    getPosition()
                            );
                        try {
                            new JRTTAsynchronousTask(mContext).reportUserAppLog(jinRiTouTiaoArticle.getGroup_id());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                KinflowLog.w("初始化今日头条的的JrttNewsApdaterViewHolder时,出错.:"+e.getMessage());
            }

        }

        @Override
        public void bundData2View(BaseRecyclerViewAdapterData modelData) {

            try {
                //Kinflow2://图片展示的优 先 级 顺 序 为 large_image_list> image_list> middle_image>无图
                JinRiTouTiaoArticle jinRiTouTiaoArticle = (JinRiTouTiaoArticle) modelData;
                switch (jinRiTouTiaoArticle.getImageType()) {
                    case JinRiTouTiaoArticle.IMAGE_TYPE_LARGE_IMAGE:
                        mBigImageLayout.setVisibility(View.VISIBLE);
                        addJrttHeadWithLargeImage(jinRiTouTiaoArticle);
                        break;
                    case JinRiTouTiaoArticle.IMAGE_TYPE_3_IMAGE:
                        m3ImageLayout.setVisibility(View.VISIBLE);
                        addJrttHeadWith3Image(jinRiTouTiaoArticle);
                        break;
                    case JinRiTouTiaoArticle.IMAGE_TYPE_1_IMAGE:
                        m1ImageLayout.setVisibility(View.VISIBLE);
                        addJrttHeadWith1Image(jinRiTouTiaoArticle);
                        break;
                    case JinRiTouTiaoArticle.IMAGE_TYPE_0_IMAGE:
                        m0ImageLayout.setVisibility(View.VISIBLE);
                        addJrttHeadWith0Image(jinRiTouTiaoArticle);
                        break;
                }
            } catch (Exception e) {
                KinflowLog.w("今日头条JrttNewsApdaterViewHolder的方法bundData2View出错:"+e.getMessage());
            }

        }

        void addJrttHeadWithLargeImage (JinRiTouTiaoArticle mFirstJrttArticle) {
            try {
//                Log.e(TAG, "addJrttHeadWithLargeImage: ===============加载大图====================");
                mBigNewsHeader.setText(mFirstJrttArticle.getTitle());
                mBigNewsFooter.setText(DateUtils.getInstance().second2TimeOrDate(mFirstJrttArticle.getPublish_time()));
                List<JinRiTouTiaoArticle.ImageInfo> largeImageInfoList = mFirstJrttArticle.getLarge_image_list();
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mBigImage.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                mBigImage.setTag(target);
                loadImage(largeImageInfoList.get(0), mBigImage);
            } catch (Exception e) {
                KinflowLog.w("今日头条加载大图时出错:"+e.getMessage());
            }

        }

        void addJrttHeadWith3Image(JinRiTouTiaoArticle mFirstJrttArticle) {

            try {
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
            } catch (Exception e) {
                KinflowLog.w("今日头条加载三张图时出错:"+e.getMessage());
            }

        }

        void addJrttHeadWith1Image(JinRiTouTiaoArticle mFirstJrttArticle) {
            try {
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
            } catch (Exception e) {
                KinflowLog.w("今日头条加载一张图时出错:"+e.getMessage());
            }

        }

        void addJrttHeadWith0Image(JinRiTouTiaoArticle mFirstJrttArticle) {
            try {
                m0ImageTitle.setText(mFirstJrttArticle.getTitle());
                m0ImageFooter.setText(DateUtils.getInstance().second2TimeOrDate(mFirstJrttArticle.getPublish_time()));
            } catch (Exception e) {
                KinflowLog.w("今日头条加载无图时出错:"+e.getMessage());
            }

        }

        void loadImage(JinRiTouTiaoArticle.ImageInfo imageInfo, ImageView toutiaoImage) {
            try {
                List<JinRiTouTiaoArticle.ImageUrl> imageUrlList = imageInfo.getUrl_list();
                if (null != imageUrlList && imageUrlList.size() != 0) {
                    Picasso.with(mContext).load(imageUrlList.get(0).getUrl()).fit().centerCrop().into(toutiaoImage);
                } else {//加载默认
                    toutiaoImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image_show));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 搜狗adapterViewHolder
     */
    public class SouGouNewsApdaterViewHolder extends BaseRecyclerViewHolder<BaseRecyclerViewAdapterData> {
        //4个根布局:大图,三图,0图,1图
        RelativeLayout m0ImageLayout;
        LinearLayout m1ImageLayout;
        //0Image
        TextView m0ImageFooter,m0ImageTitle;
        //1Image
        TextView m1ImageFooter,m1ImageTitle;
        ImageView m1ImageView;


        public SouGouNewsApdaterViewHolder(View itemView) {
            super(itemView);
        }

        public static final String TAG = "Kinflow";
        @Override
        public void initView(View itemRootView) {
            try {
                //无图
                m0ImageLayout = (RelativeLayout) itemRootView.findViewById(R.id.kinflow_news_0image);
                m0ImageFooter = (TextView) m0ImageLayout.findViewById(R.id.yidian_news_publish_time);
                m0ImageTitle = (TextView) m0ImageLayout.findViewById(R.id.yidian_news_title);
                m0ImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    Toast.makeText(mContext, "搜狗搜索新闻的,无图被点击", Toast.LENGTH_SHORT).show();
                        SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) mElementList.get(getPosition());
                        String finalOpenComponent_Head0Imapge =  sougouSearchArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head0Imapge))
                            PingManager.getInstance().reportUserAction4NewsOpen(
                                    PingManager.VALUE_CARD_CONTENT_FROM_SOUGOUSOUSOU,
                                    String.valueOf(CardIdMap.CARD_TYPE_NEWS_SOUGOU_SEARCH_NEWS),
                                    finalOpenComponent_Head0Imapge,
                                    getPosition()
                            );

                    }
                });

                //一张图片
                m1ImageLayout = (LinearLayout) itemRootView.findViewById(R.id.kinflow_news_1image);
                m1ImageFooter = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_publish_time);
                m1ImageTitle = (TextView) m1ImageLayout.findViewById(R.id.yidian_news_title);
                m1ImageView = (ImageView) m1ImageLayout.findViewById(R.id.yidian_image);
                m1ImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
    //                    Toast.makeText(mContext, "搜狗搜索新闻的一张图被点击", Toast.LENGTH_SHORT).show();
                        SougouSearchArticle sougouSearchArticle = (SougouSearchArticle)mElementList.get(getPosition());
                        String finalOpenComponent_Head1Imapge =  sougouSearchArticle.openByOrder(mContext);
                        if (!TextUtils.isEmpty(finalOpenComponent_Head1Imapge))
                            PingManager.getInstance().reportUserAction4NewsOpen(
                                    PingManager.VALUE_CARD_CONTENT_FROM_SOUGOUSOUSOU,
                                    String.valueOf(CardIdMap.CARD_TYPE_NEWS_SOUGOU_SEARCH_NEWS),
                                    finalOpenComponent_Head1Imapge,
                                    getPosition()
                            );
                    }
                });
            } catch (Exception e) {
                KinflowLog.w("搜狗搜索新闻SouGouNewsApdaterViewHolder在初始化initView时出错:"+e.getMessage());
            }
        }

        @Override
        public void bundData2View(BaseRecyclerViewAdapterData modelData) {

            try {
                SougouSearchArticle sougouSearchArticle = (SougouSearchArticle) modelData;
                //如果有图就加载图,如果无图则加载标题
                if (sougouSearchArticle.hasImage()) {//有图模式
                    m1ImageLayout.setVisibility(View.VISIBLE);
                    addSouGouHeadWith1Image(sougouSearchArticle);
                }else {//无图模式
                    m0ImageLayout.setVisibility(View.VISIBLE);
                    addSouGouHeadWith0Image(sougouSearchArticle);
                }
            } catch (Exception e) {
                KinflowLog.w("搜狗搜索新闻SouGouNewsApdaterViewHolder在绑定数据到view时时出错:"+e.getMessage());
            }

        }

        void addSouGouHeadWith1Image(SougouSearchArticle sougouSearchArticle) {
            try {
                //布局
                //数据
                m1ImageTitle.setText(sougouSearchArticle.getTitle());
                m1ImageFooter.setText(DateUtils.getInstance().second2TimeOrDate(sougouSearchArticle.getPage_time()));
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
                Picasso.with(mContext).load(sougouSearchArticle.getImg_list()[0]).fit().centerCrop().into(m1ImageView);
            } catch (Exception e) {
                KinflowLog.w("搜狗搜索新闻SouGouNewsApdaterViewHolder在加载一张图时时出错:"+e.getMessage());
            }

        }

        void addSouGouHeadWith0Image(SougouSearchArticle sougouSearchArticle) {
            try {
                m0ImageTitle.setText(sougouSearchArticle.getTitle());
                m0ImageFooter.setText(DateUtils.getInstance().second2TimeOrDate(sougouSearchArticle.getPage_time()));
            } catch (Exception e) {
                KinflowLog.w("搜狗搜索新闻SouGouNewsApdaterViewHolder在加载无图时时出错:"+e.getMessage());
            }

        }
    }
}
