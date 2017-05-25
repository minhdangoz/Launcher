package com.kapp.knews.base.imagedisplay;

import android.content.Context;
import android.widget.ImageView;

import com.kapp.knews.base.imagedisplay.option.DisplayOption;


/**
 * description：图片展示接口
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:29
 */

public interface IImageDisplay {
    void display(Context context, ImageView imageView, String url);


    void displayLocalFile(Context context, ImageView imageView, String path);

    void displayLocalDrawable(Context context, ImageView imageView, int drawableResId);

    void display(Context context, ImageView imageView, String url, int width, int height);

    void display(Context context, ImageView imageView, String url, int width, int height, DisplayOption option);
    void display(Context context, ImageView imageView, String url, DisplayOption option);

    void displayGif(Context context, ImageView imageView, String url, int width, int height, DisplayOption option);

    void displayGif(Context context, ImageView imageView, String url);

    void displayCorner(Context context, ImageView imageView, String url, int corner);

    void displayCircle(Context context, ImageView imageView, String url);

    void clearDiskCache(Context context);

    void clearMemory(Context context);
}

