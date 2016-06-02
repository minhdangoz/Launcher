package com.klauncher.kinflow.cards.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.klauncher.kinflow.cards.manager.YDCardContentManager;
import com.klauncher.kinflow.cards.manager.YMCardContentManager;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.common.task.YiDianTask;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanni on 16/4/10.
 */
public class CardViewHolder extends RecyclerView.ViewHolder {
    public ViewGroup cardView;

    public CardViewHolder(View itemView) {
        super(itemView);
        cardView = (ViewGroup) itemView;
    }

    public ViewGroup getCardView() {
        return cardView;
    }
}

class WifiCardViewHolder extends CardViewHolder {
    public View wifiLayout;

    public WifiCardViewHolder(View itemView) {
        super(itemView);
        wifiLayout = itemView.findViewById(R.id.wifi_layout);
    }
}

/**
 * adView的ViewHolder
 */
/*

class AdnativeCardViewHolder extends CardViewHolder implements View.OnClickListener{

    private Context mContext;
    private CardInfo mCardInfo;
    private NativeAdInfo mFirstNativeAdInfo;
    public View mNativeGroup;
    public ImageView mNativeImage;
    public TextView mNativeTitle;

    public AdnativeCardViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        mNativeGroup = itemView.findViewById(R.id.native_layout);
        mNativeImage = (ImageView) itemView.findViewById(R.id.native_iv_icon);
        mNativeTitle = (TextView) itemView.findViewById(R.id.native_tv_title);
    }

    public View getNativeGroup() {
        return mNativeGroup;
    }

    public void setCardInfo(CardInfo cardInfo) {
        try{
            this.mCardInfo = cardInfo;
        ADVCardContentManager manager = (ADVCardContentManager) this.mCardInfo.getmCardContentManager();
        List<NativeAdInfo> nativeAdInfoList = manager.getNativeAdInfoList();
        if (null == nativeAdInfoList || nativeAdInfoList.size() == 0) {//获取数据失败
            Log.i("MyInfo", "setCardInfo: AdView的CardInfo对应的Manager存储的数据为空");
            ViewGroup.LayoutParams params = cardView.getLayoutParams();
            params.height = 0;
            cardView.setLayoutParams(params);
            ViewParent parent = cardView.getParent();
            if (parent != null) {
                parent.requestLayout();
            }
            return;
        }
        //获取数据成功
         this.mFirstNativeAdInfo = nativeAdInfoList.get(0);
        this.mNativeTitle.setText(mFirstNativeAdInfo.getTitle());
        Picasso.with(mContext).load(mFirstNativeAdInfo.getIconUrl()).into(this.mNativeImage);
        }catch(Exception e) {
              Log.i("Kinflow", "给adview添加数据的时候出错:AdnativeCardViewHolder.setCardInfo");
        }

    }

    @Override
    public void onClick(View v) {
        mFirstNativeAdInfo.onClick(getNativeGroup());
        //String openType,String pullUpAppPackageName,String fromName
        PingManager.getInstance().reportUserAction4Banner("adview_sdk", BuildConfig.APPLICATION_ID,"adview");
    }
}
*/


/**
 * yokmob的CardView
 */
class AdbannerCardViewHolder extends CardViewHolder implements View.OnClickListener {
    private Context mContext;
    private CardInfo mCardInfo;//data
    public ImageView bannerImage;//view

