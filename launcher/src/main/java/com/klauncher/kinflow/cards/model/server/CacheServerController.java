package com.klauncher.kinflow.cards.model.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.utilities.KinflowLog;

/**
 * 作者:  xixionghui
 * 创建时间:   16/10/14  下午2:13
 * 版本号:
 * 功能描述:
 */
public class CacheServerController {

    public static final String fileName = "cacheServerController";
    public static final String keyName = "serverControllerKey";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private Context mContext;
    private static CacheServerController instance = new CacheServerController();

    private CacheServerController() {
    }


    public void init(Context context) {
        synchronized (this) {
            setContext(context);
            sharedPreferences = getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            gson = new GsonBuilder().create();
        }
    }

    public void setContext(Context context) {
        if (null == context) {
            mContext = KLauncherApplication.mKLauncherApplication;
        } else {
            mContext = context;
        }
    }

    public Context getContext() {
        if (null == mContext)
            mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    public static CacheServerController getInstance() {
        return instance;
    }

    public String serverController2String(ServerController serverController) {
        synchronized (this) {
            return gson.toJson(serverController);
        }
    }

    public ServerController string2ServerController(String serverControllerString) {
        synchronized (this) {
            if (TextUtils.isEmpty(serverControllerString)) {//传进数据为空
                return null;
            } else {
                return gson.fromJson(serverControllerString, ServerController.class);
            }
        }
    }

    public void putServerController(ServerController serverController) {
        try {
            synchronized (this) {
                if (null == serverController) return;
                editor.putString(keyName, serverController2String(serverController));
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ServerController getServerController() {
        try {
            synchronized (this) {
                String serverControlJson = sharedPreferences.getString(keyName,null);
                ServerController serverController = string2ServerController(serverControlJson);;
                if (null==serverController||serverController.isNull()) {
                    return null;
                } else {
                    return serverController;
                }
            }
        } catch (Exception e) {
            KinflowLog.w("cacheServerController.getServerController出错: " + e.getMessage());
            return null;
        }
    }

    public void clearServerController() {
        try {
            synchronized (this) {
                editor.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
