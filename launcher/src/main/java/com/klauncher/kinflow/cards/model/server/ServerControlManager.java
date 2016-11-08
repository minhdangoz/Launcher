package com.klauncher.kinflow.cards.model.server;

import android.content.Context;
import android.text.TextUtils;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.utilities.FileUtils;
import com.klauncher.kinflow.utilities.KinflowLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 服务端的控制都在里面:最新数据---->上次存储数据----->默认数据
 * @author 习雄辉
 * @date 创建时间: 16/10/14 上午11:09
 * @Description 直接对外提供打开方式列表.内部异步获取打开方式列表.
 * 有了广告控制,需要修改的地方:
 * 1.assets 广告加入---默认不加入;
 * 2.ServerController实体类的isNull方法.
 * 3.本类的getDefaultServerControl方法解析;
 * 4.MainControl.combinationData方法
 * 5.CacheServerController.getServerController
 */
public class ServerControlManager {
    private Context mContext;
    private static ServerControlManager mServerControllerManager = new ServerControlManager();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public static ServerControlManager getInstance(){
        return mServerControllerManager;
    }

    public void init(Context context) {
        setContext(context);
        CacheServerController.getInstance().init(getContext());
    }

    private void setContext(Context context) {
        if (null == context) {
            mContext = KLauncherApplication.mKLauncherApplication;
        } else {
            mContext = context;
        }
    }

    private Context getContext() {
        if (null == mContext)
            mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    public ServerController getServerController() {
        try {
            //获取本地存储控制器
            ServerController serverController = getLocalServerControl();
            //发起请求,请求最新数据
            requestServerController();
            return serverController;
        } catch (Exception e) {
            KinflowLog.w("ServerControlManager.getServerController出错 : " + e.getMessage());
            return null;
        }
    }

    private ServerController getLocalServerControl() {
        try {
            //如果本地存储了服务控制器,则使用本地存储的服务器数据,否则使用默认
            ServerController serverController = CacheServerController.getInstance().getServerController();
            if (null==serverController||serverController.isNull()) {//如果服务器配置获取到的为空,则使本地默认
                return getDefaultServerControl();
            } else {//本地存储的上次服务器数据
                return serverController;
            }
        } catch (Exception e) {
            KinflowLog.w("ServerControlManager.getLocalServerControl出错 : " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取assets中默认的ServerControl
     *
     * @return
     */
    private ServerController getDefaultServerControl() {
        try {
            ServerController defaultServerController = new ServerController();
            List<NewsOpenControl> newsOpenControlList = new ArrayList<>();
            List<AdControl> adControlList = new ArrayList<>();
            InputStream is = KLauncherApplication.mKLauncherApplication.getAssets().open("default_server_control");
            String json = FileUtils.loadStringFromStream(is);
            JSONObject localJsonRoot = new JSONObject(json);
            //解析新闻部分
            JSONArray newsOpenControlLocalJsonArray = localJsonRoot.optJSONArray("news");
            int newsOpenControlLocalJsonLength = newsOpenControlLocalJsonArray.length();
            for (int x = 0; x < newsOpenControlLocalJsonLength; x++) {
                newsOpenControlList.add(new NewsOpenControl(newsOpenControlLocalJsonArray.optJSONObject(x)));
            }
            //解析广告部分---等添加广告控制后,这里再添加代码,先预留位置
            JSONArray adOpenControlLocalJsonArray = localJsonRoot.optJSONArray("ads");
            int adOpenControlLocalJsonLength = adOpenControlLocalJsonArray.length();
            for (int y = 0; y < adOpenControlLocalJsonLength; y++) {
                adControlList.add(new AdControl(adOpenControlLocalJsonArray.getJSONObject(y)));
            }
            //返回数据
            defaultServerController.setNewsOpenControlList(newsOpenControlList);
            defaultServerController.setAdControlList(adControlList);
            return defaultServerController;
        } catch (Exception e) {
            KinflowLog.w("ServerControlManager.getDefaultServerControl出错 : " + e.getMessage());
            return null;
        }
    }

    private void requestServerController() {
        try {
            //创建请求对象
            Request request = new Request.Builder()
                    .url(Const.KINFLOW2_SERVER_CONTROL)
//                    .url(Const.KINFLOW2_SERVER_CONTROL_TEST)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    KinflowLog.w("ServerControlManager.requestServerController获取数据失败onFailure: call=" + call.toString() + "  错误信息=" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //1,如果响应失败
                    if (!response.isSuccessful()) {
                        KinflowLog.w("ServerControlManager.requestServerController获取数据失败onResponse,响应失败");
                        response.body().close();
                        return;
                    }

                    //2,响应体有错误
                    String responseBodyStr = null;
                    try {
                        responseBodyStr = response.body().string();
                    } catch (Exception e) {
                        KinflowLog.w("ServerControlManager.requestServerController获取数据失败onResponse,有响应,但是响应失败");
                        response.body().close();
                        return;
                    }

                    //3,响应体非空判断
                    if (TextUtils.isEmpty(responseBodyStr.trim())) {//响应体为空
                        KinflowLog.w("ServerControlManager.requestServerController获取数据失败onResponse,有响应,但是响应体为空");
                        response.body().close();
                        return;
                    }

                    //4,解析服务控制器
                    parseServerController(responseBodyStr);
                    response.body().close();
                }
            });
        } catch (Exception e) {
            KinflowLog.w("ServerControlManager.requestServerController出错 : " + e.getMessage());
        }

    }

    private void parseServerController(String responseJsonStr) {
        try {
            JSONObject responseJsonObject = new JSONObject(responseJsonStr);
            ServerController serverController = new ServerController(responseJsonObject);
            if (serverController.isNull()) {//为空啥也不做

            } else {//非空,则存储
                CacheServerController.getInstance().putServerController(responseJsonStr);
            }
        } catch (Exception e) {
            KinflowLog.w("ServerControlManager.parseServerController出错 : " + e.getMessage());
        }

    }

}
