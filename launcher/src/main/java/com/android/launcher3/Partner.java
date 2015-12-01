/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import java.io.File;

import com.android.launcher3.ModeSwitchHelper.Mode;
import com.webeye.launcher.ext.LauncherLog;

/**
 * Utilities to discover and interact with partner customizations. There can
 * only be one set of customizations on a device, and it must be bundled with
 * the system.
 */
public class Partner {

    static final String TAG = "Launcher.Partner";

    /** Marker action used to discover partner */
    private static final String
            ACTION_PARTNER_CUSTOMIZATION = "com.android.launcher3.action.PARTNER_CUSTOMIZATION";

    public static final String RES_FOLDER = "partner_folder";
    public static final String RES_WALLPAPERS = "partner_wallpapers";
    public static final String RES_DEFAULT_LAYOUT = "partner_default_layout";

    public static final String RES_DEFAULT_WALLPAPER_HIDDEN = "default_wallpapper_hidden";
    public static final String RES_SYSTEM_WALLPAPER_DIR = "system_wallpaper_directory";

    public static final String RES_REQUIRE_FIRST_RUN_FLOW = "requires_first_run_flow";

    /** These resources are used to override the device profile  */
    public static final String RES_GRID_AA_SHORT_EDGE_COUNT = "grid_aa_short_edge_count";
    public static final String RES_GRID_AA_LONG_EDGE_COUNT = "grid_aa_long_edge_count";
    public static final String RES_GRID_NUM_ROWS = "grid_num_rows";
    public static final String RES_GRID_NUM_COLUMNS = "grid_num_columns";
    public static final String RES_GRID_ICON_SIZE_DP = "grid_icon_size_dp";
    
    /* Lenovo-SW zhaoxin5 20150326 config for blade layout START */
    public static final String RES_GRID_WORKSPACE_PADDING_TOP_LAND = "grid_workspace_padding_top_land";
    public static final String RES_GRID_WORKSPACE_PADDING_BOTTOM_LAND = "grid_workspace_padding_bottom_land";
    public static final String RES_GRID_WORKSPACE_PADDING_LEFT_LAND = "grid_workspace_padding_left_land";
    public static final String RES_GRID_WORKSPACE_PADDING_RIGHT_LAND = "grid_workspace_padding_right_land";
    
    public static final String RES_GRID_WORKSPACE_PADDING_TOP_PORT = "grid_workspace_padding_top_port";
    public static final String RES_GRID_WORKSPACE_PADDING_BOTTOM_PORT = "grid_workspace_padding_bottom_port";
    public static final String RES_GRID_WORKSPACE_PADDING_LEFT_PORT = "grid_workspace_padding_left_port";
    public static final String RES_GRID_WORKSPACE_PADDING_RIGHT_PORT = "grid_workspace_padding_right_port";
    
    public static final String RES_GRID_HOTSEAT_HEIGHT = "grid_hotseat_height";
    public static final String RES_GRID_PAGE_INDICATOR_HEIGHT = "grid_page_indicator_height";
    public static final String RES_GRID_ICON_DRAWABLE_PADDING = "grid_icon_drawable_padding";

    public static final String RES_BLADE_LAYOUT_ROW_WIFI = "partner_blade_layout_row_wifi";
    public static final String RES_BLADE_LAYOUT_ROW_DATA = "partner_blade_layout_row_data";
    public static final String RES_BLADE_LAYOUT_ROW_CALL = "partner_blade_layout_row_call";
    
    public static final String RES_BLADE_LAYOUT_ROW_JP_WIFI = "partner_blade_layout_row_jp_wifi";
    public static final String RES_BLADE_LAYOUT_ROW_JP_DATA = "partner_blade_layout_row_jp_data";
    public static final String RES_BLADE_LAYOUT_ROW_JP_CALL = "partner_blade_layout_row_jp_call";
    
