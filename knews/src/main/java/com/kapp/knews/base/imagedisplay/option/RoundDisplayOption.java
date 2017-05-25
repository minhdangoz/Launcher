package com.kapp.knews.base.imagedisplay.option;

/**
 * description：圆角option
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:37
 */
public class RoundDisplayOption extends DisplayOption {
    public float radius = 4f;

    public RoundDisplayOption(int placeHolderResId, int errorResId, float radius) {
        super(placeHolderResId, errorResId);
        this.radius = radius;
    }
}
