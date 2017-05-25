package com.kapp.knews.repository.server;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.apkfuns.logutils.LogUtils;
import com.kapp.knews.BuildConfig;
import com.kapp.knews.base.recycler.data.NewsBaseData;
import com.kapp.knews.base.tabs.TabInfo;
import com.kapp.knews.common.LoadNewsType;
import com.kapp.knews.repository.bean.DongFangTouTiao;
import com.kapp.knews.repository.bean.lejingda.klauncher.KlauncherAdvertisement;
import com.kapp.knews.repository.net.DongFangAsynchronousTask;
import com.kapp.knews.repository.net.NetMessageFactory;
import com.kapp.knews.repository.net.NetResponse;
import com.kapp.knews.repository.utils.CommonShareData;
import com.kapp.knews.repository.utils.TelephonyUtils;
import com.kapp.knews.utils.KBannerAdUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.kapp.knews.common.LoadNewsType.TYPE_LOAD_MORE_ERROR;
import static com.kapp.knews.common.LoadNewsType.TYPE_LOAD_MORE_SUCCESS;
import static com.kapp.knews.common.LoadNewsType.TYPE_LOAD_REFRESH_ERROR;
import static com.kapp.knews.common.LoadNewsType.TYPE_LOAD_REFRESH_SUCCESS;
import static com.kapp.knews.repository.server.AdShowManager.LOAD_MORE_INDEX;
import static com.kapp.knews.repository.server.AdShowManager.LOAD_REFRESH_AD_INDEX;


/**
 * Created by xixionghui on 2016/11/24.
 */

public class DongFangRequestManager {

    public static final String LOAD_TYPE = "load_type";
    public static final String TAB_INFO = "tab_info";

    public static final String NEGATIVE = "negative";//负值，下拉刷新使用
    public static final String POSITIVE = "positive";//正值，上拉加载使用

    public static final String PARAMETER_START_KEY = "startkey";//为上次请求返回的endkey作为此次请求的startkey
    public static final String PARAMETER_NEW_KEY = "newkey";
    public static final String PARAMETER_IDX = "idx";
    public static final String PARAMETER_PGNUM = "pgnum";
    public static final String PARAMETER_IMEI = "ime";
    public static final String PARAMETER_TYPE = "type";
    public static final String PARAMETER_KEY = "key";
    public static final String PARAMETER_APP_TYPE_ID = "apptypeid";
    public static final String PARAMETER_APP_VERSION = "appver";
    public static final String PARAMETER_QID = "qid";
    public static final String PARAMETER_POSITION = "position";

    public static final String VALUE_KDAOHANG = "kdaohang";
    private static final String TAG = DongFangRequestManager.class.getSimpleName();
    private static final boolean DEBUG = true;


    private WeakReference<Activity> mContext;
    private RefreshHandle refreshHandle = new RefreshHandle(this);
    private DongFangManagerListener mListener;


    public DongFangRequestManager(Activity context, DongFangManagerListener listener) {
        this.mContext = new WeakReference<Activity>(context);
        this.mListener = listener;
    }


    /**
     * 返回完成key的名称
     *
     * @param tabCategoryName
     * @param field
     * @return
     */
    public String getCompleteKeyName(String tabCategoryName, String field, int loadType) {

        StringBuilder stringBuilder = new StringBuilder("dongFangTouTiao");//内容提供商
        stringBuilder
                .append("_").append(tabCategoryName)//频道category
                .append("_").append(field);//key名称

        if (field.equals(PARAMETER_START_KEY) || field.equals(PARAMETER_NEW_KEY)) {
            LogUtils.e("返回完整的key名称：" + stringBuilder.toString());
            return stringBuilder.toString();
        } else {
            switch (loadType) {
                case LoadNewsType.TYPE_FIRST:
                case LoadNewsType.TYPE_LOAD_MORE:
                    stringBuilder.append("_").append(NEGATIVE);
                    break;
                case LoadNewsType.TYPE_LOAD_REFRESH:
                    stringBuilder.append("_").append(POSITIVE);
                    break;
            }
            LogUtils.e("返回完整的key名称：" + stringBuilder.toString());
            return stringBuilder.toString();

        }
    }


