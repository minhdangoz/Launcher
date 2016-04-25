package com.klauncher.kinflow.cards;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.klauncher.launcher.R;
import com.klauncher.kinflow.browser.KinflowBrower;
import com.klauncher.kinflow.cards.adapter.TTNewsAdapter;
import com.klauncher.kinflow.cards.adapter.YDNewsAdapter;
import com.klauncher.kinflow.cards.control.YiDianContentManager;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.yidian.YiDianModel;
import com.klauncher.kinflow.cards.utils.CardUtils;
import com.klauncher.kinflow.common.task.YiDianTask;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.kyview.interfaces.AdNativeInterface;
import com.kyview.natives.AdNativeManager;
import com.kyview.natives.NativeAdInfo;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.ss.android.sdk.minusscreen.SsNewsApi;
import com.ss.android.sdk.minusscreen.control.feed.ArticleListQueryCallBack;
import com.ss.android.sdk.minusscreen.model.Article;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by yanni on 16/3/24.
 */
public class CardContentManager {
    public static String key_group1 = "group1";
    public static String key_group2 = "group2";
    public static String defalutGroup = key_group1;

    private Activity mContext;
    private List<Article> articleList = new ArrayList<>();
    HashMap<String, LinkedHashSet<Article>> groupArticle = new HashMap<>();

    private View mWifiCard;
    private ViewGroup mCardRoot;

