package com.kapp.knews.base.imagedisplay.option;

/**
 * description：略缩图option
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:37
 */
public class ThumbnailDisplayOption extends DisplayOption {
    /**
     * 略缩图url
     */
    public String thumbnailUrl;

    public ThumbnailDisplayOption(int placeHolderResId, int errorResId, String url) {
        super(placeHolderResId, errorResId);
        this.thumbnailUrl = url;
    }
}
