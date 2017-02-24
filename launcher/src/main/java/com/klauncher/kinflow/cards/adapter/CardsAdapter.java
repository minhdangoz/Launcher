package com.klauncher.kinflow.cards.adapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.klauncher.kinflow.browser.KinflowBrower;
import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.klauncher.launcher.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanni on 16/4/10.
 */
public class CardsAdapter extends RecyclerView.Adapter<CardViewHolder> {

    private Activity mContext;
    private List<CardInfo> mCardsList = new ArrayList<>();

    private boolean mNetworkConnected = true;
    private CardInfo mWifiCardInfo = null;

    private BroadcastReceiver mWifiChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showOrHideWifiCard();
        }
    };

    public CardsAdapter(Activity context, List<CardInfo> cardsList) {
        mContext = context;
        mWifiCardInfo = CardInfo.createWifiSettingCard(this.mContext);
//        mCardsList.addAll(cardsList);
        setCardsList(cardsList);
        //注册监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mWifiChangeReceiver, filter);
    }

    public void setCardsList(List<CardInfo> cardsList) {
        if(null == cardsList) {
            this.mCardsList = new ArrayList<>();
        }else {
            this.mCardsList = cardsList;
        }
    }

    public void addCard(CardInfo cardInfo){
        if(null==this.mCardsList) this.mCardsList = new ArrayList<>();
        this.mCardsList.add(cardInfo);
        this.notifyDataSetChanged();
    }

    public void updateCards(List<CardInfo> cardsList) {
//        this.mCardsList.clear();
//        this.mCardsList.addAll(cardsList);
        setCardsList(cardsList);
        this.notifyDataSetChanged();
    }

    //manager getList<YiDianModel>

    @Override
    public int getItemViewType(int position) {
        CardInfo card = mCardsList.get(position);
        return card.getCardSecondTypeId();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        try {
            View view;
            switch (viewType) {
                case CardInfo.CARD_TYPE_SETTING_WIFI:
                    view = LayoutInflater.from(mContext).inflate(R.layout.card_info_settings_wifi, parent, false);
                    return new WifiCardViewHolder(view);
//                case CardInfo.CARD_TYPE_AD_KAPPMOB:
                case CardIdMap.ADVERTISEMENT_YOKMOB:
                case CardIdMap.CARD_TYPE_SKIP_AMAP_DIANYING:
                case CardIdMap.CARD_TYPE_SKIP_AMAP_YULE:
                    view = LayoutInflater.from(mContext).inflate(R.layout.card_info_ads_banner, parent, false);
                    return new AdbannerCardViewHolder(mContext,view);
    //            case CardInfo.CARD_TYPE_AD_ADVIEW:
    //                view = LayoutInflater.from(mContext).inflate(R.layout.card_info_ads_native, parent, false);
    //                return new AdnativeCardViewHolder(mContext,view);
    //            case CardInfo.CARD_TYPE_NEWS_TT_REDIAN:
    //                view = LayoutInflater.from(mContext).inflate(R.layout.card_info_news_toutiao, parent, false);
    //                return new TTCardViewHolder(mContext,view);
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
                    view = LayoutInflater.from(mContext).inflate(R.layout.card_info_news_yidian, parent, false);
                    return new YDCardViewHolder(view,mContext);
                case CardIdMap.CARD_TYPE_NEWS_TT_REDIAN://头条热点
                case CardIdMap.CARD_TYPE_NEWS_TT_SHEHUI://头条社会
                case CardIdMap.CARD_TYPE_NEWS_TT_YULE://头条娱乐
                case CardIdMap.CARD_TYPE_NEWS_TT_CAIJING://头条财经
                case CardIdMap.CARD_TYPE_NEWS_TT_TIYU://头条体育
                case CardIdMap.CARD_TYPE_NEWS_TT_KEJI://头条科技
                case CardIdMap.CARD_TYPE_NEWS_TT_JUNSHI://头条军事
                case CardIdMap.CARD_TYPE_NEWS_TT_QICHE://头条汽车
                    view = LayoutInflater.from(mContext).inflate(R.layout.card_info_news_yidian, parent, false);
                    return new JRTTCardViewHolder(view,mContext);
                default:
                    log("未知的view啊 ,大哥===================");
                    view = LayoutInflater.from(mContext).inflate(R.layout.card_info_unknown, parent, false);
                    break;
            }
            return new CardViewHolder(view);
        } catch (Exception e) {
            e.printStackTrace();
            log("CardsAdapter:onBindViewHolder时出错");
            return new CardViewHolder(LayoutInflater.from(mContext).inflate(R.layout.card_info_unknown, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, final int position) {
        try {
            CardInfo card = mCardsList.get(position);
            if (holder instanceof WifiCardViewHolder) {
                ((WifiCardViewHolder) holder).wifiLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intentWifi=new Intent();
                            if (android.os.Build.VERSION.SDK_INT > 10) {
                                intentWifi.setAction(android.provider.Settings.ACTION_SETTINGS);
                            } else {
                                intentWifi.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                            }
                            mContext.startActivity(intentWifi);
                        } catch (Exception e) {
                            Toast.makeText(mContext, "打开网络设置失败,请在设置中手动打开", Toast.LENGTH_SHORT).show();
                            log("打开网络设置失败,请在设置中手动打开");
                        }

                    }
                });
            } else if (holder instanceof AdbannerCardViewHolder) {//yokmob的CardView
                AdbannerCardViewHolder yokmobHolder = (AdbannerCardViewHolder) holder;
                yokmobHolder.getBannerImage().setOnClickListener(yokmobHolder);
                yokmobHolder.setCardInfo(card);

            }
    //        else if (holder instanceof AdnativeCardViewHolder) {//adView的ViewHolder
    //            AdnativeCardViewHolder adviewHolder = (AdnativeCardViewHolder) holder;
    //            adviewHolder.getCardView().setOnClickListener(adviewHolder);
    //            adviewHolder.getNativeGroup().setOnClickListener(adviewHolder);
    //            adviewHolder.setCardInfo(card);
    //
    //        }
            else if (holder instanceof YDCardViewHolder) {
                YDCardViewHolder ydCardViewHolder = (YDCardViewHolder) holder;
                //添加监听
                ydCardViewHolder.getmYiDianHeadWith1ImageLayout().setOnClickListener(ydCardViewHolder);
                ydCardViewHolder.getmYiDianHeadWith3ImageLayout().setOnClickListener(ydCardViewHolder);
                ydCardViewHolder.getTvChangeNews().setOnClickListener(ydCardViewHolder);
                ydCardViewHolder.getTvMoreNews().setOnClickListener(ydCardViewHolder);
                //初始化cardInfo数据
                ydCardViewHolder.setmCardInfo(card);

            } else if (holder instanceof JRTTCardViewHolder) {//还缺少事件监听设置
                JRTTCardViewHolder jrttCardViewHolder = (JRTTCardViewHolder) holder;
                jrttCardViewHolder.getJrttHeadWith1ImageLayout().setOnClickListener(jrttCardViewHolder);
                jrttCardViewHolder.getJrttHeadWith3ImageLayout().setOnClickListener(jrttCardViewHolder);
                jrttCardViewHolder.getTvChangeNews().setOnClickListener(jrttCardViewHolder);
                jrttCardViewHolder.getTvMoreNews().setOnClickListener(jrttCardViewHolder);
                jrttCardViewHolder.setJrttCardInfo(card);
            }
//        else if (holder instanceof TTCardViewHolder) {
//            TTCardViewHolder ttCardViewHolder = (TTCardViewHolder) holder;
//            ttCardViewHolder.getTouTiaoFirstNewsLayout().setOnClickListener(ttCardViewHolder);
//            ttCardViewHolder.getChangeNews().setOnClickListener(ttCardViewHolder);
//            ttCardViewHolder.getMoreNews().setOnClickListener(ttCardViewHolder);
//            ttCardViewHolder.setmCardInfo(card);
//        }
        } catch (Exception e) {
            e.printStackTrace();
            log("CardsAdapter:onBindViewHolder时出错");
        }
    }

    @Override
    public int getItemCount() {
        return mCardsList.size();
    }

    private void showOrHideWifiCard() {
        if (mNetworkConnected != NetworkUtils.isNetworkAvailable(mContext)) {
            mNetworkConnected = NetworkUtils.isNetworkAvailable(mContext);
            if (mNetworkConnected) {
                mCardsList.remove(mWifiCardInfo);
            } else {
                mCardsList.add(0, mWifiCardInfo);
            }
        }
        notifyDataSetChanged();
    }

    private void loadKappmobCard(CardInfo cardInfo, final AdbannerCardViewHolder holder) {
        JSONObject json = null;
        try {
            json = new JSONObject(cardInfo.getCardExtra());
        } catch (JSONException e) {
            return;
        }
        if (json != null && json.has("img") && json.has("clc")) {
            final String imageUrl = json.optString("img");
            final String clickUrl = json.optString("clc");
            Picasso.with(mContext).load(imageUrl).into(holder.bannerImage);
            holder.bannerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    KinflowBrower.openUrl(mContext, clickUrl);
                }
            });
        }
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

}