    public AdbannerCardViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        bannerImage = (ImageView) itemView.findViewById(R.id.banner_iv_image);
    }

    public ImageView getBannerImage() {
        return bannerImage;
    }

    public void setCardInfo(CardInfo cardInfo) {
        try{
            this.mCardInfo = cardInfo;
            YMCardContentManager mManager = (YMCardContentManager) mCardInfo.getmCardContentManager();
            Picasso.with(mContext).load(mManager.getImageUrl()).into(bannerImage);
        }catch (Exception e) {
            Log.i("Kinflow", "给yokmob添加数据的时候出错:AdbannerCardViewHolder.setCardInfo");
        }

    }

    @Override
    public void onClick(View v) {
        try {
            YMCardContentManager mManager = (YMCardContentManager) mCardInfo.getmCardContentManager();
//        KinflowBrower.openUrl(mContext,mManager.getClickUrl());
            Bundle extras = new Bundle();
            extras.putString(OpenMode.OPEN_URL_KEY, mManager.getClickUrl());
            this.mCardInfo.open(mContext, extras);
        } catch (Exception e) {
            Log.e("Kinflow", "onClick: yokmob时出错");
        }
    }
}
/*
class TTCardViewHolder extends CardViewHolder implements View.OnClickListener {

    Context mContext;
    CardInfo mCardInfo;//card_base_data
    Article mFirstArticle;
    LinearLayout mTouTiaoFirstNewsLayout;
    RecyclerView mRecyclerView;
    TextView mFirstNewsTitle;
    TextView mFirstNewsAbstract;
    RelativeLayout mFooterLayout;
    TextView mMoreNews;
    TextView mChangeNews;
    List<Article>[] mArticleListArrays;
    boolean isGroup2;


    public TTCardViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        initTouTiaoCardLayout();
    }

    public LinearLayout getTouTiaoFirstNewsLayout() {
        return mTouTiaoFirstNewsLayout;
    }

    public TextView getChangeNews() {
        return mChangeNews;
    }

    public TextView getMoreNews() {
        return mMoreNews;
    }

    private void initTouTiaoCardLayout() {
        mRecyclerView = (RecyclerView) cardView.findViewById(R.id.card_news_list_toutiao);

        mTouTiaoFirstNewsLayout = (LinearLayout) cardView.findViewById(R.id.first_news_layout_toutiao);
        mFirstNewsTitle = (TextView) mTouTiaoFirstNewsLayout.findViewById(R.id.first_toutiao_title);
        mFirstNewsAbstract = (TextView) mTouTiaoFirstNewsLayout.findViewById(R.id.first_toutiao_abstract);
        mFooterLayout = (RelativeLayout) cardView.findViewById(R.id.card_footer);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mMoreNews = (TextView) mFooterLayout.findViewById(R.id.more_news);
        mChangeNews = (TextView) mFooterLayout.findViewById(R.id.change_news);
        //initData
        mChangeNews.setVisibility(View.INVISIBLE);
        TTNewsAdapter adapter = new TTNewsAdapter(null, mContext, mCardInfo);
        mRecyclerView.setAdapter(adapter);
    }

    public void setmCardInfo(CardInfo cardInfo) {
        this.mCardInfo = cardInfo;
        mMoreNews.setText(this.mCardInfo.getCardFooter());
        TTCardContentManager ttCardContentManager = (TTCardContentManager) this.mCardInfo.getmCardContentManager();
        mArticleListArrays = ttCardContentManager.getArticleListArrays();
        if (null == mArticleListArrays) {//第一次从assets中读取到的CardInfo对应Manager没有数据
            return;
        }
        List<Article> group1 = mArticleListArrays[0];
        List<Article> group2 = mArticleListArrays[1];
        if (null == group1) {
            //隐藏
            return;
        }
        if (null != group2 && group2.size() > 0) {//获取到了两组数据:group1,group2
            mChangeNews.setVisibility(View.VISIBLE);
        } else {//获取到了一组数据:group1
            mChangeNews.setVisibility(View.INVISIBLE);
        }

        mMoreNews.setText("更多头条新闻");
        setData(group1, cardInfo);
        isGroup2 = false;
    }

    void setData (List<Article> group,CardInfo cardInfo){
        //今日头条新闻的4条非abstract新闻
        TTNewsAdapter adapter = (TTNewsAdapter) mRecyclerView.getAdapter();
        adapter.updateAdapter(group.subList(1,group.size()), cardInfo);
        //今日头条的第一条新闻,abstract新闻
        //第一条新闻
        mFirstArticle = group.get(0);
        mFirstNewsTitle.setText(mFirstArticle.mTitle);
        mFirstNewsAbstract.setText(mFirstArticle.mAbstract);
    }

    @Override
    public void onClick(View v) {
    try {
            switch (v.getId()) {
            case R.id.more_news:
                String openPackageName = "";
                boolean b = CommonUtils.getInstance().isInstalledAPK(mContext,OpenMode.COMPONENT_NAME_JINRI_TOUTIAO);
                if (b) {
//                    ComponentName componentName = new ComponentName(Const.TOUTIAO_packageName, Const.TOUTIAO_mainActivity);
//                    Intent intent = new Intent();
//                    intent.setComponent(componentName);
//                    mContext.startActivity(intent);
                    CommonUtils.getInstance().openApp(mContext,OpenMode.COMPONENT_NAME_JINRI_TOUTIAO);
                    openPackageName = Const.TOUTIAO_packageName;
                } else {
                    openPackageName = CommonUtils.getInstance().openHotWord(mContext, "http://m.toutiao.com/");
                }
                //String openType,String pullUpAppPackageName,String fromName
                PingManager.getInstance().reportUserAction4CardMore(openPackageName,"jinRiTouTiao");
                break;
            case R.id.change_news:
                if (null == mArticleListArrays) {//第一次从assets中读取到的CardInfo对应Manager没有数据
                    Toast.makeText(mContext, "没更多了,请刷新", Toast.LENGTH_SHORT).show();
                }else {
                    List<Article> group1 = mArticleListArrays[0];
                    List<Article> group2 = mArticleListArrays[1];
                    if (null==group2||group2.size()==0) {
                        Toast.makeText(mContext, "没更多了,请刷新", Toast.LENGTH_SHORT).show();
                    }else {
                        if (isGroup2) {//第二组
                            setData(group1,mCardInfo);
                            isGroup2 = false;
                        }else {//第一组
                            setData(group2,mCardInfo);
                            isGroup2 = true;
                        }
                    }
                }
                PingManager.getInstance().reportUserAction4Changes("jinRiTouTiao",String.valueOf(mCardInfo.getCardSecondTypeId()));
                break;
            case R.id.first_news_layout_toutiao:
                if (null == mFirstArticle) return;
                Bundle extras = new Bundle();
                extras.putString(OpenMode.OPEN_URL_KEY,mFirstArticle.mSrcUrl);
                extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL + mFirstArticle.mGroupId);
                mCardInfo.open(mContext, extras);
                PingManager.getInstance().reportUserAction4cardNewsOpen("jinRiTouTiao","301");
                break;
        }
        } catch (Exception e) {
            Log.e("Kinflow", "onClick: 今日头条时出错");
        }

    }

    */

