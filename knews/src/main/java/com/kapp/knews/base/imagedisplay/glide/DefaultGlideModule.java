package com.kapp.knews.base.imagedisplay.glide;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;
import com.kapp.knews.utils.SystemUtil;

import java.io.File;



/**
 * description：glide全局配置
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:17
 */

public class DefaultGlideModule implements GlideModule {
    private static final String TAG = DefaultGlideModule.class.getSimpleName();

    /**
     * Lazily apply options to a {@link GlideBuilder} immediately before the Glide singleton is
     * created.
     * <p/>
     * <p>
     * This method will be called once and only once per implementation.
     * </p>
     *
     * @param context An Application {@link Context}.
     * @param builder The {@link GlideBuilder} that will be used to create Glide.
     */
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        logDebug("applyOptions(...) ...");
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取系统分配给应用的总内存大小
        int memoryCacheSize = maxMemory / 8;//设置图片内存缓存占用八分之一
        //设置内存缓存大小
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));


        File cacheDir = context.getExternalCacheDir();//指定的是数据的缓存地址
        int diskCacheSize = 1024 * 1024 * 30;//最多可以缓存多少字节的数据
        //设置磁盘缓存大小
        String cacheName = SystemUtil.getApplicationName(context);
        cacheName = (null == cacheName || "".equals(cacheName)) ? "RVAlbum" : cacheName;
        builder.setDiskCache(new DiskLruCacheFactory(cacheDir.getPath(), cacheName, diskCacheSize));


        //设置图片解码格式
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

        //设置BitmapPool缓存内存大小
        builder.setBitmapPool(new LruBitmapPool(memoryCacheSize));

//        为了使缩略图请求正确工作，实现类必须把请求根据Priority优先级排好序。
//        builder.setDiskCacheService(ExecutorService service);
//        builder.setResizeService(ExecutorService service);
    }

    private void logDebug(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * Lazily register components immediately after the Glide singleton is created but before any requests can be
     * started.
     * <p/>
     * <p>
     * This method will be called once and only once per implementation.
     * </p>
     *
     * @param context An Application {@link Context}.
     * @param glide   The newly created Glide singleton.
     */
    @Override
    public void registerComponents(Context context, Glide glide) {
        // register ModelLoaders here.
        logDebug("registerComponents(...) ...");
    }
}
