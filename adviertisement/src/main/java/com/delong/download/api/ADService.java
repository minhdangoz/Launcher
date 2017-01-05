package com.delong.download.api;


import com.miguan.market.entries.CategoryModel;
import com.miguan.market.entries.CommentsResponse;
import com.miguan.market.entries.HotTitleBean;
import com.miguan.market.entries.ReplyResponse;
import com.miguan.market.entries.ServerAppDetailInfo;
import com.miguan.market.entries.LocalSimpleAppInfo;
import com.miguan.market.entries.SplashResponse;
import com.miguan.market.entries.VersionControl;
import com.miguan.market.entries.AppListResult;
import com.miguan.market.entries.GameRankResponse;
import com.miguan.market.entries.GameGiftResponse;
import com.miguan.market.entries.GiftToken;
import com.miguan.market.entries.AppRecommendAdInfo;
import com.miguan.market.entries.FloatAdInfo;
import com.miguan.market.entries.RecommendEntry;
import com.miguan.market.entries.QuickCompleteResponse;
import com.miguan.market.entries.SearchRecommendInfo;
import com.miguan.market.entries.HotGameListResponse;
import com.miguan.market.auth.TokenResponse;
import com.miguan.market.auth.User;
import com.miguan.market.entries.AppInfo;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by oeager on 2015/10/13.
 * email:oeager@foxmail.com
 */
public interface ADService {


    @GET("game/token/token_api")
    Observable<TokenResponse> getToken(@Query("device_id") String deviceId, @Query("ver_code") int versionCode, @Query("channel_id") String channel_id);

    @GET("game/pic/lunch")
    Observable<SplashResponse> getSplashUri(@QueryMap Map<String, String> commonParam, @Query("v") int versionCode);

    @GET("game/pic/carousel")
    Observable<AppRecommendAdInfo> getHomeAd(@QueryMap Map<String, String> commonParam);

    @GET("game/pic/carouseltool")
    Observable<AppRecommendAdInfo> getToolsAd(@QueryMap Map<String, String> commonParam);

    @GET("game/datasource/category")
    Observable<CategoryModel> getGameCategory(@QueryMap Map<String, String> commonParam);

    @GET("game/app/appmessage")
    Observable<ServerAppDetailInfo> getAppDetailInfo(@QueryMap Map<String, String> commonParam, @Query("app_id") long queryParam, @Query("pkg_name") String pkg_name, @Query("app_category_id") int app_category_id);

    @GET("game/app/applist")
    Observable<AppListResult> getGameList(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum, @Query("source_id") int sourceId, @Query("type") int type);

    @GET("game/app/systemClean")
    Observable<AppInfo> getSysCleanAppDetail(@QueryMap Map<String, String> commonParam);

    @GET("game/search/app_rec")
    Observable<SearchRecommendInfo> getSearchRecommendInfo(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum);


    @POST("game/app/applist_update")
    Observable<AppListResult> getAppUpgradeList(@QueryMap Map<String, String> commonParam, @Body List<LocalSimpleAppInfo> list);

    @GET("search_miguan_api/search/auto_keyword")
    Observable<QuickCompleteResponse> getSearchHotKey(@QueryMap Map<String, String> commonParam, @Query("key_words") String keyWords);

    @GET("game/update/inspection_ver?actionid=21")
    Observable<VersionControl> checkAppVersionUp(@QueryMap Map<String, String> commonParam, @Query("cur_ver_code") int currentVersionCode);

    @GET("game/log/uploadhwtoken?actionid=25")
    Observable<Void> bindPushToken(@QueryMap Map<String, String> commonParam, @Query("hwtoken") String token);

