package com.klauncher.biddingos.commons.net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.Build;

import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AppUtils;
import com.klauncher.biddingos.commons.utils.LogUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 * 用来处理HTTP请求，带cookie、gzip压缩、连接复用，以及处理代理、超时、跳转、UA、鉴权、异常日志等
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";

    /**
     * 初始化代理及相关连接池
     * @param context
     */
    public static synchronized void init(Context context) {

    }

    /**
     * 获取User Agent
     * @return
     */
    public static String getUserAgent() {
        PackageInfo packageInfo = AppUtils.getPackageInfoByPackageName(Setting.context.getPackageName());
        String versionCode = null!=packageInfo? String.valueOf(packageInfo.versionCode):"";
        String versionName = null!=packageInfo? String.valueOf(packageInfo.versionName):"";
        return String.format("BiddingOS %s (Android %s; %s Build/%s %s/%s[%s])",
                Setting.SDK_VERSION,
                Build.VERSION.RELEASE,
                Build.MODEL,
                Build.ID,
                Setting.context.getPackageName(),
                versionName,
                versionCode);
    }

    /**
     * 执行Http请求
     * @return
     */
    public static HttpResponse execute(HttpRequest input) throws Exception {

        LogUtils.d(TAG, "http req: " + (null == input ? null : input.getUrl()));

        URL url = null;
        try {
            url = new URL(input.getUrl());
        } catch (MalformedURLException e) {
            LogUtils.e(TAG, "", e);
            throw e;
        }
        Proxy proxy = getClientProxy();
        DataOutputStream dataOutputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            if(AppUtils.getNetWorkType()== ConnectivityManager.TYPE_MOBILE && null!=proxy) {
                urlConnection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestProperty("Content-Encoding", (String) input.getHeaderValue("Content-Encoding"));
            urlConnection.setRequestProperty("User-Agent", (String) input.getHeaderValue("User-Agent"));
            urlConnection.setConnectTimeout(input.getTimeout());
            urlConnection.setReadTimeout(input.getTimeout());
            switch (input.getMethod()) {
                case GET:
                    urlConnection.connect();
                    break;
                case POST:
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.connect();
                    OutputStream os;
                    os = urlConnection.getOutputStream();
                    dataOutputStream=new DataOutputStream(os);
                    dataOutputStream.writeBytes(input.getReqBody());
                    dataOutputStream.flush();
                    break;
            }

            InputStream is;
            is=urlConnection.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String line="";
            StringBuffer sb=new StringBuffer();
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            return new HttpResponse(HttpResponseStatus.getStatus(urlConnection.getResponseCode()), sb.toString());
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            throw e;
        }finally {
            try{
                if(null!=dataOutputStream)  dataOutputStream.close();
                if(null!=urlConnection)     urlConnection.disconnect();
            }catch (IOException e){
                LogUtils.e(TAG, "", e);
            }

        }

    }


    /**
     * 获取移动端代理信息
     */
    public static Proxy getClientProxy() {
        boolean isIcsOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress = "";
        int proxyPort = -1;
        if( isIcsOrLater )
        {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            try {
                proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
            }catch (NumberFormatException e) {
                LogUtils.e(TAG, e.toString());
                proxyPort = -1;
            }

        }
        else
        {
            if(AppUtils.checkPermission("android.permission.ACCESS_NETWORK_STATE")) {
                proxyAddress = android.net.Proxy.getHost(Setting.context);
                proxyPort = android.net.Proxy.getPort( Setting.context );
            }

        }

        if(proxyPort==-1 || proxyPort==0 || proxyAddress==null || proxyAddress=="" || proxyAddress.equals("")  ) {
            return null;
        }

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));

    }

}
