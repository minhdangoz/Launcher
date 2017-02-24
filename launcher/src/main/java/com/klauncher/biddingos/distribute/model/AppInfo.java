package com.klauncher.biddingos.distribute.model;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alpsdroid.ads.AppCreative;
import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;
import com.klauncher.biddingos.impl.AdHelplerImpl;

import java.io.File;
import java.util.List;

/**
 * Created by xixionghui on 16/6/20.
 * AppInfo必定返回的字段有两点控制:
 * 1,控制审核广告主投放广告;
 * 2,本地sdk也会过滤检查,参照AdInfo类171-205行
 */
public class AppInfo implements AppCreative, Parcelable {
    //原来数据
    /**
     * app_id : 1550kuSirtCx
     * app_package : com.pinxiaotong.test
     * app_logo : http://7xohoy.com2.z0.glb.qiniucdn.com/appinfo/20151030-14/141037.png
     * app_name : 狙击战神
     * app_desc : 狙击战神应用简介
     * app_size : 1402865
     * app_checksum : d5e23f1f8fe30ab579e7d3f61fd80df1
     * app_rank : 1
     * app_screenshots : ["http://7xohoy.com2.z0.glb.qiniucdn.com/appinfo/20151030-14/10d653d69c19050032039d55d8e26dbe.jpg","http://7xohoy.com2.z0.glb.qiniucdn.com/appinfo/20151030-14/319a3dedd16d757cce121bfc8a0652a0.jpg","http://7xohoy.com2.z0.glb.qiniucdn.com/appinfo/20151030-14/30585185adfffd38293c687d646c58e8.jpg","http://7xohoy.com2.z0.glb.qiniucdn.com/appinfo/20151030-14/28ad7f7b475ab9aa985a8020e2642c8d.jpg"]
     * app_price : 1.30
     * app_version : 1.0
     * app_version_code : 1
     * app_download_url : http://sandbox-adn.biddingos.com/fileserver/attach/channel/dl?pid=118&f=15504iOpm2Bw&zid=387&cid=24&bid=121&cb=4894ecc717858eef&rd=7c726185ae9b77a4f635d390d1f0ba13&uids=device.adid.1cf112170af77b94%7Cphone.deviceid.869139024815967%7Cdevice.mac.50:68:0a:d4:fe:ab%7Cdevice.serial.69P0216313005132%7Csim.serial.898600710116F0068595%7Csim.imsi.460007862151384&mid=2&channel=2.22.android.3.7.9
     * app_download_count : 1600000
     * app_release_note :
     * app_update_date : 2015-10-30 14:11:30
     * app_comment :
     * app_star : 0
     * app_image_url :
     * app_image_width : 0
     * app_image_height : 0
     * app_refresh : 0
     * app_support_os : 17
     * app_support_lang : ["zh"]
     * app_support_country : ["cn"]
     * maincategory : 游戏
     * secondcategory : 默认游戏
     * app_crc32 : 8a92eb26
     */

    private String app_id;
    private String app_package;
    private String app_logo;
    private String app_name;
    private String app_desc;
    private int app_size;
    private String app_checksum;
    private String app_rank;
    private double app_price;
    private String app_version;
    private String app_version_code;
    private String app_download_url;
    private String app_download_count;
    private String app_release_note;
    private String app_update_date;
    private String app_comment;
    private String app_star;
    private String app_image_url;
    private String app_image_width;
    private String app_image_height;
    private double app_refresh;
    private String app_support_os;
    private String maincategory;
    private String secondcategory;
    private String app_crc32;
    private List<String> app_screenshots;
    private List<String> app_support_lang;
    private List<String> app_support_country;

    @Override
    public String getAppID() {
        return getApp_id();
    }

    @Override
    public String getPackageName() {
        return getApp_package();
    }

