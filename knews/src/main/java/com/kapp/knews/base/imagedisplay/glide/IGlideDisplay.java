package com.kapp.knews.base.imagedisplay.glide;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.kapp.knews.base.imagedisplay.IImageDisplay;
import com.kapp.knews.base.imagedisplay.option.DisplayOption;


/**
 * description：Glide图片展示接口
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:24
 */

public interface IGlideDisplay extends IImageDisplay {
    void display(Activity activity, ImageView imageView, String url);

    void display(Fragment fragment, ImageView imageView, String url);

    void display(Activity activity, ImageView imageView, String url, int width, int height);

    void display(Fragment fragment, ImageView imageView, String url, int width, int height);

    void display(Activity activity, ImageView imageView, String url, int width, int height, DisplayOption option);

    void display(Fragment fragment, ImageView imageView, String url, int width, int height, DisplayOption option);
    void displayAnimation(Context context, ImageView imageView, String url, int animaResId);


}