class YDCardViewHolder extends CardViewHolder implements OnClickListener {
    Context mContext;
    View mYiDianCardRoot;//view
    CardInfo mCardInfo;//card_base_data
    List<YiDianModel> mYiDianModelList = new ArrayList<>();//card_internal_data
    YiDianModel mFirstYiDianModel;
    LinearLayout mYiDianHeadWith1ImageLayout;
    RelativeLayout mYiDianHeadWith3ImageLayout;
    RecyclerView mRecyclerView;
    TextView tvMoreNews;
    TextView tvChangeNews;

    public YDCardViewHolder(View itemView, Context context) {
        super(itemView);
        mYiDianCardRoot = itemView;
        this.mContext = context;
        initYiDianCardLayout();
    }

    private void initYiDianCardLayout() {
        //头部初始化,但是默认为隐藏
        mYiDianHeadWith1ImageLayout = (LinearLayout) mYiDianCardRoot.findViewById(R.id.yidian_head_1image);
        mYiDianHeadWith3ImageLayout = (RelativeLayout) mYiDianCardRoot.findViewById(R.id.yidian_head_3image);
        //中间&&底部
        mRecyclerView = (RecyclerView) mYiDianCardRoot.findViewById(R.id.card_news_list_toutiao);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        tvMoreNews = (TextView) mYiDianCardRoot.findViewById(R.id.more_news);
        tvChangeNews = (TextView) mYiDianCardRoot.findViewById(R.id.change_news);
    }


    /**
     * 初始化数据
     *
     * @param cardInfo
     */
    public void setmCardInfo(CardInfo cardInfo) {
        try {
            //初始化数据
            this.mCardInfo = cardInfo;
            String footerName = mCardInfo.getCardFooter();
            if (TextUtils.isEmpty(footerName)) {
                footerName = "更多新闻";
            } else {
                footerName="更多"+footerName;
            }
            tvMoreNews.setText(footerName);
            YDCardContentManager manager = (YDCardContentManager) cardInfo.getmCardContentManager();
            List<YiDianModel> yiDianModelList = manager.getmYiDianModelList();
            YDNewsAdapter adapter = new YDNewsAdapter(mContext, cardInfo);
            mRecyclerView.setAdapter(adapter);
            addData(adapter, yiDianModelList);
        }catch (Exception e) {
            log("CardViewHolder.setmCardInfo时出错,也就是添加数据时出错");
        }
    }