    @Override
    public int getVersionCode() {
        return Integer.parseInt(getApp_version_code());
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_package() {
        return app_package;
    }

    public void setApp_package(String app_package) {
        this.app_package = app_package;
    }

    public String getApp_logo() {
        return app_logo;
    }

    public void setApp_logo(String app_logo) {
        this.app_logo = app_logo;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_desc() {
        return app_desc;
    }

    public void setApp_desc(String app_desc) {
        this.app_desc = app_desc;
    }

    public int getApp_size() {
        return app_size;
    }

    public void setApp_size(int app_size) {
        this.app_size = app_size;
    }

    public String getApp_checksum() {
        return app_checksum;
    }

    public void setApp_checksum(String app_checksum) {
        this.app_checksum = app_checksum;
    }

    public String getApp_rank() {
        return app_rank;
    }

    public void setApp_rank(String app_rank) {
        this.app_rank = app_rank;
    }

    public double getApp_price() {
        return app_price;
    }

    public void setApp_price(double app_price) {
        this.app_price = app_price;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_version_code() {
        return app_version_code;
    }

    public void setApp_version_code(String app_version_code) {
        this.app_version_code = app_version_code;
    }

    public String getApp_download_url() {
        return app_download_url;
    }

    public void setApp_download_url(String app_download_url) {
        this.app_download_url = app_download_url;
    }

    public String getApp_download_count() {
        return app_download_count;
    }

    public void setApp_download_count(String app_download_count) {
        this.app_download_count = app_download_count;
    }

    public String getApp_release_note() {
        return app_release_note;
    }

    public void setApp_release_note(String app_release_note) {
        this.app_release_note = app_release_note;
    }

    public String getApp_update_date() {
        return app_update_date;
    }

    public void setApp_update_date(String app_update_date) {
        this.app_update_date = app_update_date;
    }

    public String getApp_comment() {
        return app_comment;
    }

    public void setApp_comment(String app_comment) {
        this.app_comment = app_comment;
    }

    public String getApp_star() {
        return app_star;
    }

    public void setApp_star(String app_star) {
        this.app_star = app_star;
    }

    public String getApp_image_url() {
        return app_image_url;
    }

    public void setApp_image_url(String app_image_url) {
        this.app_image_url = app_image_url;
    }

    public String getApp_image_width() {
        return app_image_width;
    }

    public void setApp_image_width(String app_image_width) {
        this.app_image_width = app_image_width;
    }

    public String getApp_image_height() {
        return app_image_height;
    }

    public void setApp_image_height(String app_image_height) {
        this.app_image_height = app_image_height;
    }

    public double getApp_refresh() {
        return app_refresh;
    }

    public void setApp_refresh(double app_refresh) {
        this.app_refresh = app_refresh;
    }

    public String getApp_support_os() {
        return app_support_os;
    }

    public void setApp_support_os(String app_support_os) {
        this.app_support_os = app_support_os;
    }

    public String getMaincategory() {
        return maincategory;
    }

    public void setMaincategory(String maincategory) {
        this.maincategory = maincategory;
    }

    public String getSecondcategory() {
        return secondcategory;
    }

    public void setSecondcategory(String secondcategory) {
        this.secondcategory = secondcategory;
    }

    public String getApp_crc32() {
        return app_crc32;
    }

    public void setApp_crc32(String app_crc32) {
        this.app_crc32 = app_crc32;
    }

    public List<String> getApp_screenshots() {
        return app_screenshots;
    }

    public void setApp_screenshots(List<String> app_screenshots) {
        this.app_screenshots = app_screenshots;
    }

    public List<String> getApp_support_lang() {
        return app_support_lang;
    }

    public void setApp_support_lang(List<String> app_support_lang) {
        this.app_support_lang = app_support_lang;
    }

    public List<String> getApp_support_country() {
        return app_support_country;
    }

    public void setApp_support_country(List<String> app_support_country) {
        this.app_support_country = app_support_country;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "app_id='" + app_id + '\'' +
                ", app_package='" + app_package + '\'' +
                ", app_logo='" + app_logo + '\'' +
                ", app_name='" + app_name + '\'' +
                ", app_desc='" + app_desc + '\'' +
                ", app_size=" + app_size +
                ", app_checksum='" + app_checksum + '\'' +
                ", app_rank='" + app_rank + '\'' +
                ", app_price='" + app_price + '\'' +
                ", app_version='" + app_version + '\'' +
                ", app_version_code=" + app_version_code +
                ", app_download_url='" + app_download_url + '\'' +
                ", app_download_count='" + app_download_count + '\'' +
                ", app_release_note='" + app_release_note + '\'' +
                ", app_update_date='" + app_update_date + '\'' +
                ", app_comment='" + app_comment + '\'' +
                ", app_star='" + app_star + '\'' +
                ", app_image_url='" + app_image_url + '\'' +
                ", app_image_width='" + app_image_width + '\'' +
                ", app_image_height='" + app_image_height + '\'' +
                ", app_refresh='" + app_refresh + '\'' +
                ", app_support_os='" + app_support_os + '\'' +
                ", maincategory='" + maincategory + '\'' +
                ", secondcategory='" + secondcategory + '\'' +
                ", app_crc32='" + app_crc32 + '\'' +
                ", app_screenshots=" + app_screenshots +
                ", app_support_lang=" + app_support_lang +
                ", app_support_country=" + app_support_country +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.app_id);
        dest.writeString(this.app_package);
        dest.writeString(this.app_logo);
        dest.writeString(this.app_name);
        dest.writeString(this.app_desc);
        dest.writeInt(this.app_size);
        dest.writeString(this.app_checksum);
        dest.writeString(this.app_rank);
        dest.writeDouble(this.app_price);
        dest.writeString(this.app_version);
        dest.writeString(this.app_version_code);
        dest.writeString(this.app_download_url);
        dest.writeString(this.app_download_count);
        dest.writeString(this.app_release_note);
        dest.writeString(this.app_update_date);
        dest.writeString(this.app_comment);
        dest.writeString(this.app_star);
        dest.writeString(this.app_image_url);
        dest.writeString(this.app_image_width);
        dest.writeString(this.app_image_height);
        dest.writeDouble(this.app_refresh);
        dest.writeString(this.app_support_os);
        dest.writeString(this.maincategory);
        dest.writeString(this.secondcategory);
        dest.writeString(this.app_crc32);
        dest.writeStringList(this.app_screenshots);
        dest.writeStringList(this.app_support_lang);
        dest.writeStringList(this.app_support_country);
    }

    public AppInfo() {
    }

    protected AppInfo(Parcel in) {
        this.app_id = in.readString();
        this.app_package = in.readString();
        this.app_logo = in.readString();
        this.app_name = in.readString();
        this.app_desc = in.readString();
        this.app_size = in.readInt();
        this.app_checksum = in.readString();
        this.app_rank = in.readString();
        this.app_price = in.readDouble();
        this.app_version = in.readString();
        this.app_version_code = in.readString();
        this.app_download_url = in.readString();
        this.app_download_count = in.readString();
        this.app_release_note = in.readString();
        this.app_update_date = in.readString();
        this.app_comment = in.readString();
        this.app_star = in.readString();
        this.app_image_url = in.readString();
        this.app_image_width = in.readString();
        this.app_image_height = in.readString();
        this.app_refresh = in.readDouble();
        this.app_support_os = in.readString();
        this.maincategory = in.readString();
        this.secondcategory = in.readString();
        this.app_crc32 = in.readString();
        this.app_screenshots = in.createStringArrayList();
        this.app_support_lang = in.createStringArrayList();
        this.app_support_country = in.createStringArrayList();
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    //以上为获取到的品效通字段,以及自动生成的相关方法,下面是自己定义的必要字段
    public static final String TAG = "pinXiaoTong";
    boolean isDownloading = false;

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setIsDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    public void downloadAPK() {
        try {
            //判断是否正在下载
            if (isDownloading) {
                Toast.makeText(Setting.context, "正在下载中，请耐心等待...", Toast.LENGTH_LONG).show();
                LogUtils.w(TAG, "appInfo has been downloading apk file");
                return;
            }
            //日志提示
            LogUtils.i(TAG, "下载APK:" + app_download_url);
            Toast.makeText(Setting.context, "开始下载...", Toast.LENGTH_LONG).show();
            //对url判断
            DownloadManager downloadManager = (DownloadManager) Setting.context.getSystemService(Setting.context.DOWNLOAD_SERVICE);
            if (TextUtils.isEmpty(app_download_url) || "null".equals(app_download_url) || (!app_download_url.startsWith("http"))) {
                LogUtils.i(TAG, "url is null apkUrl=" + app_download_url);
//            Toast.makeText(Setting.context,"下载地址有误",Toast.LENGTH_SHORT).show();
                return;
            }
            //设置下载请求
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(app_download_url));
            request.setTitle("Downloading apk...");
            request.setMimeType("application/vnd.android.package-archive");
            //设置下载文件名称
            final String fileName = String.valueOf(System.currentTimeMillis()) + ".apk";
            LogUtils.i(TAG, "fileName:" + fileName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            //获取下载队列的id
            final long downloadId = downloadManager.enqueue(request);
            //注册下载结果的广播
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId == reference) {
                        LogUtils.i(TAG, "Downloaded end");
                        //设置不在下载中
//                    ad.setbIsDownloading(false);
                        setIsDownloading(false);
                        //检测下载文件完整性
                        String apkPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
                        if (AppUtils.isApkCanInstall(Setting.context, apkPath)) {
//                        downloadSucceed(zoneId,apkPath, sTiUrl, ad);
                            downloadSucceed(apkPath);
                        } else {
//                        Toast.makeText(Setting.context,"下载失败",Toast.LENGTH_SHORT).show();
                            LogUtils.d(TAG, "down failed");
                        }

                    }
                }
            };

            //注册广播接收器
            Setting.context.getApplicationContext().registerReceiver(receiver, filter);
            //修改状态
            isDownloading = true;
        } catch (Exception e) {
            Log.e(TAG, "downloadAPK: 下载apk时,发生异常:" + e.getMessage());
        }
    }

    private void downloadSucceed(String apkPath) {
        //下载完成,上报
        new AdHelplerImpl().notifyConversion(app_id);
        //下载完成,拉起安装界面
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        Setting.context.startActivity(intent);
    }

    /**
     * appinfo 是否有效可用
     * @return
     */
    public boolean isValid() {
        if (!TextUtils.isEmpty(app_id)
                && !TextUtils.isEmpty(app_name)
                && !TextUtils.isEmpty(app_logo)
                && !TextUtils.isEmpty(app_download_url)) {
            return true;
        } else {
            return false;
        }
    }
}
