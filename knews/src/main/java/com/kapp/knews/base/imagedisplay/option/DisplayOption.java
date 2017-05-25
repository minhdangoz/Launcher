package com.kapp.knews.base.imagedisplay.option;

/**
 * description：图片显示option
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:36
 */
public class DisplayOption {

    /**
     * 默认略缩图相对于原图的比例为一半
     */
    public static final float THUMBNAIL_SCALE_DEFAULT = 0.5f;
    /**
     * 略缩图相对于原图的比例
     */
    public float thumbnailScale = THUMBNAIL_SCALE_DEFAULT;

    public int placeHolderResId;
    public int errorResId;

    public DisplayOption(int placeHolderResId, int errorResId) {
        this.placeHolderResId = placeHolderResId;
        this.errorResId = errorResId;
    }

    public DisplayOption(DisplayOption option) {
        this.placeHolderResId = option.placeHolderResId;
        this.errorResId = option.errorResId;
    }
}
