package com.kapp.knews.repository.server;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kapp.knews.KnewsApp;
import com.kapp.knews.R;
import com.kapp.knews.base.recycler.data.NewsBaseData;
import com.kapp.knews.common.browser.KinflowBrower;
import com.kapp.knews.common.utils.FileUtils;
import com.kapp.knews.helper.Utils;
import com.kapp.knews.helper.content.ContentHelper;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdBean;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdControl;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdvertisement;
import com.kapp.knews.utils.KBannerAdUtil;
import com.kapp.knews.utils.MathUtil;
import com.kapp.knews.utils.NetworkUtil;
import com.kapp.knews.utils.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by xixionghui on 2016/12/23.
 */

/*
*
* 以后要把广告存进数据库，用数据库管理
* */
public class AdShowManager {
    public static final String LOAD_REFRESH_AD_INDEX = "refresh_ad_index";
    public static final String LOAD_MORE_INDEX = "refresh_ad_more";

    public static final int AD_INTERVAL = 15;
    /**
     * 后台控制从ADX-SDK取广告的标志
     */
    private static final String FLAG_ADX_SDK = "#";
    public static final String KEY_AD_INTERVAL = "key_ad_interval";
    /**
     * 默认5条新闻插入广告
     */
    public static final int INTERVAL_DEFAULT = 6;

    private static AdShowManager adShowManager = new AdShowManager();

    private AdShowManager() {
    }

    public static AdShowManager getInstance() {
        return adShowManager;
    }

//    2017-1-6
//    /**
//     * 获取所有广告
//     *
//     * @return
//     */
//    public List<? extends NewsBaseData> getAdList() {
//        List<NewsBaseData> klauncherAdvertisementList = new ArrayList<>();
//        List<AdControl> adControlList = ServerControlManager.getInstance().getServerController().getAdControlList();
//        //把AdControl赋值到KlauncherAdvertisement
//
//        for (AdControl adControl :
//                adControlList) {
//            //adOrder广告位置，adOpenOptions打开方式，adSID广告平台分发的sid,adExtra预留字段
//            KlauncherAdControl klauncherAdControl = new KlauncherAdControl(adControl.getAdOrder(), adControl
// .getAdOpenOptions());
//            KlauncherAdBean klauncherAdBean = new KlauncherAdBean(adControl.getImageUrl(), adControl.getClickUrl());
//            klauncherAdvertisementList.add(new KlauncherAdvertisement(klauncherAdControl, klauncherAdBean));
//
//        }
//
//        return klauncherAdvertisementList;
//    }

    /**
     * 获取所有广告
     *
     * @param activity
     * @return
     */
    public List<NewsBaseData> getAdList(Activity activity) {
        List<NewsBaseData> klauncherAdvertisementList = new ArrayList<>();
        List<AdControl> adControlList = ServerControlManager.getInstance().getServerController().getAdControlList();
        //把AdControl赋值到KlauncherAdvertisement

        for (AdControl adControl :
                adControlList) {
            //adOrder广告位置，adOpenOptions打开方式，adSID广告平台分发的sid,adExtra预留字段
            KlauncherAdControl klauncherAdControl = new KlauncherAdControl(adControl.getAdOrder(), adControl
                    .getAdOpenOptions());

            if (adControl.getImageUrl().equals(FLAG_ADX_SDK)) {//来自ADX-SDK的广告控制
                if (NetworkUtil.isNetworkAvailable(activity)) {

                    klauncherAdvertisementList.add(KBannerAdUtil.getKBannerAdForPlaceHolder());

                }
            } else {
                KlauncherAdBean klauncherAdBean = new KlauncherAdBean(adControl.getImageUrl(), adControl.getClickUrl());
                klauncherAdvertisementList.add(new KlauncherAdvertisement(klauncherAdControl, klauncherAdBean));
            }
        }

        return klauncherAdvertisementList;
    }

    /**
     * 随机获取本地的广告
     *
     * @return
     */
    public NewsBaseData getRangeLocalAdBean() {
        InputStream is = null;
        try {
            is = ContentHelper.getAssets().open("default_server_control");
            String json = FileUtils.loadStringFromStream(is);
            JSONObject localJsonRoot = new JSONObject(json);
            JSONArray adsArr = localJsonRoot.optJSONArray("ads");

            //获取随机的本地广告
            int randomInt = MathUtil.randomInt(adsArr.length());
            JSONObject randomJsonObject = adsArr.getJSONObject(randomInt);
            AdControl adControl = new AdControl(randomJsonObject);
            KlauncherAdControl klauncherAdControl = new KlauncherAdControl(adControl.getAdOrder(), adControl
                    .getAdOpenOptions());
            KlauncherAdBean klauncherAdBean = new KlauncherAdBean(adControl.getImageUrl(), adControl.getClickUrl());

            return new KlauncherAdvertisement(klauncherAdControl, klauncherAdBean);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.safeClose(is);
        }
        return null;
    }

