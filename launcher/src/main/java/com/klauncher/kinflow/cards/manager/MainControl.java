package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.apistore.sdk.ApiCallBack;
import com.baidu.apistore.sdk.ApiStoreSDK;
import com.baidu.apistore.sdk.network.Parameters;
import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.utilities.FileUtils;
import com.klauncher.launcher.R;
import com.klauncher.kinflow.cards.CardsListManager;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.common.utils.CacheLocation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.common.utils.MathUtils;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.weather.model.Weather;
import com.kyview.natives.NativeAdInfo;
import com.ss.android.sdk.minusscreen.model.Article;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by xixionghui on 16/4/12.
 */
public class MainControl {

    private Context mContext;
    //    private Handler mHandler;
    private ResponseListener mListener;
    private Semaphore mRequestSemaphore;
    private int permitCount;//信号量
    private int[] mRequestTypes;
    private List<CardInfo> mCardInfoList = new ArrayList<>();
    List<HotWord> mRandomHotWordList = new ArrayList<>();
    List<Navigation> mNavigationList = new ArrayList<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mRequestSemaphore.release();
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD://获取百度热词
                    handleHotWords((List<HotWord>) msg.obj);
                    log("获取到百度热词");
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION://获取Navigation
                    if (null != msg.obj)
                        mNavigationList = (List<Navigation>) msg.obj;
                    Collections.sort(mNavigationList);
                    log("获取到导航");
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN://获取一点咨询
                    log("获取到一点资讯,msg.what=" + msg.what);
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO:
                    log("获取到今日头条");
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YOKMOB:
                    log("获取到yokmob");
                    break;
//                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW:
//                    log("获取到adview");
//                    break;
                default:
                    log("what the fuck ??  msg.what=" + msg.what);
                    break;

            }
            //当信号量全部收到
            if (mRequestSemaphore.availablePermits() == permitCount) {
//            mListener.onCompleted(mRandomHotWordList,mNavigationList,mCardInfoList);
                mListener.onCompleted();
                for (int i = 0; i < MainControl.this.mRequestTypes.length; i++) {
                    switch (MainControl.this.mRequestTypes[i]) {
                        case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                            mListener.onHotWordUpdate(mRandomHotWordList);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                            mListener.onNavigationUpdate(mNavigationList);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_CARD:
//                            mCardInfoList
                            List<CardInfo> filiterCardInfoList = new ArrayList<>();
                            for (CardInfo cardInfo : mCardInfoList) {
                                switch (cardInfo.getCardSecondTypeId()) {
                                    case CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN:
                                    case CardIdMap.CARD_TYPE_NEWS_YD_REDIAN:
                                    case CardIdMap.CARD_TYPE_NEWS_YD_YULE:
                                    case CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG:
                                    case CardIdMap.CARD_TYPE_NEWS_YD_QICHE:
                                    case CardIdMap.CARD_TYPE_NEWS_YD_LVYOU:
                                        YDCardContentManager ydCardContentManager = (YDCardContentManager) cardInfo.getmCardContentManager();
                                        if (null!=ydCardContentManager.getmYiDianModelList()&&ydCardContentManager.getmYiDianModelList().size()!=0)
                                            filiterCardInfoList.add(cardInfo);
                                        break;
                                    case CardIdMap.CARD_TYPE_NEWS_TT_REDIAN://头条
                                        TTCardContentManager ttCardContentManager = (TTCardContentManager) cardInfo.getmCardContentManager();
                                        List<Article>[] mArticleListArrays = ttCardContentManager.getArticleListArrays();
                                        if (null!=mArticleListArrays&&null!=mArticleListArrays[0]&&0!=mArticleListArrays[0].size())
                                            filiterCardInfoList.add(cardInfo);
                                        break;
                                    case CardIdMap.ADVERTISEMENT_YOKMOB:
                                        YMCardContentManager ymCardContentManager = (YMCardContentManager) cardInfo.getmCardContentManager();
                                        if (null!=ymCardContentManager.getImageUrl()&&null!=ymCardContentManager.getClickUrl())
                                            filiterCardInfoList.add(cardInfo);
                                        break;
//                                    case CardIdMap.ADVERTISEMENT_ADVIEW:
//                                        break;
//                                    case CardIdMap.ADVERTISEMENT_BAIDU:
//                                        break;
                                }
                            }
                            mListener.onCardInfoUpdate(filiterCardInfoList);
                            //非adViewCard更新完毕,再启用adViewCard请求
                            //单独请求adView
                            ADVCardContentManager advCardContentManager = (ADVCardContentManager) adViewCardInfo.getmCardContentManager();
                            advCardContentManager.requestCardContent(singleRequestHandler, adViewCardInfo);
                            break;

                    }
                }
            }
        }
    };

    private Handler singleRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAION_CITY_NAME://获取到城市名称
                    log("获取到城市名称");
                    if (null!=msg.obj)
                    try {
                        String cityName = URLEncoder.encode((String) msg.obj, "UTF-8");
                        parseWeather(cityName);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW:
                    List<NativeAdInfo> nativeAdInfoList = (List<NativeAdInfo>) msg.obj;
                    if (null == nativeAdInfoList || nativeAdInfoList.size() == 0) {
                        log("MainControl获取adview失败");
                        Log.i("MyInfo", "MainControl 获取AdView失败");
                        return;
                    } else {
                        log("MainControl获取adview成功");
//                        ADVCardContentManager manager = (ADVCardContentManager) adViewCardInfo.getmCardContentManager();
                        Log.i("MyInfo", "MainControl 获取AdView成功");
                        mListener.onAddAdview(adViewCardInfo);
                    }
                    break;
            }
        }
    };

    /**
     * 将获取到的热词随机出3条.并加入到mRandomHotWordList集合.以便在Completed的时候传入
     *
     * @param mHotWordList
     */
    private void handleHotWords(List<HotWord> mHotWordList) {
        mRandomHotWordList.clear();
        if (null != mHotWordList && mHotWordList.size() != 0) {
            List<Integer> randomNumbers = MathUtils.randomNumber(mHotWordList.size());
            for (int i = 0; i < randomNumbers.size(); i++) {
                int position = randomNumbers.get(i);
                if (i == 0 || i == 1 || i == 2) {
                    mRandomHotWordList.add(mHotWordList.get(position));
                }
            }
        }

    }

    public MainControl(Context mContext, ResponseListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
        getAdViewCardInfo();
    }

    static final String ADVIEW_CARD_PATH = "adview_card";
    CardInfo adViewCardInfo;

    private void getAdViewCardInfo() {
        try {
            InputStream is = mContext.getAssets().open(ADVIEW_CARD_PATH);
            String json = FileUtils.loadStringFromStream(is);
            adViewCardInfo = new CardInfo(new JSONObject(json), mContext);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 可请求一个或者多个
     *
     * @param msgWhats
     */
    public void asynchronousRequest(int... msgWhats) {
        this.mRequestTypes = msgWhats;
//        this.mCardInfoList = cardInfoList;
        this.mCardInfoList = CardsListManager.getInstance().getInfos();
        permitCount = mCardInfoList.size() + 2;
        log("请求的总数=" + permitCount + " ,其中card请求个数=" + mCardInfoList.size());
        mRequestSemaphore = new Semaphore(permitCount);
        for (int what : msgWhats) {
            try {
                switch (what) {
                    case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                        mRequestSemaphore.acquire();
                        new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD).run(Const.URL_HOT_WORD);
                        break;
                    case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                        mRequestSemaphore.acquire();
                        Calendar latestModifiedCalendar = DateUtils.getInstance().millis2Calendar(CommonShareData.getString(Const.NAVIGATION_LOCAL_LAST_MODIFIED, String.valueOf(Calendar.getInstance().getTimeInMillis())));
                        String navagationUpdateInterval = CommonShareData.getString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL, String.valueOf(Calendar.getInstance().getTimeInMillis()));
                        int second = (int) (Long.valueOf(navagationUpdateInterval) / 1000);
                        latestModifiedCalendar.add(Calendar.SECOND, second);
                        if (latestModifiedCalendar.before(Calendar.getInstance())) {
                            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION).run(Const.NAVIGATION_GET);
                        } else {
                            Message msg = Message.obtain();
                            msg.arg1 = AsynchronousGet.SUCCESS;
                            msg.what = MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION;
                            mHandler.sendMessage(msg);
                        }
                        break;
                    case MessageFactory.MESSAGE_WHAT_OBTAION_CARD:
                        for (CardInfo cardInfo : mCardInfoList) {
                            BaseCardContentManager cardContentManager = cardInfo.getmCardContentManager();
                            mRequestSemaphore.acquire();
                            cardContentManager.requestCardContent(mHandler, cardInfo);
                        }
                        break;
                    default:
                        log("未知请求,what=" + what);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 通过定位获取城市名称，将城市名称逆解析后，再请求天气。（自动完成）
     */
    public void obtainCityName() {
        String param = "?location=" + CacheLocation.getInstance(mContext).getLatLng() + "&output=json&key=" + Const.BAIDU_APIKEY;
        String url = Const.OBTAIN_CITY_NAME + param;
        try {
            new AsynchronousGet(singleRequestHandler, MessageFactory.MESSAGE_WHAT_OBTAION_CITY_NAME).run(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseWeather(String cityName) {
        Parameters para = new Parameters();
        para.put("cityname", cityName);
        ApiStoreSDK.execute(Const.OBTAION_WEATHER_BY_CITY_NAME,
                ApiStoreSDK.GET,
                para,
                new ApiCallBack() {

                    @Override
                    public void onSuccess(int status, String responseString) {
                        try {
                            JSONObject jsonAll = new JSONObject(responseString);
                            int resultCode = jsonAll.getInt("errNum");
                            if (resultCode != 0) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_obtain_weather_success), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject jsonWeather = (JSONObject) jsonAll.get("retData");

                            String city = jsonWeather.getString("city");
                            String pinyin = jsonWeather.getString("pinyin");
                            String citycode = jsonWeather.getString("citycode");
                            String date = jsonWeather.getString("date");
                            String time = jsonWeather.getString("time");
                            String postCode = jsonWeather.getString("postCode");
                            Double longitude = jsonWeather.getDouble("longitude");
                            Double latitude = jsonWeather.getDouble("latitude");
                            String altitude = jsonWeather.getString("altitude");
                            String weather = jsonWeather.getString("weather");
                            String temp = jsonWeather.getString("temp");
                            String l_tmp = jsonWeather.getString("l_tmp");
                            String h_tmp = jsonWeather.getString("h_tmp");
                            String WD = jsonWeather.getString("WD");
                            String WS = jsonWeather.getString("WS");
                            String sunrise = jsonWeather.getString("sunrise");
                            String sunset = jsonWeather.getString("sunset");
                            Weather weatherObject = new Weather(city, pinyin, citycode, date, time, postCode, longitude, latitude, altitude, weather, temp, l_tmp, h_tmp, WD, WS, sunrise, sunset);
                            mListener.onWeatherUpdate(weatherObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(int status, String responseString, Exception e) {
                        Log.i("sdkdemo", "errMsg: " + (e == null ? "" : e.getMessage()));
                    }

                });
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

    public interface ResponseListener {
//        void onCompleted(List<HotWord> mHotWordList, List<Navigation> mNavigationList, List<CardInfo> cardInfoList);

        void onWeatherUpdate(Weather weather);

        void onHotWordUpdate(List<HotWord> hotWordList);

        void onNavigationUpdate(List<Navigation> navigationList);

        void onCardInfoUpdate(List<CardInfo> cardInfoList);

        void onAddAdview(CardInfo adViewCardInfo);//获取到adview之后,在CardsAdapter中添加这个adview

        void onCompleted();//此方法调用在最前面,通知所有信号量已经释放.所有数据已获取.
    }
}
