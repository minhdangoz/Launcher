package com.delong.assistance.api;


import com.delong.assistance.bean.RecommendAppList;
import com.delong.assistance.bean.UpgradeInfo;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by oeager on 2015/10/13.
 * email:oeager@foxmail.com
 */
public interface AssistService {

    @GET("launcher/recommend/guesslove")
    Observable<RecommendAppList> getAdList(@QueryMap Map<String, String> commonParams, @Query("category") String category, @Query("platform") String platform);

    @GET("launcher/update")
    Observable<UpgradeInfo> checkUpgrade(@QueryMap Map<String, String> commonParams, @Query("ver_code") int ver_code , @Query("platform") String platform);

}
