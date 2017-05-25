package com.kapp.knews.base.imagedisplay.option;

import android.graphics.Bitmap;

/**
 * description：对加载后的bitmap进行操作的option
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:37
 */
public class LoadAfterDisplayOption extends DisplayOption {

    public OnLoadAfterListener listener;

    public LoadAfterDisplayOption(int placeHolderResId, int errorResId, OnLoadAfterListener listener) {
        super(placeHolderResId, errorResId);
        this.listener=listener;
    }

    public interface OnLoadAfterListener{
        Bitmap onLoadAfter(Bitmap resource);
    }

}