    /**
     * 获取ADX-SDK广告失败后的view
     *
     * @param context
     * @param newsBaseData
     * @return
     */
    public View getAdxSdkOnFailView(final Context context, final NewsBaseData newsBaseData) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_big_image_no_title, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.banner_big_image_no_title);
        final int contentProvider = newsBaseData.getContentProvider();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newsBaseData.getDataType() == NewsBaseData.TYPE_AD) {
                    if (contentProvider == KlauncherAdvertisement.contentProvider) {
                        KlauncherAdvertisement klauncherAdvertisement = (KlauncherAdvertisement) newsBaseData;
                        String openUrl = klauncherAdvertisement.getmNewsBean().getClickUrl();
                        KinflowBrower.openUrl(context, openUrl);
                    }
                }
            }
        });

        if (contentProvider == KlauncherAdvertisement.contentProvider) {
            KlauncherAdvertisement klauncherAdvertisement = (KlauncherAdvertisement) newsBaseData;

            Glide.with(context).load(klauncherAdvertisement.mNewsBean.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .placeholder(R.color.image_place_holder)
                    .error(R.drawable.ic_load_fail)
                    .into(imageView);

        }
        return view;
    }

    /**
     * 获取指定数量的ADX-SDK广告获取失败的view
     *
     * @param context
     * @param size
     * @return
     */
    public List<View> getAdxSdkOnFailViews(Context context, int size) {
        List<NewsBaseData> newsBaseDatas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            newsBaseDatas.add(getRangeLocalAdBean());
        }
        return getAdxSdkOnFailViews(context, newsBaseDatas);
    }

    /**
     * 获取指定数量的ADX-SDK广告获取失败的view
     *
     * @param context
     * @param newsBaseDatas
     * @return
     */
    private List<View> getAdxSdkOnFailViews(Context context, List<NewsBaseData> newsBaseDatas) {
        List<View> views = new ArrayList<>(newsBaseDatas.size());
        for (NewsBaseData newsBaseData : newsBaseDatas) {
            views.add(getAdxSdkOnFailView(context, newsBaseData));
        }
        return views;
    }

//    2017-1-6
//    public NewsBaseData getNextAd(int loadType) {
//        //获取上次用到的广告的index
//        NewsBaseData adNewsBaseData = null;
//        //所有广告集合
//        List<? extends NewsBaseData> adList = getAdList();
//
//        switch (loadType) {
//            case LoadNewsType.TYPE_LOAD_REFRESH_SUCCESS:
//                Collections.reverse(adList);//集合反转
//                int currentRefreshIndex = CommonShareData.getInt(LOAD_REFRESH_AD_INDEX, -1) + 1;
//                if (currentRefreshIndex >= adList.size()) {
//                    currentRefreshIndex = 0;
//                }
//                adNewsBaseData = adList.get(currentRefreshIndex);
//                CommonShareData.putInt(LOAD_REFRESH_AD_INDEX, currentRefreshIndex);
//                break;
//            case LoadNewsType.TYPE_LOAD_MORE_SUCCESS:
//                int currentMoreIndex = CommonShareData.getInt(LOAD_MORE_INDEX, -1) + 1;//默认为-1，代表一个也没加载
//                if (currentMoreIndex >= adList.size()) {
//                    currentMoreIndex = 0;
//                }
//                adNewsBaseData = adList.get(currentMoreIndex);
//                CommonShareData.putInt(LOAD_MORE_INDEX, currentMoreIndex);
//                break;
//        }
//
//        return adNewsBaseData;
//
//    }

    public List<NewsBaseData> getNextAd(int loadType, Activity activity) {
        return getAdList(activity);
    }

    /**
     * 保存请求到的interval
     *
     * @param interval
     */
    public void saveAdInterval(int interval) {
        PreferenceUtil.write(Utils.getContext(), KnewsApp.FILE_PREFRENCE, AdShowManager.KEY_AD_INTERVAL, interval);
    }

    /**
     * 读取最新缓存的interval
     *
     * @return
     */
    public int getLocalAdInterval() {
        int defaultInterval = AdShowManager.INTERVAL_DEFAULT;
        return PreferenceUtil.readInt(Utils.getContext(), KnewsApp.FILE_PREFRENCE, AdShowManager.KEY_AD_INTERVAL,
                defaultInterval);
    }

}