    private BroadcastReceiver mWifiChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            showOrHideWifiCard();
        }
    };

    public CardContentManager(Activity context, ViewGroup cardRoot) {
        mContext = context;
        mCardRoot = cardRoot;
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mWifiChangeReceiver, filter);
    }

    public void loadCardView(CardInfo cardInfo) {
        Log.d("MyInfo", ("cardTypeId=" + cardInfo.getCardTypeId()));
        switch (cardInfo.getCardTypeId()) {
            case CardInfo.CARD_TYPE_AD:
                switch (cardInfo.getCardSecondTypeId()) {
                    case CardInfo.CARD_TYPE_AD_KAPPMOB:
                        getKappmob(mCardRoot, cardInfo);
                        break;
                    case CardInfo.CARD_TYPE_AD_ADVIEW:
                        getAdview(mCardRoot);
                        break;
                }
                break;
            case CardInfo.CARD_TYPE_NEWS:
                defalutGroup = key_group1;

                if (cardInfo.getCardSecondTypeId() < CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN) {//加载今日头条
                    Log.d("MyInfo", "加载今日头条，cardInfo：" + cardInfo.toString());
                    loadTTNewsCard(mCardRoot, cardInfo);
                } else {//加载一点资讯
                    Log.d("MyInfo", "加载一点资讯,cardInfo: " + cardInfo.toString());
                    //发起网络请求，等根据回调结果选择要加载的layout
                    requestYDZZ(mCardRoot, cardInfo);
                }
                break;
            case CardInfo.CARD_TYPE_SETTING:
                initWifiSettingCard(cardInfo);
                showOrHideWifiCard();
                break;
        }
    }

    public void showOrHideWifiCard() {
        if (mCardRoot != null) {
            if (!NetworkUtils.isNetworkAvailable(mContext)) {
                if (mWifiCard.getParent() == null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.topMargin = Dips.dipsToIntPixels(12.0f, mContext);
                    mCardRoot.addView(mWifiCard, 0, params);
                }
            } else {
                if (mWifiCard != null && mCardRoot.getChildAt(0) == mWifiCard) {
                    mCardRoot.removeViewAt(0);
                }
            }
        }
    }

    private void initWifiSettingCard(final CardInfo cardInfo) {
        mWifiCard = LayoutInflater.from(mContext).inflate(R.layout.card_info_settings_wifi, null);
        mWifiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardInfo.open(mContext, null);
            }
        });
    }

    /**
     * 请求一点资讯
     *
     * @param cardRoot
     * @param cardInfo
     */
    private void requestYDZZ(final ViewGroup cardRoot, final CardInfo cardInfo) {
        final YiDianContentManager yiDianContentManager = new YiDianContentManager(cardInfo);
        try {
            new YiDianTask(mContext,cardInfo.getCardSecondTypeId(), new YiDianTask.YiDianQuestCallBack() {
                @Override
                public void onError(int errorCode) {
                    switch (errorCode) {
                        case ERROR_CONNECT:
                            Toast.makeText(mContext, "网络连接错误", Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR_RESPONSE:
                            Toast.makeText(mContext, "服务器响应失败", Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR_DATA_NULL:
                            Toast.makeText(mContext, "获得数据为空", Toast.LENGTH_SHORT).show();
                            break;
                        case ERROR_PARSE:
                            Toast.makeText(mContext, "数据解析失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onSuccess(List<YiDianModel> yiDianModelList) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.topMargin = Dips.dipsToIntPixels(12.0f, mContext);

                    List<YiDianModel> sortedYiDianModelLst = CardUtils.sortYiDianModelList(yiDianModelList);
                    addYiDianLayout(cardRoot,cardInfo,sortedYiDianModelLst,params,yiDianContentManager);
//                    addYiDianLayout(cardRoot,cardInfo,yiDianModelList,params,yiDianContentManager);
                }
            }).run(yiDianContentManager.getNextRequestUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadTTNewsCard(final ViewGroup cardRoot, final CardInfo cardInfo) {
        final View rootView = LayoutInflater.from(mContext).inflate(R.layout.card_info_news_toutiao, null);
        //初始化card布局
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.card_news_list_toutiao);

        LinearLayout firstNewsLayout = (LinearLayout) rootView.findViewById(R.id.first_news_layout_toutiao);
        final TextView tv_firstNewsTitle = (TextView) firstNewsLayout.findViewById(R.id.first_toutiao_title);
        final TextView tv_firstNewsAbstract = (TextView) firstNewsLayout.findViewById(R.id.first_toutiao_abstract);
        RelativeLayout footerLayout = (RelativeLayout) rootView.findViewById(R.id.card_footer);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        //初始化数据：默认为空
//        final TTNewsAdapter newsAdapter = new TTNewsAdapter(articleList, mContext);
//        recyclerView.setAdapter(newsAdapter);
        //添加监听
        TextView tv_moreNews = (TextView) footerLayout.findViewById(R.id.more_news);
        tv_moreNews.setText("更多" + cardInfo.getCardName());
        tv_moreNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = CommonUtils.getInstance().isInstalledAPK(mContext,OpenMode.COMPONENT_NAME_JINRI_TOUTIAO);
                if (b) {
                    ComponentName componentName = new ComponentName(Const.TOUTIAO_packageName, Const.TOUTIAO_mainActivity);
                    Intent intent = new Intent();
                    intent.setComponent(componentName);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "请先下载今日头条APP", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final TextView tv_changeNews = (TextView) footerLayout.findViewById(R.id.change_news);
        tv_changeNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defalutGroup = defalutGroup.equals(key_group1) ? key_group2 : key_group1;
                loadNewsWithRecycler(recyclerView, tv_firstNewsTitle, tv_firstNewsAbstract, groupArticle.get(defalutGroup), cardInfo);
            }
        });
        firstNewsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                LinkedHashSet<Article> articleLinkedHashSet = groupArticle.get(defalutGroup);
                List<Article> articleListShow = new ArrayList<>();
                articleListShow.addAll(articleLinkedHashSet);
                Article firstArticle = articleListShow.get(0);
//                extras.putString("content", String.valueOf(firstArticle.mGroupId));//传入uri,如果配置成今日头条,没问题可以打开.如果配置成QQ浏览器,QQ浏览器不认识这个uri
                //配置第一种打开方式可能出现项:通过uri打开,假如不配置
                Bundle extras = new Bundle();
                extras.putString(OpenMode.OPEN_URL_KEY, firstArticle.mSrcUrl);
                extras.putString(OpenMode.FIRST_OPEN_MODE_TYPE_URI, Const.URI_TOUTIAO_ARTICLE_DETAIL + firstArticle.mGroupId);
                cardInfo.open(mContext, extras);

            }
        });
        //获取数据
        SsNewsApi.queryArticleList(mContext, new ArticleListQueryCallBack() {
            @Override
            public void onArticleListReceived(boolean b, List<Article> list) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = Dips.dipsToIntPixels(12.0f, mContext);
                rootView.setLayoutParams(params);

                if (!b) {
                    Toast.makeText(mContext, (mContext.getResources().getString(R.string.kinflow_string_obtain_toutiao_fail)), Toast.LENGTH_SHORT).show();
                    return;
                }
                //1、先对数据处理，然后再赋值
                CardContentManager.this.articleList = list;
                groupArticle = CardUtils.groupByAbstract(CardContentManager.this.articleList);

                int size = groupArticle.size();
                if (size <= 0) {
                    loadTTNewsCard(cardRoot, cardInfo);
                } else if (size == 1) {//获取到了一组
                    tv_changeNews.setVisibility(View.INVISIBLE);
                    loadNewsWithRecycler(recyclerView, tv_firstNewsTitle, tv_firstNewsAbstract, groupArticle.get(defalutGroup), cardInfo);
                } else {
                    tv_changeNews.setVisibility(View.VISIBLE);
                    //2、加载数据
                    loadNewsWithRecycler(recyclerView, tv_firstNewsTitle, tv_firstNewsAbstract, groupArticle.get(defalutGroup), cardInfo);
                }
                cardRoot.addView(rootView);
            }
        }, 20);
    }

    private void loadNewsWithRecycler(final RecyclerView recyclerView, final TextView tv_firstNewsTitle, final TextView tv_firstNewsAbstract, LinkedHashSet<Article> articleLinkedHashSet, CardInfo cardInfo) {
        //非空判断
        if (null == articleLinkedHashSet || articleLinkedHashSet.size() == 0) return;
        //类型转换
        List<Article> articleListShow = new ArrayList<>();
        articleListShow.addAll(articleLinkedHashSet);
        //四条新闻
//        TTNewsAdapter adapter = (TTNewsAdapter) recyclerView.getAdapter();
        //List<Article> articleList, Context context
        recyclerView.removeAllViews();
        TTNewsAdapter adapter = new TTNewsAdapter(articleListShow.subList(1, articleListShow.size()), mContext, cardInfo);
        recyclerView.setAdapter(adapter);
//        adapter.setArticleList(articleListShow.subList(1, articleListShow.size()));//报错：一共4条，反而截取1-5
//        adapter.notifyDataSetChanged();
        //第一条新闻
        Article firstArticle = articleListShow.get(0);
        tv_firstNewsTitle.setText(firstArticle.mTitle);
        tv_firstNewsAbstract.setText(firstArticle.mAbstract);
    }


    public void getKappmob(final ViewGroup cardRoot, CardInfo cardInfo) {
        final View kappmobCardView = LayoutInflater.from(mContext).inflate(R.layout.card_info_ads_banner, null);
        final ImageView imageView = (ImageView) kappmobCardView.findViewById(R.id.banner_iv_image);
        JSONObject json = null;
        try {
            json = new JSONObject(cardInfo.getCardExtra());
        } catch (JSONException e) {
            return;
        }
        if (json != null && json.has("img") && json.has("clc")) {
            final String imageUrl = json.optString("img");
            final String clickUrl = json.optString("clc");
            Picasso.with(mContext)
                    .load(imageUrl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            imageView.setImageBitmap(bitmap);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    bitmap.getHeight()
                            );
                            params.topMargin = Dips.dipsToIntPixels(12.0f, mContext);
                            kappmobCardView.setLayoutParams(params);
                            kappmobCardView.requestLayout();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
            kappmobCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    KinflowBrower.openUrl(mContext, clickUrl);
                }
            });
            cardRoot.addView(kappmobCardView);
        }
    }

    public void getAdview(final ViewGroup cardRoot) {
        final ViewGroup adviewCardView = (CardView) LayoutInflater.from(mContext).inflate(R.layout.card_info_ads_native, null);
        AdNativeManager adNativeManager = new AdNativeManager(mContext, "SDK20161518030339tw90ofxelsoctuo");
        //设置原生回调接口
        adNativeManager.setAdNativeInterface(new AdNativeInterface() {
            /**
             * 当广告请求成功时调用该函数. */
            @Override
            public void onReceivedAd(List arg0) {
                final NativeAdInfo nativeAdInfo = (NativeAdInfo) arg0.get(0);
                ImageView imageView = (ImageView) adviewCardView.findViewById(R.id.native_iv_icon);
                TextView textView = (TextView) adviewCardView.findViewById(R.id.native_tv_title);
                textView.setText(nativeAdInfo.getTitle());
                Picasso.with(mContext).load(nativeAdInfo.getIconUrl()).into(imageView);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = Dips.dipsToIntPixels(12.0f, mContext);
                adviewCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nativeAdInfo.onClick(adviewCardView);
                    }
                });
                cardRoot.addView(adviewCardView, params);
            }

            /**
             * 当广告请求失败时调用该函数.
             */
            @Override
            public void onFailedReceivedAd(String arg0) {
            }

            /**
             * 为下载广告时调用 返回现下载内容状态.
             */
            @Override
            public void onAdStatusChanged(int arg0) {
            }
        });
        //请求原生广告(可以自定义请求广告条数)
        adNativeManager.requestAd();
    }

    /**
     * 添加一点资讯Layout:3张图一个布局,0和1是一个布局
     * @param cardRoot
     * @param cardInfo
     * @param yiDianModelList
     * @param params
     * @param yiDianContentManager
     */
    private void addYiDianLayout(final ViewGroup cardRoot, final CardInfo cardInfo, List<YiDianModel> yiDianModelList, final LinearLayout.LayoutParams params, final YiDianContentManager yiDianContentManager) {
        //布局
        final ViewGroup cardNewsYiDianLayout = (CardView) LayoutInflater.from(mContext).inflate(R.layout.card_info_news_yidian, null);
        cardNewsYiDianLayout.setLayoutParams(params);
        cardNewsYiDianLayout.requestLayout();
        //头部初始化,但是默认为隐藏
        final LinearLayout yiDianHeadWith1Image = (LinearLayout) cardNewsYiDianLayout.findViewById(R.id.yidian_head_1image);
        final RelativeLayout yiDianHeadWith3Image = (RelativeLayout) cardNewsYiDianLayout.findViewById(R.id.yidian_head_3image);
        //中间&&底部
        final TextView tv_moreNews = (TextView) cardNewsYiDianLayout.findViewById(R.id.more_news);
        TextView tv_changeNews = (TextView) cardNewsYiDianLayout.findViewById(R.id.change_news);
        final RecyclerView rv_newsList = (RecyclerView) cardNewsYiDianLayout.findViewById(R.id.card_news_list_toutiao);
        rv_newsList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        tv_moreNews.setText("更多" + cardInfo.getCardName());
        tv_moreNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yiDianContentManager.moreNews(mContext);
            }
        });
        tv_changeNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //移除所有
                //提示正在加载....请稍等:在card级别提示,Fragment布局中
                //请求新内容
                try {
                    new YiDianTask(mContext,cardInfo.getCardSecondTypeId() ,new YiDianTask.YiDianQuestCallBack() {
                        @Override
                        public void onError(int errorCode) {
                            switch (errorCode) {
                                case ERROR_CONNECT:
                                    Toast.makeText(mContext, "网络连接错误", Toast.LENGTH_SHORT).show();
                                    break;
                                case ERROR_RESPONSE:
                                    Toast.makeText(mContext, "服务器响应失败", Toast.LENGTH_SHORT).show();
                                    break;
                                case ERROR_DATA_NULL:
                                    Toast.makeText(mContext, "获得数据为空", Toast.LENGTH_SHORT).show();
                                    break;
                                case ERROR_PARSE:
                                    Toast.makeText(mContext, "数据解析失败", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }

                        @Override
                        public void onSuccess(List<YiDianModel> yiDianModelList) {
                            List<YiDianModel> sortedYiDianModelLst = CardUtils.sortYiDianModelList(yiDianModelList);
                            //加载数据---中部四条
//                            tv_moreNews.setText(cardInfo.getCardFooter());
//                            rv_newsList.removeAllViews();
//                            YDNewsAdapter adapter = new YDNewsAdapter(sortedYiDianModelLst.subList(1, sortedYiDianModelLst.size()), mContext, cardInfo);
//                            rv_newsList.setAdapter(adapter);
                            YDNewsAdapter ydNewsAdapter = (YDNewsAdapter) rv_newsList.getAdapter();
                            ydNewsAdapter.updateYDnewsRecycler(sortedYiDianModelLst.subList(1, sortedYiDianModelLst.size()));
                            //加载头部数据---头部布局根据数据add
                            //这里需不需要移除头部??

//                            /*
                            addYiDianHead(cardInfo,
                                    cardRoot,
                                    cardNewsYiDianLayout,
                                    sortedYiDianModelLst.get(0),
                                    yiDianHeadWith1Image,
                                    yiDianHeadWith3Image,
                                    true);
//                            */
                        }
                    }).run(yiDianContentManager.getNextRequestUrl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //unFirst YiDianModel
        rv_newsList.removeAllViews();
        //重构前
//        YDNewsAdapter adapter = new YDNewsAdapter(yiDianModelList.subList(1, yiDianModelList.size()), mContext, cardInfo);
        //重构后
        YDNewsAdapter adapter = new YDNewsAdapter(mContext, cardInfo);
        rv_newsList.setAdapter(adapter);
        addYiDianHead(cardInfo,
                cardRoot,
                cardNewsYiDianLayout,
                yiDianModelList.get(0),
                yiDianHeadWith1Image,
                yiDianHeadWith3Image,
                false);
    }

    /**
     *
     * @param cardInfo 从服务器端获取到的当前card的信息
     * @param cardRoot 所有card的根目录,用于card加载完成之后,将card添加到所有card的根目录之下
     * @param cardNewsYiDianLayout 当前card的根目录
     * @param firstYiDianModel 一点咨询的头条信息
     * @param yiDianHeadWith1Image 头条数据针对两个布局中的一个(此布局包含一张图片)
     * @param yiDianHeadWith3Image 头条数据针对两个布局中的一个(此布局包含三张图片)
     * @param isChanges 是否为换一换,如果是换一换.则就不在cardRoot中添加cardNewsYiDianLayout
     */
    private void addYiDianHead(CardInfo cardInfo,
                               ViewGroup cardRoot,
                               final ViewGroup cardNewsYiDianLayout,
                               YiDianModel firstYiDianModel,
                               LinearLayout yiDianHeadWith1Image,
                               RelativeLayout yiDianHeadWith3Image,
                               boolean isChanges){
        yiDianHeadWith1Image.setVisibility(View.GONE);
        yiDianHeadWith3Image.setVisibility(View.GONE);
        switch (firstYiDianModel.getImages().length) {
            case 3://3张图片
                yiDianHeadWith3Image.setVisibility(View.VISIBLE);
                addYiDianHeadWith3Image(yiDianHeadWith3Image,firstYiDianModel,cardInfo);
                break;
            default://0张图片||1张图片
                yiDianHeadWith1Image.setVisibility(View.VISIBLE);
                addYiDianHeadWith1Image(yiDianHeadWith1Image, firstYiDianModel, cardInfo);
                break;
        }
        if (isChanges) return;
        cardRoot.addView(cardNewsYiDianLayout);//head部分最后添加,添加完成后,记得一定将card.add进去
    }

    private void addYiDianHeadWith1Image(LinearLayout yiDianHeadWith1Image, final  YiDianModel firstYiDianModel,final CardInfo cardInfo) {
        //布局
        final TextView tv_title = (TextView) yiDianHeadWith1Image.findViewById(R.id.yidian_news_title);
        final TextView tv_publishTime = (TextView) yiDianHeadWith1Image.findViewById(R.id.yidian_news_publish_time);
        final ImageView iv_yidianImage = (ImageView) yiDianHeadWith1Image.findViewById(R.id.yidian_image);
        //监听
        yiDianHeadWith1Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString(OpenMode.OPEN_URL_KEY, firstYiDianModel.getUrl());
                cardInfo.open(mContext, extras);
            }
        });
        //数据
        tv_title.setText(firstYiDianModel.getTitle());
        tv_publishTime.setText(firstYiDianModel.getDate());
        int imageCount  = firstYiDianModel.getImages().length;
        if (imageCount==1) {//0张图
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
            Picasso.with(mContext).load(firstYiDianModel.getImages()[0]).fit().centerCrop().into(iv_yidianImage);
        }else {//0张图
//            Picasso.with(mContext).load(String.valueOf(mContext.getResources().getDrawable(R.mipmap.ic_launcher)));
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            iv_yidianImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_image_show));
        }
    }

    public void addYiDianHeadWith3Image(RelativeLayout yiDianHeadWith3Image,final YiDianModel firstYiDianModel,final CardInfo cardInfo){
        //布局
        final TextView tv_title = (TextView) yiDianHeadWith3Image.findViewById(R.id.yidian_news_title);
        final TextView tv_publishTime = (TextView) yiDianHeadWith3Image.findViewById(R.id.yidian_news_publish_time);
        final ImageView iv_yidianImageLeft = (ImageView) yiDianHeadWith3Image.findViewById(R.id.yidian_image_left);
        final ImageView iv_yidianImageMiddle = (ImageView) yiDianHeadWith3Image.findViewById(R.id.yidian_image_middle);
        final ImageView iv_yidianImageRight = (ImageView) yiDianHeadWith3Image.findViewById(R.id.yidian_image_right);
        //监听
        yiDianHeadWith3Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString(OpenMode.OPEN_URL_KEY, firstYiDianModel.getUrl());
                cardInfo.open(mContext, extras);
            }
        });
        //数据
        tv_title.setText(firstYiDianModel.getTitle());
        tv_publishTime.setText(firstYiDianModel.getDate());

        int length = firstYiDianModel.getImages().length;
        for (int i = 0 ; i < length ; i++) {
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
                    Picasso.with(mContext).load(firstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageLeft);
                    break;
                case 1:
                    iv_yidianImageMiddle.setTag(target);
                    Picasso.with(mContext).load(firstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageMiddle);
                    break;
                case 2:
                    iv_yidianImageRight.setTag(target);
                    Picasso.with(mContext).load(firstYiDianModel.getImages()[i]).fit().centerCrop().into(iv_yidianImageRight);
                    break;
            }
        }
    }
}
