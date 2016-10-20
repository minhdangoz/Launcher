package com.klauncher.kinflow.cards.model.server;

import android.content.Context;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.utilities.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

/**
 * 作者:  xixionghui
 * 创建时间:   16/10/14  下午2:13
 * 版本号:
 * 功能描述:
 */
public class CacheServerController {

    private static final String SERVER_CONTROLLER_LIST = "server_control_list";
    public Context mContext;

    public static CacheServerController instance = new CacheServerController();

    public static CacheServerController getInstance () {
        return instance;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public Context getContext () {
        synchronized (this) {
            try {
                if (null==mContext)
                    mContext = KLauncherApplication.mKLauncherApplication;
                return mContext;
            } catch (Exception e) {
                return KLauncherApplication.mKLauncherApplication;
            }
        }

    }

    private File getNavListFile() {
        return new File(getContext().getFilesDir().getAbsolutePath() + "/" + SERVER_CONTROLLER_LIST);
    }

    public void putServerController(String resultData){
        synchronized (this) {
            try {
                //        Log.e("Kinflow","要缓存的服务端控制器= "+resultData);
                if (!resultData.isEmpty()) {
                    FileUtils.write(resultData, getNavListFile(),
                            Charset.defaultCharset());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public ServerController getServerController(){
        synchronized (this) {
            ServerController serverController = null;

            try {
                File navFile = getNavListFile();
                if (navFile.exists()) {
                    String json = FileUtils.loadStringFromStream(new FileInputStream(navFile));
//                    Log.e("Kinflow","获取到缓存数据= "+json);
                    serverController = new ServerController(new JSONObject(json));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serverController;
        }

    }
}
