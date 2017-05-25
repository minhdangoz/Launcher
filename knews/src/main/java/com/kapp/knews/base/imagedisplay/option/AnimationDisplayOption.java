package com.kapp.knews.base.imagedisplay.option;

/**
 * description：加了动画的图片显示option
 * <br>author：caowugao
 * <br>time： 2017/03/30 15:36
 */
public class AnimationDisplayOption extends DisplayOption {
    public int animateResId;

    public AnimationDisplayOption(int placeHolderResId, int errorResId,int animateResId) {
        super(placeHolderResId, errorResId);
        this.animateResId=animateResId;
    }
}
