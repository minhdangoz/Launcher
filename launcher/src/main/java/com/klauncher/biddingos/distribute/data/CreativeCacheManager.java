package com.klauncher.biddingos.distribute.data;

import android.util.Log;

import com.alpsdroid.ads.CachedRemoteCreative;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.distribute.model.AppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreativeCacheManager extends CachedRemoteCreative<AppInfo> {

    private Gson mGson = new GsonBuilder().create();

    @Override
    public Map<String, AppInfo> generateAppInfo(Map<String, String> appId_assets) {

        Map<String, AppInfo> appId_appInfo = new HashMap<>();

        try {
            for (Map.Entry<String, String> entry:appId_assets.entrySet()) {
                String appId = entry.getKey();
                String asset = entry.getValue(); //BiddIngOS的广告素材在这里取
                LogUtils.e("MyInfo", "获取到的AppInfo的Json数据:\n" + asset);
                Log.e("CreativeCacheManager", "获取到的AppInfo的Json数据:\n" + asset);
                //AppInfo appInfo = mGson.fromJson(asset,AppInfo.class);
                AppInfo appInfo = resolveJson(asset);
                Log.e("CreativeCacheManager",appInfo.toString());
                appId_appInfo.put(appId,appInfo);
            }
        } catch (JsonSyntaxException eJsonSyntax) {
            Log.e("Kinflow", "generateAppInfo发生异常:" + eJsonSyntax.getMessage());
        } catch (Exception e) {
            Log.e("Kinflow", "generateAppInfo发生异常:" + e.getMessage());
        }

        return appId_appInfo;
    }

    private AppInfo resolveJson(String json){
        AppInfo appInfo = new AppInfo();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            appInfo.setMaincategory(jsonObject.getString("maincategory"));
            appInfo.setApp_release_note(jsonObject.getString("app_release_note"));
            appInfo.setApp_price(jsonObject.getDouble( "app_price"));
            appInfo.setApp_crc32(jsonObject.getString( "app_crc32"));
            appInfo.setApp_version(jsonObject.getString("app_version"));
            appInfo.setApp_logo(jsonObject.getString("app_logo"));
            appInfo.setApp_download_url(jsonObject.getString("app_download_url"));
            appInfo.setApp_version_code(jsonObject.getString("app_version_code"));
            appInfo.setApp_name(jsonObject.getString("app_name"));
            appInfo.setApp_update_date(jsonObject.getString("app_update_date"));
            appInfo.setApp_package(jsonObject.getString("app_package"));
            appInfo.setApp_support_os(jsonObject.getString("app_support_os"));
            appInfo.setApp_desc(jsonObject.getString("app_desc"));
            appInfo.setApp_refresh(jsonObject.getDouble("app_refresh"));
            appInfo.setApp_download_count(jsonObject.getString("app_download_count"));
            appInfo.setApp_star(jsonObject.getString("app_star"));
            appInfo.setApp_comment(jsonObject.getString("app_comment"));
            appInfo.setApp_checksum(jsonObject.getString("app_checksum"));
            appInfo.setApp_rank(jsonObject.getString("app_rank"));
            appInfo.setSecondcategory(jsonObject.getString("secondcategory"));
            appInfo.setApp_size(jsonObject.getInt("app_size"));
            appInfo.setApp_id(jsonObject.getString("app_id"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return appInfo;
    }
}