    @GET("game/recom/recommended_api")
    Observable<RecommendEntry> getHomeGame(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/recom/recommended_tools")
    Observable<RecommendEntry> getHomeTools(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/present/getAppPresentLimit")
    Observable<GameGiftResponse> getAppDetailGift(@QueryMap Map<String, String> commonParam, @Query("app_id") long appId, @Query("pkg_name") String pkg_name, @Query("app_category_id") int app_category_id);

    @GET("game/present/myPresent")
    Observable<GameGiftResponse> getMyGameGiftList(@QueryMap Map<String, String> commonParam);

    @GET("game/present/getAppPresent")
    Observable<GameGiftResponse> getGameHoloGift(@QueryMap Map<String, String> commonParam, @Query("app_id") long appId, @Query("pkg_name") String pkg_name, @Query("app_category_id") int app_category_id, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/present/acquirePresent")
    Observable<GiftToken> requireGift(@QueryMap Map<String, String> commonParam, @Query("app_id") long appId, @Query("pkg_name") String pkg_name, @Query("app_category_id") int app_category_id, @Query("gift_pkg_id") String giftPkgId);

    @GET("game/ranking/list")
    Observable<GameRankResponse> getRank(@QueryMap Map<String, String> commonParam);

    @GET("game/ranking/nameList")
    Observable<HotTitleBean> getHotTitleList(@QueryMap Map<String, String> commonParam);

    @GET("game/recom/hot")
    Observable<HotGameListResponse> getHotGameList(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/recom/special")
    Observable<RecommendEntry> getTopicList(@QueryMap Map<String, String> commonParam, @Query("page_id") int pageId, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/app/appListChange")
    Observable<RecommendEntry.Model> getModelRefresh(@QueryMap Map<String, String> commonParam, @Query("page_size") int pageSize, @Query("page_num") int pageNum, @Query("source_id") int sourceId, @Query("type") int type);

    @GET("search_miguan_api/search/searchresult")
    Observable<AppListResult> getSearchResult(@QueryMap Map<String, String> commonParam, @Query("key_words") String keywords, @Query("page_size") int pageSize, @Query("page_num") int pageNum);

    @GET("game/pic/floatAd")
    Observable<FloatAdInfo> getFloatAd(@QueryMap Map<String, String> commonParam);

    @FormUrlEncoded
    @POST
    Observable<User> login(@Url String url, @QueryMap Map<String, String> commonParams, @Field("login_type") int loginType, @Field("device_id") String deviceId, @Field("origin_data") String originData, @Field("auth_support") String authSupport);

    @GET
    Observable<Void> logOut(@Url String url, @QueryMap Map<String, String> commonParams, @Query("login_type") int loginType, @Query("device_id") String deviceId, @Query("auth_id") String authId, @Query("user_id") String userId);

    @GET("game/clean/suggestion")
    Observable<Void> suggestion(@QueryMap Map<String, String> commonParams, @Query("user_id") String userId, @Query("suggestion") String suggestion, @Query("phone_number") String phoneNumber);

    @GET("game/recom/clean_recommend")
    Observable<RecommendEntry> getCleanRecommend(@QueryMap Map<String, String> commonParams, @Query("user_id") String userId, @Query("device_id") String deviceId);

    @GET("game/app/tagRecommend")
    Observable<AppListResult> getAppRecommendList(@QueryMap Map<String, String> commonParams, @Query("app_id") long appId, @Query("app_category_id") int categoryId);

    @GET("game/download/recommend")
    Observable<AppListResult> getDownloadRecommend(@QueryMap Map<String, String> commonParams);

    @GET("game/get_app_comments")
    Observable<CommentsResponse> getComments(@QueryMap Map<String, String> commonParams, @Query("user_id") String userId, @Query("app_id") long appId, @Query("pkg_name") String pkg, @Query("app_category_id") int categoryId, @Query("filter") String filter, @Query("page_index") int pageIndex, @Query("page_size") int page_size);

    @FormUrlEncoded
    @POST("game/post_comment")
    Observable<CommentsResponse.CommentData> postComment(@QueryMap Map<String, String> commonParams, @Field("user_id") String userID, @Field("app_id") long appId, @Field("pkg_name") String pkg, @Field("app_category_id") int categoryId, @Field("star") float star, @Field("content") String content, @Field("target_version_name") String targetVersionName, @Query("target_version_code") int targetVersionCode);

    @GET("game/get_replies")
    Observable<ReplyResponse> getReplies(@QueryMap Map<String, String> commonParams, @Query("user_id") String userId, @Query("comment_id") long commentId, @Query("page_index") int pageIndex, @Query("page_size") int pageSize);

    @FormUrlEncoded
    @POST("game/post_reply")
    Observable<ReplyResponse.ReplyData> postReply(@QueryMap Map<String, String> commonParams, @Field("comment_id") long commentId, @Field("user_id") String userId, @Field("reply_content") String replyContent);

    @FormUrlEncoded
    @POST("game/post_comment_follow")
    Observable<Void> postCommentFollow(@QueryMap Map<String, String> commonParams, @Field("comment_id") long commentId, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("game/post_reply_follow")
    Observable<Void> postReplyFollow(@QueryMap Map<String, String> commonParams, @Field("reply_id") int replyId, @Field("comment_id") long commentId, @Field("user_id") String userId);

//    @FormUrlEncoded
//    @POST("game/cancel_commend_follow")
//    Observable<Void> cancelCommentFollow(@QueryMap Map<String,String> commonParams,@Field("comment_id") int commentId,@Field("user_id")String userId);
//
//    @FormUrlEncoded
//    @POST("game/cancel_reply_follow")
//    Observable<Void> cancelReplyFollow(@QueryMap Map<String,String> commonParams,@Field("reply_id") int replyId,@Field("user_id")String userId);
}
