/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


public class WallpaperCropActivity {
    private static final String TAG = "Launcher.WallpaperCropActivity";
    protected static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
    protected static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";
    private static final float WALLPAPER_SCREENS_SPAN = 2f;

    public static String getSharedPreferencesKey() {
        return WallpaperCropActivity.class.getName();
    }

    // As a ratio of screen height, the total distance we want the parallax effect to span
    // horizontally
    private static float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16/10f;
        final float ASPECT_RATIO_PORTRAIT = 10/16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
            (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
            (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    static protected Point getDefaultWallpaperSize(Resources res, WindowManager windowManager) {
        Point minDims = new Point();
        Point maxDims = new Point();
        
        //Modify by zhangdy3 for support sdk 15 start
        if (android.os.Build.VERSION.SDK_INT 
        		>= android.os.Build.VERSION_CODES.JELLY_BEAN )
        	windowManager.getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);
        //Modify by zhangdy3 for support sdk 15 end

        int maxDim = Math.max(maxDims.x, maxDims.y);
        int minDim = Math.max(minDims.x, minDims.y);

		//TODO this code need framework.jar 4.4 version
        //Modify by zhangdy3 for support sdk 15 start
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN + 1) {
            //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point realSize = new Point();
            windowManager.getDefaultDisplay().getRealSize(realSize);
            maxDim = Math.max(realSize.x, realSize.y);
            minDim = Math.min(realSize.x, realSize.y);
        } else {
        	Display display = windowManager.getDefaultDisplay();
        	DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            
        	 maxDim = Math.max(dm.widthPixels, dm.heightPixels);
             minDim = Math.min(dm.widthPixels, dm.heightPixels);
        }
        //Modify by zhangdy3 for support sdk 15 end
        
        // We need to ensure that there is enough extra space in the wallpaper
        // for the intended
        // parallax effects
        final int defaultWidth, defaultHeight;
        if (isScreenLarge(res)) {
            defaultWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
            defaultHeight = maxDim;
        } else {
            defaultWidth = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
            defaultHeight = maxDim;
        }
        return new Point(defaultWidth, defaultHeight);
    }

    private static boolean isScreenLarge(Resources res) {
        Configuration config = res.getConfiguration();
        return config.smallestScreenWidthDp >= 720;
    }

    static public void suggestWallpaperDimension(Resources res,
            final SharedPreferences sharedPrefs,
            WindowManager windowManager,
            final WallpaperManager wallpaperManager) {
        final Point defaultWallpaperSize = getDefaultWallpaperSize(res, windowManager);
        mWallpaperWidth = sharedPrefs.getInt(WALLPAPER_WIDTH_KEY, defaultWallpaperSize.x);
        mWallpaperHeight = sharedPrefs.getInt(WALLPAPER_HEIGHT_KEY, defaultWallpaperSize.y);

        new Thread("suggestWallpaperDimension") {
            public void run() {
                // If we have saved a wallpaper width/height, use that instead               
                wallpaperManager.suggestDesiredDimensions(mWallpaperWidth, mWallpaperHeight);
                Log.d(TAG, "suggestDesiredDimensions mWallpaperWidth " 
                        + mWallpaperWidth + " mWallpaperHeight = " + mWallpaperHeight);
            }
        }.start();
    }
    public static void resetSuggestWallpaperDimension(final WallpaperManager wallpaperManager) {
        if (mWallpaperWidth <= 0 || mWallpaperHeight <= 0) {
            return;
        }
        wallpaperManager.suggestDesiredDimensions(mWallpaperWidth, mWallpaperHeight);
    }
    public static int mWallpaperWidth;
    public static int mWallpaperHeight;
}