    public void loadDongFang(final int loadType, final TabInfo tabInfo) {
        LogUtils.e("==============================开始请求东方头条==============================: loadType = " + loadType + "" +
                "   ,tabInfo = " + tabInfo.toString());
        Activity activity = mContext.get();
        if(null!=activity){
            new DongFangAsynchronousTask(activity, refreshHandle).accessKey(loadType, tabInfo);
        }

    }

//2017-1-6
//    /**
//     * 下拉刷新处理的handler
//     */
//    public static class RefreshHandle extends Handler {
//        private final WeakReference<DongFangRequestManager> mWeakReference;
//
//        public RefreshHandle(DongFangRequestManager dongFangRequestManager) {
//            mWeakReference = new WeakReference<DongFangRequestManager>(dongFangRequestManager);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            final DongFangRequestManager dongFangRequestManager = mWeakReference.get();
//
//            Bundle bundle = msg.getData();
//            final int loadType = bundle.getInt(LOAD_TYPE);
//            final TabInfo taInfo = bundle.getParcelable(TAB_INFO);
//
//            switch (msg.what) {
//
//                case NetMessageFactory.MESSAGE_OBTAION_DONGFANG_KEY:
//
//                    if (NetResponse.SUCCESS == msg.arg1 && null != msg.obj) {
//                        final String key = (String) msg.obj;
//                        new Thread() {
//                            @Override
//                            public void run() {
//
//                                LogUtils.e("获取到东方头条的key = "+key+"\n"+
//                                        "获取到TabInfo = "+taInfo.toString()
//                                );
//                                new DongFangAsynchronousTask(dongFangRequestManager.mContext,
// dongFangRequestManager.refreshHandle).requestDongFangTouTiaoArticle(
//                                        loadType,
//                                        taInfo,
//                                        dongFangRequestManager.getRequestBody(loadType,taInfo,key),//请求体
//                                        dongFangRequestManager.requestParameterPgnum(taInfo.getTabCategory(),
// PARAMETER_PGNUM,loadType)//请求参数pgnum
//
//                                );
//
//                            }
//                        }.start();
//                    } else {
//                        switch (loadType) {
//                            case LoadNewsType.TYPE_LOAD_MORE:
//                                dongFangRequestManager.mListener.setNewsList(null, TYPE_LOAD_MORE_ERROR);
//                                break;
//                            case LoadNewsType.TYPE_LOAD_REFRESH:
//                                dongFangRequestManager.mListener.setNewsList(null, TYPE_LOAD_REFRESH_ERROR);
//                                break;
//                        }
//
//                    }
//
//
//                    break;
//                case NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE://这里要区分下拉刷新和上拉加载
//                    switch (loadType) {
//                        case LoadNewsType.TYPE_FIRST:
//                        case LoadNewsType.TYPE_LOAD_MORE:
//                            //判断成功与否
//                            if (msg.arg1 != NetResponse.SUCCESS) {
//                                LogUtils.e("加载更多失败");
//                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
// TYPE_LOAD_MORE_ERROR);
//                            } else {
//                                LogUtils.e("加载更多成功");
//                                //添加广告----开始
//                                NewsBaseData adNewsBaseData = null;
////                                long pgnum = bundle.getLong(PARAMETER_PGNUM,0);//获取当前请求的pgnum
////                                long remainder = pgnum%2;
////                                if (remainder==1){//
////                                    adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_REFRESH_SUCCESS);
////                                }
//
//
//
//
//
//
////   2017-1-6                    adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_MORE_SUCCESS);
////                                List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
////                                LogUtils.e("加载更多成功,获取到数据条数 = "+newsBaseDataList.size());
////                                if (null!=adNewsBaseData)
////                                    newsBaseDataList.add(newsBaseDataList.size()/2,adNewsBaseData);
////                                //添加广告----结束
////                                dongFangRequestManager.mListener.setNewsList(newsBaseDataList,
// TYPE_LOAD_MORE_SUCCESS);
////                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(),msg,loadType);
//
//
// /////////////////////////////////////////////////////////////////////////////////////////////////////////
////                                adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_MORE_SUCCESS);
//                                List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
//                                LogUtils.e("加载更多成功,获取到数据条数 = "+newsBaseDataList.size());
//                                if (null!=adNewsBaseData)
//                                    newsBaseDataList.add(newsBaseDataList.size()/2,adNewsBaseData);
//                                //添加广告----结束
//                                dongFangRequestManager.mListener.setNewsList(newsBaseDataList,
// TYPE_LOAD_MORE_SUCCESS);
//                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(),msg,loadType);
//
//
//
//
//
//                            }
//                            break;
//
//                        case LoadNewsType.TYPE_LOAD_REFRESH:
//                            //判断成功与否
//                            if (msg.arg1 != NetResponse.SUCCESS) {
//                                LogUtils.e("下拉刷新失败");
//                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
// TYPE_LOAD_REFRESH_ERROR);
//                            } else {
//                                //添加广告----开始
//                                NewsBaseData adNewsBaseData = null;
//                                long pgnum = bundle.getLong(PARAMETER_PGNUM,0);//获取当前请求的pgnum
//                                long remainder = pgnum%2;
//                                if (remainder==0){//
//
//
////   2017-1-6                        adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_REFRESH_SUCCESS);
////                                    List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
////                                    if (null!=adNewsBaseData) newsBaseDataList.add(newsBaseDataList.size()/2,
// adNewsBaseData);
//
//
//
// /////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    //在这里处理广告
//                                    List<NewsBaseData> ads = AdShowManager.getInstance().getNextAd
//                                            (LoadNewsType.TYPE_LOAD_REFRESH_SUCCESS,mContext);
//                                    List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
//                                    int every= AdShowManager.INTERVAL_DEFAULT;
//                                    int newSize = newsBaseDataList.size();
//                                    int[] adIndexs = getAdIndexs(newSize, every);
//
//                                    logDebug("handleMessage(...) 请求到的新闻的数量="+newSize+"  要求每隔"+every+"条新闻插入广告");
//
//                                    int adSize = ads.size();
//                                    int insertAdSize=adIndexs.length;
//                                    List<KlauncherAdvertisement> inserAdxs=null;
//                                    if(adSize<insertAdSize){//所有广告的数量小于要插入的广告数量，则剩下的差额从ADX-SDK中取广告
//                                        inserAdxs = KBannerAdUtil.getKBannerAds(Utils
//                                                .getContext(), insertAdSize - adSize);
//                                         ads.addAll(inserAdxs);
//                                    }
//                                    StringBuilder stringIndexs=new StringBuilder();
//                                    int count=0;
//                                   for(int index:adIndexs){
//                                       stringIndexs.append(index+",");
//                                       newsBaseDataList.add(index+count,ads.get(count));
//                                       count++;
//                                   }
//                                    stringIndexs.substring(0,stringIndexs.length()-1);
//                                    logDebug("handleMessage(...) stringIndexs="+stringIndexs.toString());
//
//                                }
//                                //添加广告----结束
//                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
// TYPE_LOAD_REFRESH_SUCCESS);
//                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(),msg,loadType);
//                                break;
//                            }
//                            break;
//                        default:
//                            LogUtils.e("未知请求类型："+loadType);
//                            dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
// TYPE_LOAD_MORE_ERROR);
//                            dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
// TYPE_LOAD_REFRESH_ERROR);
//                            break;
//                    }
//
//                    break;//MESSAGE_OBTAION_DONGFANG_ARTICLE
//
//            }
//        }
//
//
//    }