    public static final String RES_BLADE_LAYOUT_PRC_WIFI = "partner_blade_layout_prc_wifi";
    public static final String RES_BLADE_LAYOUT_PRC_DATA = "partner_blade_layout_prc_data";
    public static final String RES_BLADE_LAYOUT_PRC_CALL = "partner_blade_layout_prc_call";
    
    public static final String RES_PHONE_LAYOUT_ANDROID = "partner_phone_layout_android";
    public static final String RES_PHONE_LAYOUT_VIBEUI = "partner_phone_layout_vibeui";
    
    private static String getDefaultLayoutNameForPhone() {
    	if(LauncherAppState.getInstance().getCurrentLayoutMode() == Mode.ANDROID) {
    		return RES_PHONE_LAYOUT_ANDROID;
    	} else {
    		return RES_PHONE_LAYOUT_VIBEUI;
    	}
    }
    
	public static String getDefaultLayoutName() {
		
		if(!LauncherAppState.getInstance().isTablet()) {
			return getDefaultLayoutNameForPhone();
		}
		
        if (Utilities.isRowProduct()) { /* Row product */
            if (Utilities.isWifiProduct()) {
                // Pad for Japanese WIFI version.
                if (Utilities.getCountryCode().equalsIgnoreCase("jp")) {
                    return RES_BLADE_LAYOUT_ROW_JP_WIFI;
                } else {
                    return RES_BLADE_LAYOUT_ROW_WIFI;
                }
            }

            if (Utilities.isDataProduct()) {
                // Pad for Japanese Data version.
                if (Utilities.getCountryCode().equalsIgnoreCase("jp")) {
                    return RES_BLADE_LAYOUT_ROW_JP_DATA;
                } else {
                    return RES_BLADE_LAYOUT_ROW_DATA;
                }
            }
            if (Utilities.isCallProduct()) {
                // Pad for Japanese Data version.
                if (Utilities.getCountryCode().equalsIgnoreCase("jp")) {
                    return RES_BLADE_LAYOUT_ROW_JP_CALL;
                } else {
                    return RES_BLADE_LAYOUT_ROW_CALL;
                }
            }
        } else {
            /* Prc product */
            if (Utilities.isWifiProduct())
                return RES_BLADE_LAYOUT_PRC_WIFI;
            if (Utilities.isDataProduct())
                return RES_BLADE_LAYOUT_PRC_DATA;
            if (Utilities.isCallProduct())
                return RES_BLADE_LAYOUT_PRC_CALL;
        }
		return RES_DEFAULT_LAYOUT;
	}
    /* Lenovo-SW zhaoxin5 20150326 config for blade layout START */

    private static boolean sSearched = false;
    private static Partner sPartner;

    /**
     * Find and return partner details, or {@code null} if none exists.
     */
    public static synchronized Partner get(PackageManager pm) {
        if (!sSearched) {
            Pair<String, Resources> apkInfo = Utilities.findSystemApk(ACTION_PARTNER_CUSTOMIZATION, pm);
            if (apkInfo != null) {
                sPartner = new Partner(apkInfo.first, apkInfo.second);
            }
            sSearched = true;
        }
        return sPartner;
    }

    private final String mPackageName;
    private final Resources mResources;

