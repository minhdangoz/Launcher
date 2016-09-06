/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.android.launcher3.settings.SettingsProvider;
import com.klauncher.ext.LauncherLog;
import com.klauncher.launcher.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DynamicGrid {
    @SuppressWarnings("unused")
    private static final String TAG = "DynamicGrid";

    private DeviceProfile mProfile;
    private float mMinWidth;
    private float mMinHeight;

    // This is a static that we use for the default icon size on a 4/5-inch phone
    static float DEFAULT_ICON_SIZE_DP = 66;
    static float DEFAULT_ICON_SIZE_PX = 0;

    public static float dpiFromPx(int size, DisplayMetrics metrics){
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }
    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }
    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public DynamicGrid(Context context, Resources resources,
                       int minWidthPx, int minHeightPx,
                       int widthPx, int heightPx,
                       int awPx, int ahPx) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        ArrayList<DeviceProfile> deviceProfiles =
                new ArrayList<DeviceProfile>();
        boolean hasAA = !LauncherAppState.isDisableAllApps();
        boolean useLargeIcons = SettingsProvider.getBoolean(context,
                SettingsProvider.SETTINGS_UI_GENERAL_ICONS_LARGE,
                R.bool.preferences_interface_general_icons_large_default);
        boolean launcherShortcutEnabled = LauncherApplication.LAUNCHER_SHORTCUT_ENABLED;
        int fourByFourDefaultLayout = launcherShortcutEnabled ? R.xml.ct_default_workspace_4x4
                               : R.xml.default_workspace_4x4;
        DEFAULT_ICON_SIZE_PX = pxFromDp(DEFAULT_ICON_SIZE_DP, dm);
        // Our phone profiles include the bar sizes in each orientation
        mMinWidth = dpiFromPx(minWidthPx, dm);
        mMinHeight = dpiFromPx(minHeightPx, dm);
        addCustomGrid(context, mMinWidth, mMinHeight, deviceProfiles);

        /*if (Build.MODEL.equals("YUEQEE-W009")) {
            deviceProfiles.add(new DeviceProfile("YUEQEE-W009",
                    293, 542, 4, 4, (useLargeIcons ? 58 : 52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 58 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("vivo X5Pro D")) {
            deviceProfiles.add(new DeviceProfile("vivo-X5Pro-D",
                    337, 617,  5, 4,  (useLargeIcons ? 60 : 60), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 60), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("SM-A8000")) {
            deviceProfiles.add(new DeviceProfile("SM-A8000",
                    335, 615,  5, 5,  (useLargeIcons ? 60 : 52), 11, (hasAA ? 6 : 6),
                    (useLargeIcons ? 60 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("M.PARTY LT1")) {
            deviceProfiles.add(new DeviceProfile("M.PARTY-LT1",
                    295, 544,  5, 4,  (useLargeIcons ? 60 : 52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("FNNI P8")) {
            deviceProfiles.add(new DeviceProfile("FNNI P8",
                    295, 544,  5, 4,  (useLargeIcons ? 60 : 52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("G20")) {
            deviceProfiles.add(new DeviceProfile("G20",
                    295, 544,  5, 4,  (useLargeIcons ? 60 : 52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("HUAWEI Y635-TL00")) {
            deviceProfiles.add(new DeviceProfile("HUAWEI Y635-TL00",
                    294, 544,  5, 4,  (useLargeIcons ? 60 : 56), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 56), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("2014821")) {
            deviceProfiles.add(new DeviceProfile("Xiaomi2014821 5X4",
                    335, 615, 5, 4, (useLargeIcons ? 60 : 60), 12, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 60), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("2014813")) {
            deviceProfiles.add(new DeviceProfile("HM2014813 5X4",
                    335, 615, 5, 4, (useLargeIcons ? 60 : 60), 12, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 60), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("SCL-AL00")) {
            deviceProfiles.add(new DeviceProfile("SCL-AL00 5X4",
                    335, 567, 5, 4, (useLargeIcons ? 60 : 60), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 60), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("BM002-G5")) {
            deviceProfiles.add(new DeviceProfile("BM002-G5 5X4",
                    335, 615, 5, 4, (useLargeIcons ? 58 : 56), 13, (hasAA ? 5 : 5),
                    (useLargeIcons ? 58 : 56), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("Redmi Note 2")) {
            deviceProfiles.add(new DeviceProfile("Xiaomi 5X4",
                    340, 620, 5, 4, (useLargeIcons ? 60 : 56), 13, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 56), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("G666")) {
            deviceProfiles.add(new DeviceProfile("G666 4X4",
                    295, 544, 4, 4, (useLargeIcons ? 58 : 48), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 58 : 48), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
            deviceProfiles.add(new DeviceProfile("G666 4X4",
                    335, 615, 4, 4, (useLargeIcons ? 58 : 58), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 58 : 58), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("Lenovo A3800-d")) {
            deviceProfiles.add(new DeviceProfile("Lenovo 4X4",
                    294, 554, 4, 4, (useLargeIcons ? 58 : 52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 58 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }
        if (Build.MODEL.equals("HUAWEI RIO-AL00")) {
            deviceProfiles.add(new DeviceProfile("HuaWei 5X4",
                    335, 567, 5, 4, (useLargeIcons ? 60 :52), 11, (hasAA ? 5 : 5),
                    (useLargeIcons ? 60 : 52), fourByFourDefaultLayout,
                    R.xml.default_workspace_4x4_no_all_apps));
        }*/
        deviceProfiles.add(new DeviceProfile("Super Short Stubby",
                255, 300,  2, 3,  (useLargeIcons ? 58 : 46), 13, (hasAA ? 3 : 5),
                (useLargeIcons ? 58 : 46), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Shorter Stubby",
                255, 400,  3, 3,  (useLargeIcons ? 58 : 46), 13, (hasAA ? 3 : 5),
                (useLargeIcons ? 58 : 46), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Short Stubby",
                275, 420,  3, 4,  (useLargeIcons ? 58 : 46), 13, (hasAA ? 5 : 5),
                (useLargeIcons ? 58 : 46), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Stubby",
                255, 450,  3, 4,  (useLargeIcons ? 58 : 46), 13, (hasAA ? 5 : 5),
                (useLargeIcons ? 58 : 46), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus S",
                296, 491.33f,  4, 4,  (useLargeIcons ? 58 : 46), 13, (hasAA ? 5 : 5),
                (useLargeIcons ? 58 : 46), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus 4",
                335, 567,  4, 4,  (useLargeIcons ? DEFAULT_ICON_SIZE_DP : 56), 13, (hasAA ? 5 : 5),
                (useLargeIcons ? 60 : 48), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus 5",
                359, 567,  4, 4,  (useLargeIcons ? DEFAULT_ICON_SIZE_DP : 56), 13, (hasAA ? 5 : 5),
                (useLargeIcons ? 60 : 48), fourByFourDefaultLayout,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Large Phone",
                406, 694,  5, 5,  (useLargeIcons ? 68 : 56), 14.4f,  5, (useLargeIcons ? 60 : 48),
                R.xml.default_workspace_5x5, R.xml.default_workspace_5x5_no_all_apps));
        // The tablet profile is odd in that the landscape orientation
        // also includes the nav bar on the side
        deviceProfiles.add(new DeviceProfile("Nexus 7",
                575, 904,  5, 6, (useLargeIcons ? 76 : 60), 14.4f,  7, (useLargeIcons ? 64 : 52),
                R.xml.default_workspace_5x6, R.xml.default_workspace_5x6_no_all_apps));
        // Larger tablet profiles always have system bars on the top & bottom
        
        /* Lenovo-SW zhaoxin5 20150210 Modified for Lenovo blade2_10 START */
        /** Lenovo-SW zhaoxin5 20150908 KOLEOSROW-2044 START */
        int androidModeLayout = R.xml.default_workspace_lenovo;
        if (Utilities.getCountryCodeForPhone().equalsIgnoreCase("ru")) {
        	// 俄罗斯
        	LauncherLog.i(TAG, "use russia android mode layout");
        	androidModeLayout = R.xml.default_workspace_lenovo_for_ru;
        }
        if (Utilities.getCountryCodeForPhone().equalsIgnoreCase("br")) {
        	// BR
        	LauncherLog.i(TAG, "use russia android mode layout");
        	androidModeLayout = R.xml.default_workspace_lenovo_for_br;
        }
        deviceProfiles.add(new DeviceProfile("Lenovo AIO",
                335, 615,  5, 4,  54, 12f,  5, 54,
                androidModeLayout, R.xml.default_workspace_lenovo_no_all_apps));
        /** Lenovo-SW zhaoxin5 20150908 KOLEOSROW-2044 END */
        deviceProfiles.add(new DeviceProfile("Lenovo blade2_8",
                527, 887,  4, 6,  64, 13f,  7, 56,
                fourByFourDefaultLayout, R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Lenovo blade2_10",
                727, 1207,  4, 6,  72, 18f,  7, 64,
                fourByFourDefaultLayout, R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Lenovo blade2_13",
                647, 1207,  4, 6,  60, 14f,  7, 54,
                fourByFourDefaultLayout, R.xml.default_workspace_4x4_no_all_apps));
        /* Lenovo-SW zhaoxin5 20150210 Modified for Lenovo blade2_10 START */
        
        /*deviceProfiles.add(new DeviceProfile("Nexus 10",
                727, 1207,  5, 6,  (useLargeIcons ? 80 : 64), 14.4f,  7, (useLargeIcons ? 68 : 56),
                R.xml.default_workspace_5x6, R.xml.default_workspace_5x6_no_all_apps));*/
        deviceProfiles.add(new DeviceProfile("20-inch Tablet",
                1527, 2527,  7, 7,  (useLargeIcons ? 104 : 80), 20,  7, (useLargeIcons ? 76 : 64),
                fourByFourDefaultLayout, R.xml.default_workspace_4x4_no_all_apps));

        mProfile = new DeviceProfile(context, deviceProfiles,
                mMinWidth, mMinHeight,
                widthPx, heightPx,
                awPx, ahPx,
                resources);
        Log.e("wcrow", "mMinWidth = " + mMinWidth);
        Log.e("wcrow", "mMinHeight = " + mMinHeight);
    }

    private void addCustomGrid(Context context, float minWidth, float minHeight, ArrayList<DeviceProfile> deviceProfiles) {
        AssetManager assetManager = context.getAssets();
        String strResponse = "";
        try {

            InputStream ims = assetManager.open("workgrid.wks");
            strResponse = getStringFromInputStream(ims);
            Log.e("wcrow", "strResponse = " + strResponse + " MODEL : " + Build.MODEL);
            JSONObject json = new JSONObject(strResponse);
            try {
                JSONObject jsonModel = json.optJSONObject(Build.MODEL);
                if (json != null) {
                    Log.d("wcrow", Build.MODEL + " json : " + jsonModel.toString());
                    deviceProfiles.add(new DeviceProfile("customGrid", minWidth, minHeight,
                            jsonModel.getInt("rows"),
                            jsonModel.getInt("columns"),
                            jsonModel.getInt("iconsize"),
                            jsonModel.getInt("textsize"),
                            jsonModel.getInt("hotseat"),
                            jsonModel.getInt("iconsize"),
                            R.xml.default_workspace_4x4,
                            R.xml.default_workspace_4x4_no_all_apps));
                    return;
                }
            } catch (Exception e) {
                Log.e("wcrow", "get MODEL data failed : " + Build.MODEL);
            }

            if (mMinHeight < 560) {
                json = json.optJSONObject("4x4");
            } else if (minHeight < 650) {
                json = json.optJSONObject("5x4");
            } else {
                if (mMinWidth > 500) {
                    json = json.optJSONObject("6x5");
                } else {
                    json = json.optJSONObject("6x4");
                }
            }
            deviceProfiles.add(new DeviceProfile("customGrid", minWidth, minHeight,
                    json.getInt("rows"),
                    json.getInt("columns"),
                    json.getInt("iconsize"),
                    json.getInt("textsize"),
                    json.getInt("hotseat"),
                    json.getInt("iconsize"),
                    R.xml.default_workspace_4x4,
                    R.xml.default_workspace_4x4_no_all_apps));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("wcrow", "addCustomGrid Exception  ");
        }
    }

    private static String getStringFromInputStream(InputStream a_is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(a_is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    public DeviceProfile getDeviceProfile() {
        return mProfile;
    }

    public float getNumRows() {
        return mProfile.numRows;
    }

    public float getNumColumns() {
        return mProfile.numColumns;
    }

    public String toString() {
        return "-------- DYNAMIC GRID ------- \n" +
                "Wd: " + mProfile.minWidthDps + ", Hd: " + mProfile.minHeightDps +
                ", W: " + mProfile.widthPx + ", H: " + mProfile.heightPx +
                " [r: " + mProfile.numRows + ", c: " + mProfile.numColumns +
                ", is: " + mProfile.iconSizePx + ", its: " + mProfile.iconTextSizePx +
                ", cw: " + mProfile.cellWidthPx + ", ch: " + mProfile.cellHeightPx +
                ", hc: " + mProfile.numHotseatIcons + ", his: " + mProfile.hotseatIconSizePx + "]";
    }
}
