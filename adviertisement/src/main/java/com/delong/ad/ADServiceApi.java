package com.delong.ad;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v4.util.SparseArrayCompat;

import com.delong.ad.config.OkHttpFactory;
import com.delong.download.DownloadManager;
import com.delong.download.api.ADService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.x91tec.appshelf.components.AppHook;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Administrator on 2017/1/5.
 */

public class ADServiceApi {

    private static final SparseArrayCompat<Object> mGlobalObjects = new SparseArrayCompat<>();

    private static final int KEY_OBJECT_MAPPER = 0;

    private static final int KEY_HTTP_SERVICE = 1;

    public static void init(Application ctx) {
        AppHook.get().hookApplicationWatcher(ctx);
        initHttp(ctx);
        initDownloader(ctx);
    }

    private static void initHttp(Context ctx) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(OkHttpFactory.getSocketFactory(), OkHttpFactory.getX509TrustManager())
                .hostnameVerifier(OkHttpFactory.getHostNameVerifier())
                .followRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(new Cache(Environment.getDataDirectory(), 1024 * 1024))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);//缩放办理出
        objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);//解析器忽略未定义定段
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        JacksonConverterFactory factory = JacksonConverterFactory.create(objectMapper);
        saveObject(KEY_OBJECT_MAPPER, objectMapper);
        Retrofit retrofit = new Retrofit.Builder().baseUrl("")
                .client(okHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        ADService service = retrofit.create(ADService.class);
        saveObject(KEY_HTTP_SERVICE, service);
    }

    private static void saveObject(int key, Object object) {
        mGlobalObjects.put(key, object);
    }

    public static ObjectMapper getObjectMapper() {
        return getObjects(KEY_OBJECT_MAPPER);
    }

    private static <O> O getObjects(int key) {
        Object o = mGlobalObjects.get(key);
        if (o != null) {
            return (O) o;
        }
        return null;
    }

    public static ADService service() {
        return getObjects(KEY_HTTP_SERVICE);
    }

    private static void initDownloader(Context ctx) {
        DownloadManager.init(Environment.getExternalStorageDirectory() + "/klauncher/download/", ctx);
    }

}