    private Partner(String packageName, Resources res) {
        mPackageName = packageName;
        mResources = res;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public Resources getResources() {
        return mResources;
    }

    /* Lenovo-SW zhaoxin5 20150326 config for blade layout START */
    public boolean hasDefaultLayout() {
        int defaultLayout = getResources().getIdentifier(
        		getDefaultLayoutName()/*Partner.RES_DEFAULT_LAYOUT*/,
                "xml", getPackageName());
        LauncherLog.i("xixia", "hasDefaultLayout getDefaultLayoutName() : " + getDefaultLayoutName());
        return defaultLayout != 0;
    }
    /* Lenovo-SW zhaoxin5 20150326 config for blade layout END */

    public boolean hasFolder() {
        int folder = getResources().getIdentifier(Partner.RES_FOLDER,
                "xml", getPackageName());
        return folder != 0;
    }

    public boolean hideDefaultWallpaper() {
        int resId = getResources().getIdentifier(RES_DEFAULT_WALLPAPER_HIDDEN, "bool",
                getPackageName());
        return resId != 0 && getResources().getBoolean(resId);
    }

    public File getWallpaperDirectory() {
        int resId = getResources().getIdentifier(RES_SYSTEM_WALLPAPER_DIR, "string",
                getPackageName());
        return (resId != 0) ? new File(getResources().getString(resId)) : null;
    }

    public boolean requiresFirstRunFlow() {
        int resId = getResources().getIdentifier(RES_REQUIRE_FIRST_RUN_FLOW, "bool",
                getPackageName());
        return resId != 0 && getResources().getBoolean(resId);
    }

    public DeviceProfile getDeviceProfileOverride(DisplayMetrics dm) {
        boolean containsProfileOverrides = false;

        DeviceProfile dp = new DeviceProfile();

        // We initialize customizable fields to be invalid
        dp.numRows = -1;
        dp.numColumns = -1;
        dp.allAppsShortEdgeCount = -1;
        dp.allAppsLongEdgeCount = -1;

        try {
            int resId = getResources().getIdentifier(RES_GRID_NUM_ROWS,
                    "integer", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.numRows = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_NUM_COLUMNS,
                    "integer", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.numColumns = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_AA_SHORT_EDGE_COUNT,
                    "integer", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.allAppsShortEdgeCount = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_AA_LONG_EDGE_COUNT,
                    "integer", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.allAppsLongEdgeCount = getResources().getInteger(resId);
            }

            resId = getResources().getIdentifier(RES_GRID_ICON_SIZE_DP,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                int px = getResources().getDimensionPixelSize(resId);
                dp.iconSize = DynamicGrid.dpiFromPx(px, dm);
            }
            
            /* Lenovo-SW zhaoxin5 20150326 config for blade layout START */
            /* workspace padding top*/
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_TOP_LAND,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingTopLand = getResources().getDimensionPixelSize(resId);
            }
            /* port */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_TOP_PORT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingTopPort = getResources().getDimensionPixelSize(resId);
            }
            
            
            /* workspace padding bottom*/
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_BOTTOM_LAND,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingBottomLand = getResources().getDimensionPixelSize(resId);
            }
            /* port */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_BOTTOM_PORT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingBottomPort = getResources().getDimensionPixelSize(resId);
            }
            
            
            /* workspace padding left*/
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_LEFT_LAND,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingLeftLand = getResources().getDimensionPixelSize(resId);
            }
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_LEFT_PORT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingLeftPort = getResources().getDimensionPixelSize(resId);
            }
            
            
            /* workspace padding right*/
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_RIGHT_LAND,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingRightLand = getResources().getDimensionPixelSize(resId);
            }
            /* land */
            resId = getResources().getIdentifier(RES_GRID_WORKSPACE_PADDING_RIGHT_PORT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.workspacePaddingRightPort = getResources().getDimensionPixelSize(resId);
            }
            
            
            /* hotseat height */
            resId = getResources().getIdentifier(RES_GRID_HOTSEAT_HEIGHT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.hotseatBarHeightPx = getResources().getDimensionPixelSize(resId);
            }
            
            /* page indicator height */
            resId = getResources().getIdentifier(RES_GRID_PAGE_INDICATOR_HEIGHT,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.pageIndicatorHeightPx = getResources().getDimensionPixelSize(resId);
            }
            
            /* icon drawable padding */
            resId = getResources().getIdentifier(RES_GRID_ICON_DRAWABLE_PADDING,
                    "dimen", getPackageName());
            if (resId > 0) {
                containsProfileOverrides = true;
                dp.iconDrawablePaddingOriginalPx = getResources().getDimensionPixelSize(resId);
            }            
            /* Lenovo-SW zhaoxin5 20150326 config for blade layout END */
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "Invalid Partner grid resource!", ex);
        }
        return containsProfileOverrides ? dp : null;
    }
}
