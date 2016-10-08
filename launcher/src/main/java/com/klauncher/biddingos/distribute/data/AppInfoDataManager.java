package com.klauncher.biddingos.distribute.data;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.klauncher.biddingos.distribute.model.AppInfo;
import com.klauncher.biddingos.impl.AdHelplerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xixionghui on 16/6/20.
 * 此Manager应该控制获取数据
 */
public class AppInfoDataManager {

    public static final String TAG = "pinXiaoTong";

    public static final String AD_PLACEMENT_MODULE_MO_REN = "0";
    public static final String AD_PLACEMENT_MODULE_XI_TONG_GONG_JU = "1";
    public static final String AD_PLACEMENT_MODULE_XIN_WEN_YUE_DU = "2";
    public static final String AD_PLACEMENT_MODULE_YING_YIN_YU_LE = "3";
    public static final String AD_PLACEMENT_MODULE_BIAN_JIE_SHENG_HUO = "4";

    int pageOffSet;
    String listId;
    AppInfoCallback mAppInfoCallback;
    Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            try {
                List<AppInfo> appList = (List<AppInfo>) msg.obj;
                if (null!=mAppInfoCallback) {
                    if (null!=appList&&appList.size()!=0) {//onSuccess
                        AppInfoDataManager.this.mAppInfoCallback.onSuccess(appList);
                    } else {//onFail
                        AppInfoDataManager.this.mAppInfoCallback.onFail();
                    }
                } else {
                    if (null!=mContext) Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("Kinflow", "handleMessage: 在处理获取到的AppInfoList时,发生异常" + e.getMessage());
            }

        }
    };

    /**
     *
     * @param context
     * @param pageOffSet 为0的时候是去网络请求最新的数据，非0去取缓存中的偏移量对应的数据，非0会是一样的.
     * @param listId listId是App分类的Id,这个ID由后台定义的榜单的ID决定.(需要与后台配置匹配起来)
     */
    public AppInfoDataManager(Context context,int pageOffSet, String listId) {
        this.mContext = context;
        this.pageOffSet = pageOffSet;
        this.listId = listId;
    }

    public void requestAppInfoList(AppInfoCallback appInfoCallback) {
        this.mAppInfoCallback = appInfoCallback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<AppInfo> appList = new ArrayList<>();
                    Map<Integer, AppInfo> appInfoMap = new AdHelplerImpl().getAds(pageOffSet, listId, new CreativeCacheManager());
                    appList.addAll(appInfoMap.values());
                    Message msg = Message.obtain();
                    msg.obj = appList;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e(TAG, "run: 在异步请求品效通的数据时,出错: "+e.getMessage());
                }

            }
        }).start();
    }

    public interface AppInfoCallback {
        void onSuccess(List<AppInfo> appList);

        void onFail();
    }
}