    /**
     * 下拉刷新处理的handler
     */
    public  static class RefreshHandle extends Handler {
        private final WeakReference<DongFangRequestManager> mWeakReference;

        public RefreshHandle(DongFangRequestManager dongFangRequestManager) {
            mWeakReference = new WeakReference<DongFangRequestManager>(dongFangRequestManager);
        }

        @Override
        public void handleMessage(Message msg) {
            final DongFangRequestManager dongFangRequestManager = mWeakReference.get();
            if(null==dongFangRequestManager){
                return;
            }

            Bundle bundle = msg.getData();
            final int loadType = bundle.getInt(LOAD_TYPE);
            final TabInfo taInfo = bundle.getParcelable(TAB_INFO);

            switch (msg.what) {

                case NetMessageFactory.MESSAGE_OBTAION_DONGFANG_KEY:

                    if (NetResponse.SUCCESS == msg.arg1 && null != msg.obj) {
                        final String key = (String) msg.obj;
                        Runnable keyRun=new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.e("获取到东方头条的key = " + key + "\n" +
                                        "获取到TabInfo = " + taInfo.toString()
                                );
                                Activity activity = dongFangRequestManager.mContext.get();
                                if(null!=activity){
                                    new DongFangAsynchronousTask(activity, dongFangRequestManager
                                            .refreshHandle).requestDongFangTouTiaoArticle(
                                            loadType,
                                            taInfo,
                                            dongFangRequestManager.getRequestBody(loadType, taInfo, key),//请求体
                                            dongFangRequestManager.requestParameterPgnum(taInfo.getTabCategory(),
                                                    PARAMETER_PGNUM, loadType)//请求参数pgnum

                                    );
                                }
                            }
                        };
                        Thread keyThread=new Thread(keyRun);
                        keyThread.start();
                    } else {
                        switch (loadType) {
                            case LoadNewsType.TYPE_LOAD_MORE:
                                dongFangRequestManager.mListener.setNewsList(null, TYPE_LOAD_MORE_ERROR);
                                break;
                            case LoadNewsType.TYPE_LOAD_REFRESH:
                                dongFangRequestManager.mListener.setNewsList(null, TYPE_LOAD_REFRESH_ERROR);
                                break;
                        }

                    }