    private void addData(YDNewsAdapter adapter, List<YiDianModel> yiDianModelList) {
        //第一次从assets中读取到的CardInfo对应Manager没有数据
        if (yiDianModelList.size() > 1) {//初次加载无数据
            //加载非头条数据
            adapter.updateYDnewsRecycler(yiDianModelList.subList(1, yiDianModelList.size()));
            //加载头条数据
            mYiDianHeadWith1ImageLayout.setVisibility(View.GONE);
            mYiDianHeadWith3ImageLayout.setVisibility(View.GONE);
            mFirstYiDianModel = yiDianModelList.get(0);
            if (null != mFirstYiDianModel.getImages())
                switch (mFirstYiDianModel.getImages().length) {
                    case 0:
                    case 1:
                        mYiDianHeadWith1ImageLayout.setVisibility(View.VISIBLE);
                        addYiDianHeadWith1Image();
                        break;
                    case 3:
                        mYiDianHeadWith3ImageLayout.setVisibility(View.VISIBLE);
                        addYiDianHeadWith3Image();
                        break;
                }
        }else {
            log("一点咨询"+mCardInfo.getCardSecondTypeId()+"类型无数据,隐藏layout");
            ViewGroup.LayoutParams params = cardView.getLayoutParams();
            params.height = 0;
            cardView.setLayoutParams(params);
            ViewParent parent = cardView.getParent();
            if (parent != null) {
                parent.requestLayout();
            }
        }
    }

