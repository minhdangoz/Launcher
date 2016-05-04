package com.klauncher.getui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.klauncher.launcher.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PushDemoReceiver extends BroadcastReceiver {

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    //public static StringBuilder payloadData = new StringBuilder();
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        //Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");

                //String taskid = bundle.getString("taskid");
                //String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                //boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
                //System.out.println("第三方回执接口调用" + (result ? "成功" : "失败"));

                if (payload != null) {
                    String data = new String(payload);

                    Log.d("GetuiSdkDemo", "receiver payload : " + data);

                    //payloadData.append(data);
                    Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                    handleMsgData(context, data);


                }
                break;

            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                Toast.makeText(context, cid, Toast.LENGTH_LONG).show();
                break;

            case PushConsts.THIRDPART_FEEDBACK:
                /*
                 * String appid = bundle.getString("appid"); String taskid =
                 * bundle.getString("taskid"); String actionid = bundle.getString("actionid");
                 * String result = bundle.getString("result"); long timestamp =
                 * bundle.getLong("timestamp");
                 *
                 * Log.d("GetuiSdkDemo", "appid = " + appid); Log.d("GetuiSdkDemo", "taskid = " +
                 * taskid); Log.d("GetuiSdkDemo", "actionid = " + actionid); Log.d("GetuiSdkDemo",
                 * "result = " + result); Log.d("GetuiSdkDemo", "timestamp = " + timestamp);
                 */
                break;

            default:
                break;
        }

    }

    private static int NOTIFICATION_FLAG = 1;

    /**
     * 处理推送消息 显示通知
     *
     * @param data
     */
    private void handleMsgData(Context context, String data) {
        //data type,content  1,title,pkgname,pkgActname,downloadUrl ; 2,title,url
        //Json格式{"type":"1","title":"title","pkgname":"pkgname","startActname":"startActname","url":"url"}
        //{"type":"2","title":"title","url":"url"}
        //显示出来统计   点击事件统计
        //String spStr[] = data.split(",");
        Gson gson = new GsonBuilder().create();
        GeituiElement element = gson.fromJson(data, GeituiElement.class);

        int intType = element.getType();

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        switch (intType) {
            case 1://打开APP
                String title = element.getTitle();
                final String icon = element.getIcon();
                String pkgName = element.getPkgname();
                String startActName = element.getStartActname();
                String downUrl = element.getUrl();

                Notification myNotify = new Notification();
                myNotify.icon = R.mipmap.ic_launcher;
                myNotify.tickerText = title;
                myNotify.when = System.currentTimeMillis();
                myNotify.flags = Notification.FLAG_AUTO_CANCEL;
                final RemoteViews rv = new RemoteViews(context.getPackageName(),
                        R.layout.view_my_notify);
                rv.setTextViewText(R.id.text_content, title);
                rv.setImageViewResource(R.id.iv_notify_icon,
                        R.mipmap.ic_launcher);
                myNotify.contentView = rv;

                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            URL url = new URL(params[0]);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(6000);//设置超时
                            conn.setDoInput(true);
                            conn.setUseCaches(false);//不缓存
                            conn.connect();
                            int code = conn.getResponseCode();
                            Bitmap bitmap = null;
                            if(code==200) {
                                InputStream is = conn.getInputStream();//获得图片的数据流
                                bitmap = BitmapFactory.decodeStream(is);
                            }
                            return bitmap;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            return null;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Bitmap result) {
                        super.onPostExecute(result);
                        if (result != null) {
                            rv.setImageViewBitmap(R.id.iv_notify_icon, result);
                        }
                    }
                }.execute(icon);


                PendingIntent contentIntent = null;
                if (isAppInstalled(context, pkgName)) {//已安装 打开
                    //clickintent //点击 Intent
                    Intent clickIntent = new Intent(context, NotifyClickService.class); //点击 Intent
                    clickIntent.putExtra("type", "startApp");
                    clickIntent.putExtra("pkgName", pkgName);
                    clickIntent.putExtra("startActName", startActName);

                    contentIntent = PendingIntent.getService(context, 1,
                            clickIntent, PendingIntent.FLAG_ONE_SHOT);
                } else {//下载apk
                    Intent clickIntent = new Intent(context, NotifyClickService.class);
                    clickIntent.putExtra("type", "downApk");
                    clickIntent.putExtra("downUrl", downUrl);
                    contentIntent = PendingIntent.getService(context, 1,
                            clickIntent, PendingIntent.FLAG_ONE_SHOT);
                }

                myNotify.contentIntent = contentIntent;
                manager.notify(NOTIFICATION_FLAG, myNotify);


                break;
            case 2://打开广告
                if (!TextUtils.isEmpty(element.getUrl())) {
                    String adTitle = element.getTitle();
                    final String adicon = element.getIcon();
                    String adUrl = element.getUrl();
                    Notification adNotify = new Notification();
                    adNotify.icon = R.mipmap.ic_launcher;
                    adNotify.tickerText = adTitle;
                    adNotify.when = System.currentTimeMillis();
                    adNotify.flags = Notification.FLAG_AUTO_CANCEL;
                    final RemoteViews adRv = new RemoteViews(context.getPackageName(),
                            R.layout.view_my_notify);
                    adRv.setTextViewText(R.id.text_content, adTitle);
                    adRv.setImageViewResource(R.id.iv_notify_icon,
                            R.mipmap.ic_launcher);
                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... params) {
                            try {
                                URL url = new URL(params[0]);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setConnectTimeout(6000);//设置超时
                                conn.setDoInput(true);
                                conn.setUseCaches(false);//不缓存
                                conn.connect();
                                int code = conn.getResponseCode();
                                Bitmap bitmap = null;
                                if(code==200) {
                                    InputStream is = conn.getInputStream();//获得图片的数据流
                                    bitmap = BitmapFactory.decodeStream(is);
                                }
                                return bitmap;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                return null;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            super.onPostExecute(result);
                            if (result != null) {
                                adRv.setImageViewBitmap(R.id.iv_notify_icon, result);
                            }
                        }
                    }.execute(adicon);
                    Intent clickIntent = new Intent(context, NotifyClickService.class); //点击通知之后要发送的广播
                    clickIntent.putExtra("type", "openAd");
                    clickIntent.putExtra("url", adUrl);
                    //
                    PendingIntent adIntent = PendingIntent.getService(context, 1,
                            clickIntent, PendingIntent.FLAG_ONE_SHOT);

                    adNotify.contentIntent = adIntent;
                    adNotify.contentView = adRv;
                    manager.notify(NOTIFICATION_FLAG, adNotify);
                    //显示统计
                    Log.d("BaiduStatist","reportShowStatist()  1111111");
                    BaiduStatist.reportShowStatist();
                    Log.d("BaiduStatist","reportShowStatist() 2222222");
                }
                break;
            default://不处理
                break;
        }


    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }
}

