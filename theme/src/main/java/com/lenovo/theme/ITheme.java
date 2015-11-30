package com.lenovo.theme;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;

import java.io.InputStream;

public interface ITheme {
    /**
     * Judge the theme contains default background bitmap or not.
     * @return if the theme contains a default background bitmap  return true, otherwise return false. 
     */
    public boolean hasBgBitmap();
    /**
     * Get the default wallpaper.
     * @return the inputstream of wallpaper.
     */
    public InputStream getDefaultWallpaper();
    /**
     * Get the icon.
     * @param info package ActivityInfo
     * @return the icon of the specified packagename.
     */
    public Bitmap getIconBitmap(ActivityInfo info);
    
    /**
     *  Do some work to apply the theme
     *  such as wallpaper change
     *  or unzip zip file 
     */
    public void handleTheme(boolean justReloadLauncher, boolean enableThemeMask);
}