                    break;
                case NetMessageFactory.MESSAGE_OBTAION_DONGFANG_ARTICLE://这里要区分下拉刷新和上拉加载
                    switch (loadType) {
                        case LoadNewsType.TYPE_FIRST:
                        case LoadNewsType.TYPE_LOAD_MORE:
                            //判断成功与否
                            if (msg.arg1 != NetResponse.SUCCESS) {
                                LogUtils.e("加载更多失败");
                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
                                        TYPE_LOAD_MORE_ERROR);
                            } else {
                                LogUtils.e("加载更多成功");
                                //添加广告----开始
//                                NewsBaseData adNewsBaseData = null;
//                                long pgnum = bundle.getLong(PARAMETER_PGNUM,0);//获取当前请求的pgnum
//                                long remainder = pgnum%2;
//                                if (remainder==1){//
//                                    adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_REFRESH_SUCCESS);
//                                }


//   2017-1-6                    adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_MORE_SUCCESS);
//                                List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
//                                LogUtils.e("加载更多成功,获取到数据条数 = "+newsBaseDataList.size());
//                                if (null!=adNewsBaseData)
//                                    newsBaseDataList.add(newsBaseDataList.size()/2,adNewsBaseData);
//                                //添加广告----结束
//                                dongFangRequestManager.mListener.setNewsList(newsBaseDataList,
// TYPE_LOAD_MORE_SUCCESS);
//                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(),msg,loadType);

                                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                                List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
                                LogUtils.e("加载更多成功,获取到数据条数 = " + newsBaseDataList.size());

                                Activity activity = dongFangRequestManager.mContext.get();
                                if(null!=activity){
                                    //添加广告
                                    List<NewsBaseData> ads = AdShowManager.getInstance().getNextAd
                                            (TYPE_LOAD_MORE_SUCCESS, activity);
                                    int interval = AdShowManager.getInstance().getLocalAdInterval();
                                    addAds((List<NewsBaseData>) msg.obj, ads, interval,
                                            TYPE_LOAD_MORE_SUCCESS);
                                }
                                //回调
                                dongFangRequestManager.mListener.setNewsList(newsBaseDataList, TYPE_LOAD_MORE_SUCCESS);
                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(), msg, loadType);

                            }
                            break;

