package com.kapp.knews.base.imagedisplay.glide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kapp.knews.base.imagedisplay.glide.transformation.CircleBitmapTransfer;
import com.kapp.knews.base.imagedisplay.glide.transformation.RoundTransform;
import com.kapp.knews.base.imagedisplay.option.AnimationDisplayOption;
import com.kapp.knews.base.imagedisplay.option.DisplayOption;
import com.kapp.knews.base.imagedisplay.option.LoadAfterDisplayOption;
import com.kapp.knews.base.imagedisplay.option.RoundDisplayOption;
import com.kapp.knews.base.imagedisplay.option.ThumbnailDisplayOption;

import java.io.File;



/**
 * description：Glide加载
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:10
 */

public class GlideDisplay implements IGlideDisplay {


    private static final String TAG = GlideDisplay.class.getSimpleName();
    private static GlideDisplay instance;

    private static DisplayOption defaultOption;

    private GlideDisplay() {
    }

    public static GlideDisplay getInstance() {
        if (null == instance) {
            synchronized (GlideDisplay.class) {
                if (null == instance) {
                    instance = new GlideDisplay();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化默认的DisplayOption
     *
     * @param option
     */
    public void init(DisplayOption option) {
        if (null == defaultOption) {
            defaultOption = new DisplayOption(option);
        }
    }

    /**
     * glide默认会根据imageview的大小裁剪图片进行缓存
     *
     * @param context
     * @param imageView
     * @param url
     */
    @Override
    public void display(Context context, ImageView imageView, String url) {
        if (null == defaultOption) {
            throw new IllegalArgumentException(TAG + " application中要先初始化 GlideDsiplay.init(option);");
        }
        Glide.with(context)
                .load(url)
                .centerCrop()
                .thumbnail(defaultOption.thumbnailScale)
                .placeholder(defaultOption.placeHolderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .error(defaultOption.errorResId)
                .into(imageView);
    }

    @Override
    public void displayGif(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade().into(imageView);
        //如果为ALL和Result就不行
    }

    @Override
    public void displayLocalFile(Context context, ImageView imageView, String path) {
        Glide.with(context)
                .load(new File(path))
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.5f)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void displayLocalDrawable(Context context, ImageView imageView, int drawableResId) {
        Glide.with(context)
                .load(drawableResId)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.5f)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void display(Activity activity, ImageView imageView, String url) {
        Glide.with(activity)
                .load(url)
                .centerCrop()
                .thumbnail(defaultOption.thumbnailScale)
                .placeholder(defaultOption.placeHolderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(defaultOption.errorResId)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void display(Fragment fragment, ImageView imageView, String url) {
        Glide.with(fragment)
                .load(url)
                .centerCrop()
                .thumbnail(defaultOption.thumbnailScale)
                .placeholder(defaultOption.placeHolderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(defaultOption.errorResId)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void display(Activity activity, ImageView imageView, String url, int width, int height) {
        Glide.with(activity)
                .load(url)
                .centerCrop()
                .override(width, height)
                .thumbnail(defaultOption.thumbnailScale)
                .placeholder(defaultOption.placeHolderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(defaultOption.errorResId)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void display(Fragment fragment, ImageView imageView, String url, int width, int height) {
        Glide.with(fragment)
                .load(url)
                .centerCrop()
                .override(width, height)
                .thumbnail(defaultOption.thumbnailScale)
                .placeholder(defaultOption.placeHolderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(defaultOption.errorResId)
                .crossFade()
                .into(imageView);
    }

    @Override
    public void display(Activity activity, final ImageView imageView, String url, int width, int height, DisplayOption
            option) {

        if (option instanceof AnimationDisplayOption) {//加载动画
            AnimationDisplayOption animationDisplayOption = (AnimationDisplayOption) option;
            Glide.with(activity)
                    .load(url)
                    .centerCrop()
                    .thumbnail(animationDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸，不设置的话glide会根据imageview的大小裁剪图片进行缓存
                    .animate(animationDisplayOption.animateResId)//加载动画
                    .placeholder(animationDisplayOption.placeHolderResId)//占位图
                    .error(animationDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof RoundDisplayOption) {//设置圆角
            RoundDisplayOption roundDisplayOption = (RoundDisplayOption) option;
            Glide.with(activity)
                    .load(url)
                    .thumbnail(roundDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸
                    .transform(new RoundTransform(activity, (int) roundDisplayOption.radius))//圆角
                    .placeholder(roundDisplayOption.placeHolderResId)//占位图
                    .error(roundDisplayOption.errorResId)//错误图
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof LoadAfterDisplayOption) {//处理加载的bitmap
            final LoadAfterDisplayOption afterDisplayOption = (LoadAfterDisplayOption) option;

            SimpleTarget target = new SimpleTarget<Bitmap>(width, height) {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                    if (null != afterDisplayOption.listener) {
                        Bitmap bitmap = afterDisplayOption.listener.onLoadAfter(resource);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };//不要写成匿名内部类的机制，原因就是java的自动垃圾回收机制可能在图片还没有加载好的时候就已经把你的Target回收了。


            Glide.with(activity)
                    .load(url)
                    .asBitmap()//强制Glide返回一个Bitmap
                    .centerCrop()
                    .thumbnail(afterDisplayOption.thumbnailScale)//略缩图
                    .placeholder(afterDisplayOption.placeHolderResId)//占位图
                    .error(afterDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(target);

        } else if (option instanceof ThumbnailDisplayOption) {//高级的略缩图，通过网络获取略缩图
            ThumbnailDisplayOption thumbnailDisplayOption = (ThumbnailDisplayOption) option;
            DrawableRequestBuilder<String> thumbnailRequest = Glide.with(activity).load(thumbnailDisplayOption
                    .thumbnailUrl);
            Glide.with(activity)
                    .load(url)
                    .centerCrop()
                    .thumbnail(thumbnailRequest)
                    .override(width, height)
                    .placeholder(thumbnailDisplayOption.placeHolderResId)
                    .error(thumbnailDisplayOption.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);

        } else {
            Glide.with(activity)
                    .load(url)
                    .centerCrop()
                    .thumbnail(option.thumbnailScale)
                    .override(width, height)
                    .placeholder(option.placeHolderResId)
                    .error(option.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }

    }

    @Override
    public void display(Fragment fragment, final ImageView imageView, String url, int width, int height, DisplayOption
            option) {
        if (option instanceof AnimationDisplayOption) {//加载动画
            AnimationDisplayOption animationDisplayOption = (AnimationDisplayOption) option;
            Glide.with(fragment)
                    .load(url)
                    .centerCrop()
                    .thumbnail(animationDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸，不设置的话glide会根据imageview的大小裁剪图片进行缓存
                    .animate(animationDisplayOption.animateResId)//加载动画
                    .placeholder(animationDisplayOption.placeHolderResId)//占位图
                    .error(animationDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof RoundDisplayOption) {//设置圆角
            RoundDisplayOption roundDisplayOption = (RoundDisplayOption) option;
            Glide.with(fragment)
                    .load(url)
                    .thumbnail(roundDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸
                    .transform(new RoundTransform(fragment.getContext(), (int) roundDisplayOption.radius))//圆角
                    .placeholder(roundDisplayOption.placeHolderResId)//占位图
                    .error(roundDisplayOption.errorResId)//错误图
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof LoadAfterDisplayOption) {//处理加载的bitmap
            final LoadAfterDisplayOption afterDisplayOption = (LoadAfterDisplayOption) option;

            SimpleTarget target = new SimpleTarget<Bitmap>(width, height) {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                    if (null != afterDisplayOption.listener) {
                        Bitmap bitmap = afterDisplayOption.listener.onLoadAfter(resource);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };//不要写成匿名内部类的机制，原因就是java的自动垃圾回收机制可能在图片还没有加载好的时候就已经把你的Target回收了。


            Glide.with(fragment)
                    .load(url)
                    .asBitmap()//强制Glide返回一个Bitmap
                    .centerCrop()
                    .thumbnail(afterDisplayOption.thumbnailScale)//略缩图
                    .placeholder(afterDisplayOption.placeHolderResId)//占位图
                    .error(afterDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(target);

        } else if (option instanceof ThumbnailDisplayOption) {//高级的略缩图，通过网络获取略缩图
            ThumbnailDisplayOption thumbnailDisplayOption = (ThumbnailDisplayOption) option;
            DrawableRequestBuilder<String> thumbnailRequest = Glide.with(fragment).load(thumbnailDisplayOption
                    .thumbnailUrl);
            Glide.with(fragment)
                    .load(url)
                    .centerCrop()
                    .thumbnail(thumbnailRequest)
                    .override(width, height)
                    .placeholder(thumbnailDisplayOption.placeHolderResId)
                    .error(thumbnailDisplayOption.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);

        } else {
            Glide.with(fragment)
                    .load(url)
                    .centerCrop()
                    .thumbnail(option.thumbnailScale)
                    .override(width, height)
                    .placeholder(option.placeHolderResId)
                    .error(option.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }
    }

    @Override
    public void displayAnimation(Context context, ImageView imageView, String url, int animaResId) {
        AnimationDisplayOption option = new AnimationDisplayOption(defaultOption.placeHolderResId, defaultOption
                .errorResId, animaResId);
        display(context, imageView, url, option);
    }

    @Override
    public void displayCorner(Context context, ImageView imageView, String url, int corner) {
        Glide.with(context).load(url).placeholder(defaultOption.placeHolderResId).transform(new
                RoundTransform(imageView.getContext(), corner)).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL)
                .into
                (imageView);
    }

    @Override
    public void displayCircle(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url).placeholder(defaultOption.placeHolderResId).transform(CircleBitmapTransfer
                .SINGLETON).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into
                (imageView);
    }

    /**
     * glide默认会根据imageview的大小裁剪图片进行缓存
     * <p>这里指定宽度和高度</p>
     *
     * @param context
     * @param imageView
     * @param url
     * @param width
     * @param height
     */
    @Override
    public void display(Context context, ImageView imageView, String url, int width, int height) {
        display(context, imageView, url, width, height, defaultOption);
    }

    @Override
    public void display(Context context, final ImageView imageView, String url, int width, int height, DisplayOption
            option) {
        if (option instanceof AnimationDisplayOption) {//加载动画
            AnimationDisplayOption animationDisplayOption = (AnimationDisplayOption) option;
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(animationDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸，不设置的话glide会根据imageview的大小裁剪图片进行缓存
                    .animate(animationDisplayOption.animateResId)//加载动画
                    .placeholder(animationDisplayOption.placeHolderResId)//占位图
                    .error(animationDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof RoundDisplayOption) {//设置圆角
            RoundDisplayOption roundDisplayOption = (RoundDisplayOption) option;
            Glide.with(context)
                    .load(url)
                    .thumbnail(roundDisplayOption.thumbnailScale)//略缩图
                    .override(width, height)//设置加载尺寸
                    .transform(new RoundTransform(context, (int) roundDisplayOption.radius))//圆角
                    .placeholder(roundDisplayOption.placeHolderResId)//占位图
                    .error(roundDisplayOption.errorResId)//错误图
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof LoadAfterDisplayOption) {//处理加载的bitmap
            final LoadAfterDisplayOption afterDisplayOption = (LoadAfterDisplayOption) option;

            SimpleTarget target = new SimpleTarget<Bitmap>(width, height) {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                    if (null != afterDisplayOption.listener) {
                        Bitmap bitmap = afterDisplayOption.listener.onLoadAfter(resource);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };//不要写成匿名内部类的机制，原因就是java的自动垃圾回收机制可能在图片还没有加载好的时候就已经把你的Target回收了。


            Glide.with(context)
                    .load(url)
                    .asBitmap()//强制Glide返回一个Bitmap
                    .centerCrop()
                    .thumbnail(afterDisplayOption.thumbnailScale)//略缩图
                    .placeholder(afterDisplayOption.placeHolderResId)//占位图
                    .error(afterDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(target);

        } else if (option instanceof ThumbnailDisplayOption) {//高级的略缩图，通过网络获取略缩图
            ThumbnailDisplayOption thumbnailDisplayOption = (ThumbnailDisplayOption) option;
            DrawableRequestBuilder<String> thumbnailRequest = Glide.with(context).load(thumbnailDisplayOption
                    .thumbnailUrl);
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(thumbnailRequest)
                    .override(width, height)
                    .placeholder(thumbnailDisplayOption.placeHolderResId)
                    .error(thumbnailDisplayOption.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);

        } else {
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(option.thumbnailScale)
                    .override(width, height)
                    .placeholder(option.placeHolderResId)
                    .error(option.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }
    }

    @Override
    public void display(Context context, ImageView imageView, String url, DisplayOption option) {
        if (option instanceof AnimationDisplayOption) {//加载动画
            AnimationDisplayOption animationDisplayOption = (AnimationDisplayOption) option;
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(animationDisplayOption.thumbnailScale)//略缩图
//                    .override(width, height)//设置加载尺寸，不设置的话glide会根据imageview的大小裁剪图片进行缓存
                    .animate(animationDisplayOption.animateResId)//加载动画
                    .placeholder(animationDisplayOption.placeHolderResId)//占位图
                    .error(animationDisplayOption.errorResId)//错误图
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof RoundDisplayOption) {//设置圆角
            RoundDisplayOption roundDisplayOption = (RoundDisplayOption) option;
            Glide.with(context)
                    .load(url)
                    .thumbnail(roundDisplayOption.thumbnailScale)//略缩图
//                    .override(width, height)//设置加载尺寸
                    .transform(new RoundTransform(context, (int) roundDisplayOption.radius))//圆角
                    .placeholder(roundDisplayOption.placeHolderResId)//占位图
                    .error(roundDisplayOption.errorResId)//错误图
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else if (option instanceof ThumbnailDisplayOption) {//高级的略缩图，通过网络获取略缩图
            ThumbnailDisplayOption thumbnailDisplayOption = (ThumbnailDisplayOption) option;
            DrawableRequestBuilder<String> thumbnailRequest = Glide.with(context).load(thumbnailDisplayOption
                    .thumbnailUrl);
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(thumbnailRequest)
//                    .override(width, height)
                    .placeholder(thumbnailDisplayOption.placeHolderResId)
                    .error(thumbnailDisplayOption.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);

        } else {
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .thumbnail(option.thumbnailScale)
//                    .override(width, height)
                    .placeholder(option.placeHolderResId)
                    .error(option.errorResId)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }
    }

    @Override
    public void displayGif(Context context, ImageView imageView, String url, int width, int height, DisplayOption
            option) {
        Glide.with(context)
                .load(url)
                .asGif()
                .override(width, height)
                .placeholder(option.placeHolderResId)
                .error(option.errorResId)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)//如果为ALL和Result就不行
                .into(imageView);
    }

    @Override
    public void clearDiskCache(final Context context) {
        new Thread() {
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();//清理磁盘缓存 需要在子线程中执行
            }
        }.start();
    }

    @Override
    public void clearMemory(Context context) {
        Glide.get(context).clearMemory();//清理内存缓存  可以在UI主线程中进行
    }
}
