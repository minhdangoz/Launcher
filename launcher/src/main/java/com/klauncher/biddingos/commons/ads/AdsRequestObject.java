package com.klauncher.biddingos.commons.ads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.DisplayMetrics;

import com.alpsdroid.ads.Placement;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AESUtils;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.commons.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Edward on 2015/12/8.
 */
public class AdsRequestObject {
    private static final String TAG = "AdsRequestObject";

    private String ts = "";
    private String url = "";
    private String strBody = "";
    private boolean bUsingListId;
    public static int FLOWAD=0x000000;
    public static int FEEDSAD=0x000001;
    public static int COMMONAD=0x000002;
    public static int SEARCHAD=0x000003;
    public static int BANNERAD=0x000004;
    public static int ADHELPER=0x000005;
    public List<Placement> placementList;
    /**
     * sessionid 作为整个应用打开关闭周期的会话ID
     */
    private static String sessionid = "";

    public static void setSessionid() {
        String input = ""+ Math.random()*10000 + Math.random()*10000 + System.currentTimeMillis() + Build.VERSION.RELEASE + Build.MODEL + Build.ID + AppUtils.getUserUniqueID().toString();
        sessionid = StringUtils.getMD5(input);
        LogUtils.i(TAG, sessionid);
    }


    /* 广告请求对象
     * @param bFlowAds 是否流量广告位，false 表示推荐广告位
     */
    public AdsRequestObject(int adType, String listId, List<Placement> placementList, String keyword,  List<String> pkgNameList, Boolean bUsingListId) {
        this.placementList = placementList;
        this.url = getRequestUrl();
        this.bUsingListId = bUsingListId;
        JSONObject body = new JSONObject();
        try {
            body.put("channel", Setting.MID+"."+Setting.AFID+".android."+Setting.SDK_VERSION);

            if(adType!=FLOWAD) {
                if(bUsingListId) {//只使用listId
                    body.put("list", listId);
                }else {//只使用zoneid
                    String zoneUrl = "";
                    for (int i = 0; i < placementList.size(); i++) {
                        int zoneid = placementList.get(i).getZoneid();
                        if (0 < zoneid) {
                            zoneUrl += zoneid + "|";
                        }
                    }
                    body.put("zones", zoneUrl.substring(0, zoneUrl.length() - 1));
                }
            }

            String uids = "";
            JSONArray jsUids = AppUtils.getUserUniqueID();
            try {
                for (int i=0; i<jsUids.length(); ++i) {
                    uids = uids + jsUids.getString(i) + ((jsUids.length()-1)==i?"":"|");
                }
            }catch (JSONException e) {
                LogUtils.e(TAG, "cannot get uids", e);
            }

            body.put("uids", uids);

            body.put("mode", "sdk");
            body.put("sv", Setting.SDK_VERSION);

            JSONObject context = new JSONObject();
            if(adType==FEEDSAD) {//zoneid 加dp dpi
                DisplayMetrics metric = Setting.context.getResources().getDisplayMetrics();
                int width = metric.widthPixels;  // 屏幕宽度（像素）
                int widthdp=px2dip(Setting.context,width);
                int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
                LogUtils.i(TAG, "widthdp=" + widthdp + " densityDpi=" + densityDpi);
                context.put("width",widthdp);
                context.put("dpi",densityDpi);
//                context.put("width","1000");
//                context.put("height","560");
            }
            if (null!=keyword && !keyword.equals("")) {
                context.put("keyword", keyword);
            }
            int page_size = 0;
            if(null!=pkgNameList) {
                JSONArray refer_contents = new JSONArray();
                int i = 0;
                for (String pkgName : pkgNameList ) {
                    JSONObject obj = new JSONObject();
                    obj.put("package", pkgName);
                    obj.put("position", i++);
                    refer_contents.put(obj);
                }
                context.put("refer_contents", refer_contents);
                page_size = pkgNameList.size();
            }

            context.put("platform", Setting.SYSTEM_PLATFORM);
            context.put("offset_start", 0);
            context.put("page_size", page_size);
            int nNetWorkType = AppUtils.getNetWorkType();
            if (nNetWorkType== ConnectivityManager.TYPE_WIFI) {
                context.put("net_type", "wifi");
            }else if(nNetWorkType== ConnectivityManager.TYPE_MOBILE) {
                context.put("net_type", "mobile");
            }

            String net_provider = AppUtils.getNetProvider();
            if(!net_provider.equals("")) {
                context.put("net_provider", net_provider);
            }
            if(null!=listId) {
                context.put("piece_info", listId);
            }
            context.put("sessionid", AdsRequestObject.sessionid);

            body.put("context", context);

        }catch (JSONException e) {
            LogUtils.e(TAG, "", e);
        }

        this.strBody = body.toString();

    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    private String getRequestUrl() {
        String url = "";
        url = Setting.getBiddingOS_AD_CALL_URL();

        Map<String, String> param = new HashMap<>();
        ts = String.valueOf(System.currentTimeMillis() / 1000);
        param.put("ts", ts);
        param.put("debug", "0");
        param.put("kid", Setting.KID);
        param.put("encrypt", "aes");
        String strParam = "";
        for (Map.Entry<String, String> entry : param.entrySet()) {
            strParam = strParam +"&"+ entry.getKey()+"="+entry.getValue();
        }

        url = url + strParam;

        return url;
    }

    public String getUrl() {
        return url;
    }

    public boolean isUsingListId() {
        return bUsingListId;
    }

    public String getBody() {
        LogUtils.e(TAG, strBody);
        return strBody;
    }

    public String getSignedAndEncryptString() {
        String signedString = StringUtils.signature(strBody, ts);
        //LogUtils.e("1=>", signedString);
        String encryptString = AESUtils.encode(Setting.SECRET, signedString);
        //LogUtils.e("2=>", encryptString);
        return encryptString;
    }


}