                        case LoadNewsType.TYPE_LOAD_REFRESH:
                            //判断成功与否
                            if (msg.arg1 != NetResponse.SUCCESS) {
                                LogUtils.e("下拉刷新失败");
                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
                                        TYPE_LOAD_REFRESH_ERROR);
                            } else {
                                //添加广告----开始
                                NewsBaseData adNewsBaseData = null;
                                long pgnum = bundle.getLong(PARAMETER_PGNUM, 0);//获取当前请求的pgnum
                                long remainder = pgnum % 2;
                                if (remainder == 0) {//


//   2017-1-6                        adNewsBaseData = AdShowManager.getInstance().getNextAd(LoadNewsType
// .TYPE_LOAD_REFRESH_SUCCESS);
//                                    List<NewsBaseData> newsBaseDataList = (List<NewsBaseData>) msg.obj;
//                                    if (null!=adNewsBaseData) newsBaseDataList.add(newsBaseDataList.size()/2,
// adNewsBaseData);


                                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                                    Activity activity = dongFangRequestManager.mContext.get();
                                    if(null!=activity){
                                        //在这里处理广告
                                        List<NewsBaseData> ads = AdShowManager.getInstance().getNextAd
                                                (TYPE_LOAD_REFRESH_SUCCESS, activity);
                                        int interval = AdShowManager.getInstance().getLocalAdInterval();
                                        addAds((List<NewsBaseData>) msg.obj, ads, interval,
                                                TYPE_LOAD_REFRESH_SUCCESS);
                                    }

                                }
                                //添加广告----结束
                                dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
                                        TYPE_LOAD_REFRESH_SUCCESS);
                                dongFangRequestManager.saveParameter(taInfo.getTabCategory(), msg, loadType);
                                break;
                            }
                            break;
                        default:
                            LogUtils.e("未知请求类型：" + loadType);
                            dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
                                    TYPE_LOAD_MORE_ERROR);
                            dongFangRequestManager.mListener.setNewsList((List<NewsBaseData>) msg.obj,
                                    TYPE_LOAD_REFRESH_ERROR);
                            break;
                    }

                    break;//MESSAGE_OBTAION_DONGFANG_ARTICLE

            }
        }


    }

    private static void addAds(List<NewsBaseData> news, List<NewsBaseData> ads, int interval, int loadType) {
        int newSize = news.size();
        int[] adIndexs = getAdIndexs(newSize, interval);

        logDebug("addAds(...) 请求到的新闻的数量=" + newSize + "  要求每隔" + interval + "条新闻插入广告");

        int adSize = ads.size();
        int insertAdSize = adIndexs.length;
        List<KlauncherAdvertisement> inserAdxs = null;
        if (adSize < insertAdSize) {//所有广告的数量小于要插入的广告数量，则剩下的差额从ADX-SDK中取广告

            int difference=insertAdSize - adSize;
//            List<View> onFailviews=AdShowManager.getInstance().getAdxSdkOnFailViews(Utils
//                    .getContext(),difference);

            inserAdxs = KBannerAdUtil.getKBannerAdsForPlaceHolder(difference);

            ads.addAll(inserAdxs);
        }
        StringBuilder stringIndexs = new StringBuilder();
        int currentRefreshIndex = 0;
        int count = 0;
        for (int index : adIndexs) {
            stringIndexs.append(index + ",");

            switch (loadType) {
                case TYPE_LOAD_MORE_SUCCESS:
                    currentRefreshIndex = CommonShareData.getInt(LOAD_MORE_INDEX, -1) + 1;//默认为-1，代表一个也没加载
                    if (currentRefreshIndex >= ads.size()) {
                        currentRefreshIndex = 0;
                    }
                    CommonShareData.putInt(LOAD_MORE_INDEX, currentRefreshIndex);
                    break;
                case TYPE_LOAD_REFRESH_SUCCESS:
                    Collections.reverse(ads);//集合反转
                    currentRefreshIndex = CommonShareData.getInt(LOAD_REFRESH_AD_INDEX, -1) + 1;
                    if (currentRefreshIndex >= ads.size()) {
                        currentRefreshIndex = 0;
                    }
                    CommonShareData.putInt(LOAD_REFRESH_AD_INDEX, currentRefreshIndex);
                    break;
            }
            logDebug("addAds(...) currentRefreshIndex=" + currentRefreshIndex);
            news.add(index + count, ads.get(currentRefreshIndex));
            count++;
        }
        stringIndexs.substring(0, stringIndexs.length() - 1);
        logDebug("addAds(...) stringIndexs=" + stringIndexs.toString());
    }

    private static void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    private static int[] getAdIndexs(int total, int every) {
        int count = total / every;
        int[] indexs = new int[count];
        for (int i = 0; i < count; i++) {
            indexs[i] = (i + 1) * every;
        }
        return indexs;
    }


    /**
     * 下拉顶部叠加：保存最顶部idx
     * 上拉底部叠加：保存最底部idx
     * idx需要计算保存
     * pgnum直接请求的第几页就保存第几页（发起请求的时候会主动获取已保存的页数并计算+1）
     *
     * @param tabCategory
     * @param msg
     * @param loadType
     */
    public void saveParameter(String tabCategory, Message msg, int loadType) {
        Bundle bundle = msg.getData();
        String enKey = bundle.getString(PARAMETER_START_KEY, "");
        String newKey = bundle.getString(PARAMETER_NEW_KEY, "");
        long pgnum = bundle.getLong(PARAMETER_PGNUM, 0);//获取当前请求的pgnum
        int currentDataListSize = ((List<DongFangTouTiao>) msg.obj).size();//本次获取到的新闻 条数
        long idx = CommonShareData.getLong(getCompleteKeyName(tabCategory, PARAMETER_IDX, loadType), 0);
        switch (loadType) {
            case LoadNewsType.TYPE_FIRST:
            case LoadNewsType.TYPE_LOAD_MORE:
                idx = idx + currentDataListSize;
                CommonShareData.putLong(getCompleteKeyName(tabCategory, PARAMETER_IDX, loadType), idx);//保存加载跟多的idx
                break;
            case LoadNewsType.TYPE_LOAD_REFRESH:
                idx = idx - currentDataListSize;
                CommonShareData.putLong(getCompleteKeyName(tabCategory, PARAMETER_IDX, loadType), idx);//保存下拉刷新的idx
                break;
        }
        CommonShareData.putString(getCompleteKeyName(tabCategory, PARAMETER_START_KEY, loadType), enKey);//保存endKey
        CommonShareData.putString(getCompleteKeyName(tabCategory, PARAMETER_NEW_KEY, loadType), newKey);//保存newKey
        CommonShareData.putLong(getCompleteKeyName(tabCategory, PARAMETER_PGNUM, loadType), pgnum);//保存pgnum
    }


    /**
     * 根据loadType 获取在发起请求时pgnum应该传入的参数值
     *
     * @param category
     * @param filed
     * @param loadType
     * @return
     */
    public long requestParameterPgnum(String category, String filed, int loadType) {

        long pgnum = 1;//下次请求的正页数或者负页数.默认为第一次请求
        switch (loadType) {
            case LoadNewsType.TYPE_LOAD_REFRESH:
                pgnum = CommonShareData.getLong(getCompleteKeyName(category, filed, loadType), 0) - 1;//下次请求的负页数
                break;
            case LoadNewsType.TYPE_FIRST:
            case LoadNewsType.TYPE_LOAD_MORE:
                pgnum = CommonShareData.getLong(getCompleteKeyName(category, filed, loadType), 0) + 1;//下次请求的正页数
                break;
        }

        return pgnum;

    }


    public RequestBody getRequestBody(int loadType, TabInfo tabInfo, String key) {
        Activity activity = mContext.get();
        if(null==activity){
            return null;
        }


        long pgnum = 1;//下次请求的正页数或者负页数
        long idx = 0;//之前已经获取到的正数据的总和，或者之前已经获取到的负数据的总和

        idx = CommonShareData.getLong(getCompleteKeyName(tabInfo.getTabCategory(), PARAMETER_IDX, loadType), 0);
        //根据loadType确定：之前获取到的负数据或者正数据总和
        pgnum = requestParameterPgnum(tabInfo.getTabCategory(), PARAMETER_PGNUM, loadType);//根据loadType确定：下次请求的正负页数

        String startKey = CommonShareData.getString(getCompleteKeyName(tabInfo.getTabCategory(), PARAMETER_START_KEY,
                loadType), "");
        String newKey = CommonShareData.getString(getCompleteKeyName(tabInfo.getTabCategory(), PARAMETER_NEW_KEY,
                loadType), "");
        String imei = TelephonyUtils.getIMEI(activity);

        LogUtils.e("获取请求体：   loadType = " + loadType
                + " , startKey = " + startKey
                + " , newKey = " + newKey
                + " , pgnum = " + pgnum
                + " , idx = " + idx

        );

        //--------------
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add(PARAMETER_TYPE, tabInfo.getTabCategory())
                .add(PARAMETER_START_KEY, startKey)
                .add(PARAMETER_NEW_KEY, newKey)
                .add(PARAMETER_PGNUM, String.valueOf(pgnum))
                .add(PARAMETER_IMEI, imei)
                .add(PARAMETER_KEY, key)
                .add(PARAMETER_IDX, String.valueOf(idx))
                .add(PARAMETER_APP_TYPE_ID, VALUE_KDAOHANG)
                .add(PARAMETER_APP_VERSION, BuildConfig.VERSION_NAME)
                .add(PARAMETER_QID, VALUE_KDAOHANG)
                .add(PARAMETER_POSITION, CommonShareData.getString(CommonShareData.CURRENT_CITY, "北京"));


        Map<String,String> params=new HashMap<>(10);
        params.put(PARAMETER_TYPE,tabInfo.getTabCategory());
        params.put(PARAMETER_START_KEY,startKey);
        params.put(PARAMETER_NEW_KEY,newKey);
        params.put(PARAMETER_PGNUM, String.valueOf(pgnum));
        params.put(PARAMETER_IMEI,imei);
        params.put(PARAMETER_KEY,key);
        params.put(PARAMETER_IDX, String.valueOf(idx));
        params.put(PARAMETER_APP_TYPE_ID,VALUE_KDAOHANG);
        params.put(PARAMETER_APP_VERSION,BuildConfig.VERSION_NAME);
        params.put(PARAMETER_QID,VALUE_KDAOHANG);
        params.put(PARAMETER_POSITION,CommonShareData.getString(CommonShareData.CURRENT_CITY, "北京"));

        logDebug("getRequestBody() params="+params.toString());

        return formBodyBuilder.build();

    }

    public interface DongFangManagerListener {
        void setNewsList(List<NewsBaseData> newsBaseDataList, int loadType);
    }
}