    void addYiDianHeadWith1Image() {
        //布局
        final TextView tv_title = (TextView) mYiDianHeadWith1ImageLayout.findViewById(R.id.yidian_news_title);
        final TextView tv_publishTime = (TextView) mYiDianHeadWith1ImageLayout.findViewById(R.id.yidian_news_publish_time);
        final ImageView iv_yidianImage = (ImageView) mYiDianHeadWith1ImageLayout.findViewById(R.id.yidian_image);
        //数据
        tv_title.setText(mFirstYiDianModel.getTitle());
        tv_publishTime.setText(mFirstYiDianModel.getDate());
        int imageCount = mFirstYiDianModel.getImages().length;
        if (imageCount == 1) {//0张图
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    iv_yidianImage.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            iv_yidianImage.setTag(target);
            Picasso.with(mContext).load(mFirstYiDianModel.getImages()[0]).fit().centerCrop().into(iv_yidianImage);
        } else {//0张图
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            iv_yidianImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image_show));
        }
    }

    void addYiDianHeadWith3Image() {
        final TextView tv_title = (TextView) mYiDianHeadWith3ImageLayout.findViewById(R.id.yidian_news_title);
        final TextView tv_publishTime = (TextView) mYiDianHeadWith3ImageLayout.findViewById(R.id.yidian_news_publish_time);
        final ImageView iv_yidianImageLeft = (ImageView) mYiDianHeadWith3ImageLayout.findViewById(R.id.yidian_image_left);
        final ImageView iv_yidianImageMiddle = (ImageView) mYiDianHeadWith3ImageLayout.findViewById(R.id.yidian_image_middle);
        final ImageView iv_yidianImageRight = (ImageView) mYiDianHeadWith3ImageLayout.findViewById(R.id.yidian_image_right);
        tv_title.setText(mFirstYiDianModel.getTitle());
        tv_publishTime.setText(mFirstYiDianModel.getDate());
        int length = mFirstYiDianModel.getImages().length;
        for (int i = 0; i < length; i++) {
            final int finalI = i;
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    switch (finalI) {
                        case 0:
                            iv_yidianImageLeft.setImageBitmap(bitmap);
                            break;
                        case 1:
                            iv_yidianImageMiddle.setImageBitmap(bitmap);
                            break;
                        case 2:
                            iv_yidianImageRight.setImageBitmap(bitmap);
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
            switch (finalI) {
                case 0:
                    iv_yidianImageLeft.setTag(target);
                    Picasso.with(mContext).load(mFirstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageLeft);
                    break;
                case 1:
                    iv_yidianImageMiddle.setTag(target);
                    Picasso.with(mContext).load(mFirstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageMiddle);
                    break;
                case 2:
                    iv_yidianImageRight.setTag(target);
                    Picasso.with(mContext).load(mFirstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageRight);
                    break;
            }
        }
    }

    public LinearLayout getmYiDianHeadWith1ImageLayout() {
        return mYiDianHeadWith1ImageLayout;
    }

    public RelativeLayout getmYiDianHeadWith3ImageLayout() {
        return mYiDianHeadWith3ImageLayout;
    }

    public RecyclerView getmRecyclerView() {
        return mRecyclerView;
    }

    public TextView getTvMoreNews() {
        return tvMoreNews;
    }

    public TextView getTvChangeNews() {
        return tvChangeNews;
    }

    public void addCardListener() {

    }

    public void addYiDianModelData(List<YiDianModel> yiDianModelList) {
        this.mYiDianModelList = yiDianModelList;
    }

    @Override
    public void onClick(View v) {
        try {
            Bundle extras = new Bundle();
            switch (v.getId()) {
                case R.id.yidian_head_1image:
                    extras.putString(OpenMode.OPEN_URL_KEY, mFirstYiDianModel.getUrl());
                    String finalOpenComponent_Head1Imapge = mCardInfo.open(mContext, extras);
                    if (!TextUtils.isEmpty(finalOpenComponent_Head1Imapge))
                        PingManager.getInstance().reportUserAction4cardNewsOpen(PingManager.VALUE_CARD_CONTENT_FROM_YIDIANZIXUN, String.valueOf(mCardInfo.getCardSecondTypeId()));
                    break;
                case R.id.yidian_head_3image:
                    extras.putString(OpenMode.OPEN_URL_KEY, mFirstYiDianModel.getUrl());
                    String finalOpenComponent_Head3Imapge = mCardInfo.open(mContext, extras);
                    if (!TextUtils.isEmpty(finalOpenComponent_Head3Imapge))
                        PingManager.getInstance().reportUserAction4cardNewsOpen(PingManager.VALUE_CARD_CONTENT_FROM_YIDIANZIXUN, String.valueOf(mCardInfo.getCardSecondTypeId()));
                    break;
                case R.id.more_news:
                    String url = "";
                    String cardExtra = mCardInfo.getCardExtra();
                    try {
                        JSONObject extraJson = new JSONObject(cardExtra);
                        url = extraJson.optString("clc", Const.YI_DIAN_CHANNEL_MORE_JingXuan);
                    } catch (Exception e) {
                        url = Const.YI_DIAN_CHANNEL_MORE_JingXuan;
                        log("解析一点资讯Card的extra出错:"+e.getMessage());
                    }
                    extras.putString(OpenMode.OPEN_URL_KEY, url);
                    String finalOpenComponent_moreNews =  mCardInfo.open(mContext, extras);
                    if (!TextUtils.isEmpty(finalOpenComponent_moreNews))
                        PingManager.getInstance().reportUserAction4CardMore(finalOpenComponent_moreNews, PingManager.VALUE_CARD_CONTENT_FROM_YIDIANZIXUN);
                    break;
                case R.id.change_news:
                    if (null != mCardInfo) {
                        YDCardContentManager ydCardContentManager = (YDCardContentManager) mCardInfo.getmCardContentManager();
                        try {
                            new YiDianTask(mContext, ydCardContentManager.getmOurDefineChannelId(), new YiDianTask.YiDianQuestCallBack() {
                                @Override
                                public void onError(int errorCode) {

                                }

                                @Override
                                public void onSuccess(List<YiDianModel> yiDianModelList) {
                                    ((YDCardContentManager) mCardInfo.getmCardContentManager()).setYiDianModelList(yiDianModelList);
//                                int position = YDCardViewHolder.this.getAdapterPosition();
                                    YDCardViewHolder.this.setmCardInfo(mCardInfo);
//                                YDCardViewHolder.this.addData((YDNewsAdapter) mRecyclerView.getAdapter(), yiDianModelList);
                                }
                            }).run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                    }
                    PingManager.getInstance().reportUserAction4Changes(PingManager.VALUE_CARD_CONTENT_FROM_YIDIANZIXUN,String.valueOf(mCardInfo.getCardSecondTypeId()));
                    break;
            }
        } catch (Exception e) {
            Log.e("Kinflow", "onClick: 一点咨询时出错");
        }
    }

    final protected static void log(String msg) {
        KinflowLog.e(msg);
    }
}