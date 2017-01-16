package com.delong.download.wrapper;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.delong.download.R;
import com.delong.download.bean.ServerAppInfo;
import com.delong.download.utils.AppUtils;
import com.delong.download.utils.ImageUtils;

/**
 * Created by oeager on 2015/10/13.
 * email:oeager@foxmail.com
 */
public class DrawableLoadingWrapper {


    @BindingAdapter("imageUrl")
    public static void displayAppImageWithPlaceHolder(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext()).load(imageUrl).error(R.mipmap.ic_default).fitCenter().crossFade().into(imageView);
    }

    @BindingAdapter("appInfo")
    public static void disPlayAppIcon(final ImageView imageView, ServerAppInfo appInfo) {
//        if (appInfo.status.get() == ServerAppInfo.ADD_ITEM) {
//            imageView.setBackgroundResource(R.mipmap.ic_add);
//            return;
//        }
        if (appInfo.status.get() == ServerAppInfo.INSTALLED) {
            Drawable appIconDrawable = AppUtils.getAppIconDrawable(imageView.getContext(), appInfo.pkgName);
            if (appIconDrawable!=null){
                Bitmap bitmap = ImageUtils.drawableToBitmap(appIconDrawable);
                Bitmap bitmap1 = ImageUtils.bitmapRound(bitmap);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap1);
                imageView.setImageDrawable(bitmapDrawable);
            }

        } else {
            Glide.with(imageView.getContext()).load(appInfo.iconUrl).error(R.mipmap.ic_default).into(new ImageViewTarget<GlideDrawable>(imageView) {
                @Override
                protected void setResource(GlideDrawable resource) {
                    if (resource!=null){
                        Bitmap bitmap = ImageUtils.drawableToBitmap(resource);
                        Bitmap bitmap1 = ImageUtils.bitmapRound(bitmap);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap1);
                        view.setImageDrawable(bitmapDrawable);
                    }
                }
            });
        }
    }




}
    