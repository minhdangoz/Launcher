package com.klauncher.biddingos.commons.analytics;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户事件相关上下文对象
 */
public class UserContext {

    /**
     * 产生事件的应用信息
     */
    private String app;

    /**
     * 产生事件时的用户角色
     */
    private String role;

    /**
     * 产生事件的应用界面信息
     */
    private String scene;


    private String versionName;

    private String versionCode;

    public UserContext(String app, String role, String scene, String versionName, String versionCode) {
        this.app = app;
        this.role = role;
        this.scene = scene;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    /**
     * 转化为JSON对象
     * @return JSON对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("app", app);
            json.put("role", role);
            json.put("scene", scene);
            json.put("version", versionName);
            json.put("code", versionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
