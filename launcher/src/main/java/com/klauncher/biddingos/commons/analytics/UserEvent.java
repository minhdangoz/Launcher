package com.klauncher.biddingos.commons.analytics;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * 用户事件对象
 */
public class UserEvent {

    public static final String EVENT_APPSCAN_APPLIST    = "appscan.applist";
    public static final String EVENT_APPSCAN_INSTALLED  = "appscan.installed";
    public static final String EVENT_APPSCAN_UNINSTALLED = "appscan.uninstalled";
    public static final String EVENT_APPSCAN_UPDATED    = "appscan.updated";
    public static final String EVENT_ADS_REQUEST        = "ads.request";
    public static final String EVENT_ADS_IMPRESSION    = "ads.impression";
    public static final String EVENT_ADS_VIEW           = "ads.view";
    public static final String EVENT_ADS_DOWNLOAD_START = "ads.download.start";
    public static final String EVENT_ADS_DOWNLOAD_END  = "ads.download.end";
    public static final String EVENT_ADS_INSTALL        = "ads.install";

    /**
     * 用户事件相关上下文
     */
    private UserContext context;

    /**
     * 事件发生时的UNIX时间戳（UTC时间）
     */
    private Date timestamp;

    /**
     * 事件类型ID
     */
    private String type;

    /**
     * 事件目标信息
     */
    private String target;

    /**
     * 事件持续时间，单位：毫秒
     */
    private long duration;

    /**
     * 事件附加数据
     */
    private String extra;

    public UserEvent(UserContext context, String type, String target, long duration, String extra, Date timestamp) {
        this.context = context;
        this.timestamp = timestamp;
        this.type = type;
        this.target = target;
        this.duration = duration;
        this.extra = extra;
    }

    /**
     * 转化为JSON对象
     * @return JSON对象
     */
    public JSONObject toJson() {
        JSONObject jsonContextEvent = new JSONObject();
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            jsonContextEvent.put("timestamp", cal.getTime().getTime()/1000);
            jsonContextEvent.put("type", type);
            jsonContextEvent.put("target", target);
            jsonContextEvent.put("duration", duration);
            jsonContextEvent.put("extra", extra);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonEvent = new JSONObject();
        try {
            jsonEvent.put("context", context.toJson());
            jsonEvent.put("event", jsonContextEvent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonEvent;
    }
}
