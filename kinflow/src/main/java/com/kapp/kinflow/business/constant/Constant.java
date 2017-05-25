package com.kapp.kinflow.business.constant;

import android.util.Log;

import com.kapp.kinflow.business.util.UrlUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * description：常量
 * <br>author：caowugao
 * <br>time： 2017/05/18 14:15
 */

public class Constant {

    private static final String TAG = Constant.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Constant() {
    }

//    /**
//     * 每日美图
//     */
//    public static final String DAILY_MITO_URL = "http://www.opgirl" +
//            ".cn/gallery/list?did=6&start=0&end=15&pl=0&strategy=1&cid=2&st=0";

    /**
     * 品牌优惠
     */
    private static final String BASE_BAND_OFFERS_URL = "http://api.kkeymall.com/v1/getGoods";
    /**
     * 品牌优惠更多精品
     */
    public static final String BAND_OFFERS_MORE_URL = "http://www.kkeymall.com/";
    /**
     * 小说推荐
     */
    public static final String NOVEL_RECOMMEND_URL = "http://prerelease.iyd" +
            ".cn/mobile/data/kaixin/sort?channel_id=kaixinh5&version=066700029";
    /**
     * 小说搜索接口
     */
    private static final String BASE_NOVEL_SEARCH_URL = "http://prerelease.iyd" +
            ".cn/mobile/data/search";
    /**
     * 每日美图渠道控制
     */
//    public static final String DAILY_MITO_CTROL_URL = "http://www.opgirl.cn/native/list?did=6";
    public static final String DAILY_MITO_CTROL_URL = "http://opapi.cnlofter.com/native/list";

    //
//    public static String getDailyMitoUrl(int id) {
////        http://opapi.cnlofter.com/gallery/list?did=6&start=0&end=9&cid=2&pl=0&strategy=1&st=0
////        String baseUrl = "http://opapi.cnlofter.com/gallery/list?did=6&start=0&end=9&cid=";
////        String endUrl = "&pl=0&strategy=1&st=0";
//
//        String baseUrl = "http://opapi.cnlofter.com/gallery/list?did=6&start=0&end=9&lid=";
//        String endUrl = "&pl=0&strategy=1&st=1";
//
//
//        return baseUrl + id + endUrl;
//    }
    public static String getDailyMitoUrl(int did, int lid, String galleryparam) {
//        http://opapi.cnlofter.com/gallery/list?did=6&start=0&end=9&cid=2&pl=0&strategy=1&st=0
//        String baseUrl = "http://opapi.cnlofter.com/gallery/list?did=6&start=0&end=9&cid=";
//        String endUrl = "&pl=0&strategy=1&st=0";

        String baseUrl = "http://opapi.cnlofter.com/gallery/list?did=" + did + "&start=0&end=19&lid=" + lid;
        String endUrl = galleryparam;
        return baseUrl + endUrl;
    }

    public static String getBandOffersUrl(int pageNumber, int pageSize) {
        //http://api.kkeymall.com/v1/getGoods?data=[{"page":"0","size":"10"}]
        try {
            JSONArray datas = new JSONArray();
            JSONObject data = new JSONObject();
            data.put("page", String.valueOf(pageNumber));
            data.put("size", String.valueOf(pageSize));
            datas.put(0, data);
            String result = BASE_BAND_OFFERS_URL + "?data=" + datas.toString();
            logDebug("getBandOffersUrl() result=" + result);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getNovelSearchUrl(String keyWord, int pageNumber, int pageSize) {
        Map<String, String> params = new HashMap<>(4);
        params.put("channel_id", "kaixinh5");
        params.put("keyword", keyWord);
        params.put("pageIndex", String.valueOf(pageNumber));
        params.put("pageNum", String.valueOf(pageSize));
        String paramsUrl = UrlUtil.pieceParmasByGET(params);
        return BASE_NOVEL_SEARCH_URL + paramsUrl;
    }

    private static void logDebug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
