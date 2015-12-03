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

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.android.launcher3.settings.SettingsProvider;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

class DeviceProfileQuery {
    DeviceProfile profile;
    float widthDps;
    float heightDps;
    float value;
    PointF dimens;

    DeviceProfileQuery(DeviceProfile p, float v) {
        widthDps = p.minWidthDps;
        heightDps = p.minHeightDps;
        value = v;
        dimens = new PointF(widthDps, heightDps);
        profile = p;
    }
}

@SuppressWarnings("unused")
public class DeviceProfile {
    public static interface DeviceProfileCallbacks {
        public void onAvailableSizeChanged(DeviceProfile grid);
    }

    public final static int GRID_SIZE_MAX = 3;
    public final static int GRID_SIZE_MIN = 2;

    public enum GridSize {
        Comfortable(0),
        Cozy(1),
        Condensed(2),
        Custom(3);

        private final int mValue;
        private GridSize(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static GridSize getModeForValue(int value) {
            switch (value) {
                case 1:
                    return Cozy;
                case 2:
                    return Condensed;
                case 3:
                    return Custom;
                default :
                    return Comfortable;
            }
        }
    }

    String name;
    float minWidthDps;
    float minHeightDps;
    float numRows;
    float numColumns;
    int numRowsBase;
    int numColumnsBase;
    float numHotseatIcons;
    float iconSize;
    private float iconTextSize;
    public int iconDrawablePaddingOriginalPx;
    private float hotseatIconSize;

    int defaultLayoutId;
    int defaultNoAllAppsLayoutId;

    boolean isLandscape;
    boolean isTablet;
    boolean isLargeTablet;
    boolean isLayoutRtl;
    boolean transposeLayoutWithOrientation;

    int desiredWorkspaceLeftRightMarginPx;
    int edgeMarginPx;
    /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line begin*/
    int horizontalEdgeMarginPx;
    int verticalEdgeMarginPx;
    /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line end*/
    Rect defaultWidgetPadding;

    int widthPx;
    int heightPx;
    int availableWidthPx;
    int availableHeightPx;
    int defaultPageSpacingPx;

    int overviewModeMinIconZoneHeightPx;
    int overviewModeMaxIconZoneHeightPx;
    int overviewModeBarItemWidthPx;
    int overviewModeBarSpacerWidthPx;
    float overviewModeIconZoneRatio;
    float overviewModeScaleFactor;

    int iconSizePx;
    int iconTextSizePx;
    int iconDrawablePaddingPx;
    int cellWidthPx;
    public int cellHeightPx;
    int allAppsIconSizePx;
    int allAppsIconTextSizePx;
    int allAppsCellWidthPx;
    int allAppsCellHeightPx;
    int allAppsCellPaddingPx;
    int folderBackgroundOffset;
    int folderIconSizePx;
    int folderCellWidthPx;
    int folderCellHeightPx;
    int hotseatCellWidthPx;
    int hotseatCellHeightPx;
    int hotseatIconSizePx;
    int hotseatBarHeightPx;
    int hotseatAllAppsRank;
    int allAppsNumRows;
    int allAppsNumCols;
    int searchBarSpaceWidthPx;
    int searchBarSpaceMaxWidthPx;
    int searchBarSpaceHeightPx;
    int searchBarHeightPx;
    int pageIndicatorHeightPx;
    int allAppsButtonVisualSize;
    int dockviewBarHeightPx;
    int dockviewTopPaddingPx;

    boolean searchBarVisible;

    float dragViewScale;

    int allAppsShortEdgeCount = -1;
    int allAppsLongEdgeCount = -1;

    private ArrayList<DeviceProfileCallbacks> mCallbacks = new ArrayList<DeviceProfileCallbacks>();

    DeviceProfile(String n, float w, float h, float r, float c,
                  float is, float its, float hs, float his, int dlId, int dnalId) {
        // Ensure that we have an odd number of hotseat items (since we need to place all apps)
        if (!LauncherAppState.isDisableAllApps() && hs % 2 == 0) {
            throw new RuntimeException("All Device Profiles must have an odd number of hotseat spaces");
        }

        name = n;
        minWidthDps = w;
        minHeightDps = h;
        numRows = r;
        numColumns = c;
        iconSize = is;
        iconTextSize = its;
        numHotseatIcons = hs;
        hotseatIconSize = his;
        defaultLayoutId = dlId;
        defaultNoAllAppsLayoutId = dnalId;
    }

    DeviceProfile() {
    }

    DeviceProfile(Context context,
                  ArrayList<DeviceProfile> profiles,
                  float minWidth, float minHeight,
                  int wPx, int hPx,
                  int awPx, int ahPx,
                  Resources res) {
        DisplayMetrics dm = res.getDisplayMetrics();
        ArrayList<DeviceProfileQuery> points =
                new ArrayList<DeviceProfileQuery>();
        transposeLayoutWithOrientation =
                res.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        minWidthDps = minWidth;
        minHeightDps = minHeight;

        ComponentName cn = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, cn, null);
        /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line begin*/
        horizontalEdgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin_horizontal);
        verticalEdgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin_vertical);
        /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line end*/
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
        pageIndicatorHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_page_indicator_height) / 2;
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */
        defaultPageSpacingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_page_spacing);
        allAppsCellPaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_all_apps_cell_padding);
        overviewModeMinIconZoneHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_min_icon_zone_height);
        overviewModeMaxIconZoneHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_max_icon_zone_height);
        overviewModeBarItemWidthPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_item_width);
        overviewModeBarSpacerWidthPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_spacer_width);
        overviewModeIconZoneRatio =
                res.getInteger(R.integer.config_dynamic_grid_overview_icon_zone_percentage) / 100f;
        overviewModeScaleFactor =
                res.getInteger(R.integer.config_dynamic_grid_overview_scale_percentage) / 100f;

        // Find the closes profile given the width/height
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p, 0f));
        }
        DeviceProfile closestProfile = findClosestDeviceProfile(minWidth, minHeight, points);

        // Snap to the closest row count
        numRows = closestProfile.numRows;

        // Snap to the closest column count
        numColumns = closestProfile.numColumns;

        // Snap to the closest hotseat size
        numHotseatIcons = closestProfile.numHotseatIcons;
        hotseatAllAppsRank = (int) (numHotseatIcons / 2);

        numRowsBase = (int) numRows;
        int gridResize = SettingsProvider.getIntCustomDefault(context,
                SettingsProvider.SETTINGS_UI_DYNAMIC_GRID_SIZE, 0);
        if (GridSize.getModeForValue(gridResize) != GridSize.Custom) {
            numRows += gridResize;
        } else {
            int iTempNumberOfRows = SettingsProvider.getIntCustomDefault(context,
                    SettingsProvider.SETTINGS_UI_HOMESCREEN_ROWS, (int)numRows);
            if (iTempNumberOfRows > 0) {
                numRows = iTempNumberOfRows;
            }
        }
        numColumnsBase = (int) numColumns;
        if (GridSize.getModeForValue(gridResize) != GridSize.Custom) {
            numColumns += gridResize;
        } else {
            int iTempNumberOfColumns = SettingsProvider.getIntCustomDefault(context,
                    SettingsProvider.SETTINGS_UI_HOMESCREEN_COLUMNS, (int)numColumns);
            if (iTempNumberOfColumns > 0) {
                numColumns = iTempNumberOfColumns;
            }
        }

        // Snap to the closest default layout id
        defaultLayoutId = closestProfile.defaultLayoutId;

        // Snap to the closest default no all-apps layout id
        defaultNoAllAppsLayoutId = closestProfile.defaultNoAllAppsLayoutId;

        // Interpolate the icon size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p, p.iconSize));
        }
        iconSize = invDistWeightedInterpolate(minWidth, minHeight, points);

        // AllApps uses the original non-scaled icon size
        allAppsIconSizePx = DynamicGrid.pxFromDp(iconSize, dm);

        // Interpolate the icon text size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p, p.iconTextSize));
        }
        iconTextSize = invDistWeightedInterpolate(minWidth, minHeight, points);
        iconDrawablePaddingOriginalPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        // AllApps uses the original non-scaled icon text size
        allAppsIconTextSizePx = DynamicGrid.pxFromDp(iconTextSize, dm);

        // Interpolate the hotseat icon size
        points.clear();
        for (DeviceProfile p : profiles) {
            points.add(new DeviceProfileQuery(p, p.hotseatIconSize));
        }
        // Hotseat
        hotseatIconSize = invDistWeightedInterpolate(minWidth, minHeight, points);

        // If the partner customization apk contains any grid overrides, apply them
        applyPartnerDeviceProfileOverrides(context, dm);

        // Calculate the remaining vars
        updateFromConfiguration(context, res, wPx, hPx, awPx, ahPx);
        updateAvailableDimensions(context);
        computeAllAppsButtonSize(context);
        // Search Bar
        searchBarVisible = isSearchBarEnabled(context);
        searchBarSpaceWidthPx = Math.min(searchBarSpaceMaxWidthPx, widthPx);
        searchBarSpaceHeightPx = 2 * edgeMarginPx + (searchBarVisible ? searchBarHeightPx  : 3 * edgeMarginPx);

        // dockview
        // use values from dimen.xml, like widget_list_panel
        dockviewBarHeightPx = res.getDimensionPixelSize(R.dimen.editing_panel_height);
        // calculate correct top padding according to invisible icon label height
        float scaleFactorForItem = getOverviewModeScale();
        dockviewTopPaddingPx = (int) ((dockviewBarHeightPx - iconSizePx * scaleFactorForItem) / 2);
        Log.e("wcrow", "mProfile = " + closestProfile.name);
        Log.e("wcrow", "iconSize = " + iconSize);
        Log.e("wcrow", "hotseatIconSize = " + hotseatIconSize);
    }

    /* Lenovo-SW zhaoxin5 20150326 config for balde layout START */
    public int workspacePaddingTopLand    = Integer.MIN_VALUE;
    public int workspacePaddingBottomLand = Integer.MIN_VALUE;
    public int workspacePaddingLeftLand   = Integer.MIN_VALUE;
    public int workspacePaddingRightLand  = Integer.MIN_VALUE;
    
    public int workspacePaddingTopPort    = Integer.MIN_VALUE;
    public int workspacePaddingBottomPort = Integer.MIN_VALUE;
    public int workspacePaddingLeftPort   = Integer.MIN_VALUE;
    public int workspacePaddingRightPort  = Integer.MIN_VALUE;
    
    Rect getWorkspacePaddingCustom(int orientation) {
        Rect searchBarBounds = getSearchBarBounds(orientation);
        Rect padding = new Rect();
    	int paddingBottom = hotseatBarHeightPx + pageIndicatorHeightPx;
    	
    	boolean isLand = orientation == CellLayout.LANDSCAPE;

    	int leftPx = isLand ? workspacePaddingLeftLand : workspacePaddingLeftPort;
    	int rightPx = isLand ? workspacePaddingRightLand : workspacePaddingRightPort;
    	int topPx = isLand ? workspacePaddingTopLand : workspacePaddingTopPort;
    	int bottomPx = isLand ? workspacePaddingBottomLand : workspacePaddingBottomPort;
        
        if (orientation == CellLayout.LANDSCAPE &&
                transposeLayoutWithOrientation) {
            // Pad the left and right of the workspace with search/hotseat bar sizes
            if (isLayoutRtl) {
                padding.set(hotseatBarHeightPx, edgeMarginPx,
                        searchBarBounds.width(), edgeMarginPx);
            } else {
            	// blade 8寸横屏
                padding.set(leftPx, topPx,
                        rightPx + hotseatBarHeightPx, bottomPx);
            }
        } else {
            if (isTablet()) {
            	// blade 10寸和13寸 和寸的竖屏
            	padding.set(leftPx, topPx, rightPx, bottomPx + paddingBottom);
            } else {
            }
        }
        return padding;
    }
    /* Lenovo-SW zhaoxin5 20150326 config for balde layout END */
    
    /**
     * Apply any Partner customization grid overrides.
     *
     * Currently we support: all apps row / column count.
     */
    private void applyPartnerDeviceProfileOverrides(Context ctx, DisplayMetrics dm) {
        Partner p = Partner.get(ctx.getPackageManager());
        if (p != null) {
            DeviceProfile partnerDp = p.getDeviceProfileOverride(dm);
            if (partnerDp != null) {
                if (partnerDp.numRows > 0 && partnerDp.numColumns > 0) {
                    numRows = partnerDp.numRows;
                    numColumns = partnerDp.numColumns;
                }
                if (partnerDp.allAppsShortEdgeCount > 0 && partnerDp.allAppsLongEdgeCount > 0) {
                    allAppsShortEdgeCount = partnerDp.allAppsShortEdgeCount;
                    allAppsLongEdgeCount = partnerDp.allAppsLongEdgeCount;
                }
                if (partnerDp.iconSize > 0) {
                    iconSize = partnerDp.iconSize;
                    // AllApps uses the original non-scaled icon size
                    allAppsIconSizePx = DynamicGrid.pxFromDp(iconSize, dm);
                }

                /* Lenovo-SW zhaoxin5 20150326 config for balde layout START */
                if(partnerDp.workspacePaddingTopLand >= 0){
                	workspacePaddingTopLand = partnerDp.workspacePaddingTopLand;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingTopLand:"+workspacePaddingTopLand);
                	}
                }
                if(partnerDp.workspacePaddingTopPort >= 0){
                	workspacePaddingTopPort = partnerDp.workspacePaddingTopPort;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingTopPort:"+workspacePaddingTopPort);
                	}
                }
                
                
                if(partnerDp.workspacePaddingBottomLand >= 0){
                	workspacePaddingBottomLand = partnerDp.workspacePaddingBottomLand;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingBottomLand:"+workspacePaddingBottomLand);
                	}
                }
                if(partnerDp.workspacePaddingBottomPort >= 0){
                	workspacePaddingBottomPort = partnerDp.workspacePaddingBottomPort;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingBottomPort:"+workspacePaddingBottomPort);
                	}
                }
                
                
                if(partnerDp.workspacePaddingLeftLand >= 0){
                	workspacePaddingLeftLand = partnerDp.workspacePaddingLeftLand;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingLeftLand:"+workspacePaddingLeftLand);
                	}
                }
                if(partnerDp.workspacePaddingLeftPort >= 0){
                	workspacePaddingLeftPort = partnerDp.workspacePaddingLeftPort;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingLeftPort:"+workspacePaddingLeftPort);
                	}
                }
                
                
                if(partnerDp.workspacePaddingRightLand >= 0){
                	workspacePaddingRightLand = partnerDp.workspacePaddingRightLand;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingRightLand:"+workspacePaddingRightLand);
                	}
                }
                if(partnerDp.workspacePaddingRightPort >= 0){
                	workspacePaddingRightPort = partnerDp.workspacePaddingRightPort;
                	if(true){
                		LauncherLog.i("xixia", "workspacePaddingRightPort:"+workspacePaddingRightPort);
                	}
                }
                
                
                if(partnerDp.hotseatBarHeightPx >= 0){
                	hotseatBarHeightPx = partnerDp.hotseatBarHeightPx;
                	if(true){
                		LauncherLog.i("xixia", "hotseatBarHeightPx:"+hotseatBarHeightPx);
                	}
                }
                if(partnerDp.pageIndicatorHeightPx >= 0){
                	pageIndicatorHeightPx = partnerDp.pageIndicatorHeightPx;
                	if(true){
                		LauncherLog.i("xixia", "pageIndicatorHeightPx:"+pageIndicatorHeightPx);
                	}
                }
                if(partnerDp.iconDrawablePaddingOriginalPx >= 0){
                	iconDrawablePaddingOriginalPx = partnerDp.iconDrawablePaddingOriginalPx;
                	if(true){
                		LauncherLog.i("xixia", "iconDrawablePaddingOriginalPx:"+iconDrawablePaddingOriginalPx);
                	}
                }
                /* Lenovo-SW zhaoxin5 20150326 config for balde layout END */
            }
        }
    }

    /**
     * Determine the exact visual footprint of the all apps button, taking into account scaling
     * and internal padding of the drawable.
     */
    private void computeAllAppsButtonSize(Context context) {
        Resources res = context.getResources();
        float padding = res.getInteger(R.integer.config_allAppsButtonPaddingPercent) / 100f;
        LauncherAppState app = LauncherAppState.getInstance();
        allAppsButtonVisualSize = (int) (hotseatIconSizePx * (1 - padding));
    }

    void addCallback(DeviceProfileCallbacks cb) {
        mCallbacks.add(cb);
        cb.onAvailableSizeChanged(this);
    }
    void removeCallback(DeviceProfileCallbacks cb) {
        mCallbacks.remove(cb);
    }

    private int getDeviceOrientation(Context context) {
        WindowManager windowManager =  (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        boolean isLandscape = (config.orientation == Configuration.ORIENTATION_LANDSCAPE) &&
                (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180);
        boolean isRotatedPortrait = (config.orientation == Configuration.ORIENTATION_PORTRAIT) &&
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270);
        if (isLandscape || isRotatedPortrait) {
            return CellLayout.LANDSCAPE;
        } else {
            return CellLayout.PORTRAIT;
        }
    }

    private void updateAvailableDimensions(Context context) {
        WindowManager windowManager =  (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        // There are three possible configurations that the dynamic grid accounts for, portrait,
        // landscape with the nav bar at the bottom, and landscape with the nav bar at the side.
        // To prevent waiting for fitSystemWindows(), we make the observation that in landscape,
        // the height is the smallest height (either with the nav bar at the bottom or to the
        // side) and otherwise, the height is simply the largest possible height for a portrait
        // device.
        Point size = new Point();
        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getSize(size);
        display.getCurrentSizeRange(smallestSize, largestSize);
        availableWidthPx = size.x;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            availableHeightPx = smallestSize.y;
        } else {
            availableHeightPx = largestSize.y;
        }

        // Check to see if the icons fit in the new available height.  If not, then we need to
        // shrink the icon size.
        float scale = 1f;
        int drawablePadding = iconDrawablePaddingOriginalPx;
        updateIconSize(1f, drawablePadding, resources, dm);
        float usedHeight = (cellHeightPx * numRows);

        Rect workspacePadding = getWorkspacePadding();
        int maxHeight = (availableHeightPx - workspacePadding.top - workspacePadding.bottom);
        if (usedHeight > maxHeight) {
            scale = maxHeight / usedHeight;
            drawablePadding = 0;
        }
        updateIconSize(scale, drawablePadding, resources, dm);

        // Make the callbacks
        for (DeviceProfileCallbacks cb : mCallbacks) {
            cb.onAvailableSizeChanged(this);
        }
    }

    private void updateIconSize(float scale, int drawablePadding, Resources resources,
                                DisplayMetrics dm) {
        iconSizePx = (int) (DynamicGrid.pxFromDp(iconSize, dm) * scale);
        iconTextSizePx = (int) (DynamicGrid.pxFromSp(iconTextSize, dm) * scale);
        iconDrawablePaddingPx = drawablePadding;
        hotseatIconSizePx = (int) (DynamicGrid.pxFromDp(hotseatIconSize, dm) * scale);

        // Search Bar
        searchBarSpaceMaxWidthPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_max_width);
        searchBarHeightPx = resources.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_height);
        searchBarSpaceWidthPx = Math.min(searchBarSpaceMaxWidthPx, widthPx);
        searchBarSpaceHeightPx = searchBarHeightPx + getSearchBarTopOffset();

        // Calculate the actual text height
        Paint textPaint = new Paint();
        textPaint.setTextSize(iconTextSizePx);
        FontMetrics fm = textPaint.getFontMetrics();
        cellWidthPx = iconSizePx;
        cellHeightPx = iconSizePx + iconDrawablePaddingPx + (int) Math.ceil(fm.bottom - fm.top);
        final float scaleDps = resources.getDimensionPixelSize(R.dimen.dragViewScale);
        dragViewScale = (iconSizePx + scaleDps) / iconSizePx;

        // Hotseat
        /* Lenovo-SW zhaoxin5 20150326 config for blade layout START */
        if(workspacePaddingTopLand == Integer.MIN_VALUE){
            hotseatBarHeightPx = iconSizePx + 4 * edgeMarginPx;
            float textsize = resources.getDimension(R.dimen.infomation_count_textsize);
            /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
            hotseatBarHeightPx = (int) (hotseatBarHeightPx + textsize/2) - 5;
            /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */
        }
        /* Lenovo-SW zhaoxin5 20150326 config for blade layout END */
        
        hotseatCellWidthPx = iconSizePx;
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
        hotseatCellHeightPx = (int) (cellHeightPx * 0.8) + 2;
        /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */

        // Folder
        /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line begin*/
        folderCellWidthPx = cellWidthPx + horizontalEdgeMarginPx;
        folderCellHeightPx = cellHeightPx + verticalEdgeMarginPx;
        /*Lenovo-sw zhangyj19 add 2015/07/08 modify folder default row and line end*/
	   /*Lenovo-sw zhangyj19 add 2015/06/03 modify folder size begin */
        folderBackgroundOffset = 0;//-edgeMarginPx;
        folderIconSizePx = iconSizePx;// + 2 * -folderBackgroundOffset;
	   /*Lenovo-sw zhangyj19 add 2015/06/03 modify folder size end */
        // All Apps
        Rect padding = getWorkspacePadding(isLandscape ?
                CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
        int pageIndicatorOffset =
                resources.getDimensionPixelSize(R.dimen.apps_customize_page_indicator_offset);
        allAppsCellWidthPx = allAppsIconSizePx;
        allAppsCellHeightPx = allAppsIconSizePx + drawablePadding + iconTextSizePx;
        int maxLongEdgeCellCount =
                resources.getInteger(R.integer.config_dynamic_grid_max_long_edge_cell_count);
        int maxShortEdgeCellCount =
                resources.getInteger(R.integer.config_dynamic_grid_max_short_edge_cell_count);
        int minEdgeCellCount =
                resources.getInteger(R.integer.config_dynamic_grid_min_edge_cell_count);
        int maxRows = (isLandscape ? maxShortEdgeCellCount : maxLongEdgeCellCount);
        int maxCols = (isLandscape ? maxLongEdgeCellCount : maxShortEdgeCellCount);

        if (allAppsShortEdgeCount > 0 && allAppsLongEdgeCount > 0) {
            allAppsNumRows = isLandscape ? allAppsShortEdgeCount : allAppsLongEdgeCount;
            allAppsNumCols = isLandscape ? allAppsLongEdgeCount : allAppsShortEdgeCount;
        } else {
            allAppsNumRows = (availableHeightPx - pageIndicatorHeightPx) /
                    (allAppsCellHeightPx + allAppsCellPaddingPx);
            allAppsNumRows = Math.max(minEdgeCellCount, Math.min(maxRows, allAppsNumRows));
            allAppsNumCols = (availableWidthPx) /
                    (allAppsCellWidthPx + allAppsCellPaddingPx);
            allAppsNumCols = Math.max(minEdgeCellCount, Math.min(maxCols, allAppsNumCols));
        }
    }

    void updateFromConfiguration(Context context, Resources resources, int wPx, int hPx,
                                 int awPx, int ahPx) {
        Configuration configuration = resources.getConfiguration();
        isLandscape = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE);
        isTablet = resources.getBoolean(R.bool.is_tablet);
        isLargeTablet = resources.getBoolean(R.bool.is_large_tablet);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isLayoutRtl = (configuration.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
        } else {
            isLayoutRtl = false;
        }
        widthPx = wPx;
        heightPx = hPx;
        availableWidthPx = awPx;
        availableHeightPx = ahPx;

        float scaleFactorForItem = getOverviewModeScale();
        dockviewBarHeightPx = resources.getDimensionPixelSize(R.dimen.editing_panel_height);
        dockviewTopPaddingPx = (int) ((dockviewBarHeightPx - iconSizePx * scaleFactorForItem) / 2);

        updateAvailableDimensions(context);
    }

    private float dist(PointF p0, PointF p1) {
        return (float) Math.sqrt((p1.x - p0.x)*(p1.x-p0.x) +
                (p1.y-p0.y)*(p1.y-p0.y));
    }

    private float weight(PointF a, PointF b,
                        float pow) {
        float d = dist(a, b);
        if (d == 0f) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) (1f / Math.pow(d, pow));
    }

    /** Returns the closest device profile given the width and height and a list of profiles */
    private DeviceProfile findClosestDeviceProfile(float width, float height,
                                                   ArrayList<DeviceProfileQuery> points) {
        return findClosestDeviceProfiles(width, height, points).get(0).profile;
    }

    /** Returns the closest device profiles ordered by closeness to the specified width and height */
    private ArrayList<DeviceProfileQuery> findClosestDeviceProfiles(float width, float height,
                                                   ArrayList<DeviceProfileQuery> points) {
        final PointF xy = new PointF(width, height);

        // Sort the profiles by their closeness to the dimensions
        ArrayList<DeviceProfileQuery> pointsByNearness = points;
        Collections.sort(pointsByNearness, new Comparator<DeviceProfileQuery>() {
            public int compare(DeviceProfileQuery a, DeviceProfileQuery b) {
                return (int) (dist(xy, a.dimens) - dist(xy, b.dimens));
            }
        });

        return pointsByNearness;
    }

    private float invDistWeightedInterpolate(float width, float height,
                ArrayList<DeviceProfileQuery> points) {
        float sum = 0;
        float weights = 0;
        float pow = 5;
        float kNearestNeighbors = 3;
        final PointF xy = new PointF(width, height);

        ArrayList<DeviceProfileQuery> pointsByNearness = findClosestDeviceProfiles(width, height,
                points);

        for (int i = 0; i < pointsByNearness.size(); ++i) {
            DeviceProfileQuery p = pointsByNearness.get(i);
            if (i < kNearestNeighbors) {
                float w = weight(xy, p.dimens, pow);
                if (w == Float.POSITIVE_INFINITY) {
                    return p.value;
                }
                weights += w;
            }
        }

        for (int i = 0; i < pointsByNearness.size(); ++i) {
            DeviceProfileQuery p = pointsByNearness.get(i);
            if (i < kNearestNeighbors) {
                float w = weight(xy, p.dimens, pow);
                sum += w * p.value / weights;
            }
        }

        return sum;
    }

    /** Returns the search bar top offset */
    int getSearchBarTopOffset() {
        if (isTablet() && !isVerticalBarLayout()) {
            return searchBarVisible ? 4 * edgeMarginPx : 0;
        } else {
            return searchBarVisible ? 2 * edgeMarginPx : 0;
        }
    }

    /** Returns the search bar bounds in the current orientation */
    Rect getSearchBarBounds() {
        return getSearchBarBounds(isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
    }
    /** Returns the search bar bounds in the specified orientation */
    Rect getSearchBarBounds(int orientation) {
        Rect bounds = new Rect();
        if (orientation == CellLayout.LANDSCAPE &&
                transposeLayoutWithOrientation) {
            if (isLayoutRtl) {
                bounds.set(availableWidthPx - searchBarSpaceHeightPx, edgeMarginPx,
                        availableWidthPx, availableHeightPx - edgeMarginPx);
            } else {
                bounds.set(0, edgeMarginPx, searchBarSpaceHeightPx,
                        availableHeightPx - edgeMarginPx);
            }
        } else {
            if (isTablet()) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                int width = (orientation == CellLayout.LANDSCAPE)
                        ? Math.max(widthPx, heightPx)
                        : Math.min(widthPx, heightPx);
                // XXX: If the icon size changes across orientations, we will have to take
                //      that into account here too.
                int gap = (int) ((width - 2 * edgeMarginPx -
                        (numColumns * cellWidthPx)) / (2 * (numColumns + 1)));
                bounds.set(edgeMarginPx + gap, getSearchBarTopOffset(),
                        availableWidthPx - (edgeMarginPx + gap),
                        searchBarVisible ? searchBarSpaceHeightPx : edgeMarginPx);
            } else {
                bounds.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
                        getSearchBarTopOffset(),
                        availableWidthPx - (desiredWorkspaceLeftRightMarginPx -
                        defaultWidgetPadding.right), searchBarVisible ? searchBarSpaceHeightPx : edgeMarginPx);
            }
        }
        return bounds;
    }

    /** Returns the bounds of the workspace page indicators. */
    Rect getWorkspacePageIndicatorBounds(Rect insets) {
        Rect workspacePadding = getWorkspacePadding();
        if (isLandscape && transposeLayoutWithOrientation) {
            if (isLayoutRtl) {
                return new Rect(workspacePadding.left, workspacePadding.top,
                        workspacePadding.left + pageIndicatorHeightPx,
                        heightPx - workspacePadding.bottom - insets.bottom);
            } else {
                int pageIndicatorLeft = widthPx - workspacePadding.right;
                return new Rect(pageIndicatorLeft, workspacePadding.top,
                        pageIndicatorLeft + pageIndicatorHeightPx,
                        heightPx - workspacePadding.bottom - insets.bottom);
            }
        } else {
            int pageIndicatorTop = heightPx - insets.bottom - workspacePadding.bottom;
            return new Rect(workspacePadding.left, pageIndicatorTop,
                    widthPx - workspacePadding.right, pageIndicatorTop + pageIndicatorHeightPx);
        }
    }

    /** Returns the workspace padding in the specified orientation */
    Rect getWorkspacePadding() {
        return getWorkspacePadding(isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
    }
    Rect getWorkspacePadding(int orientation) {
    	
    	/* Lenovo-SW 20150316 zhaoxin5 for blade8,10,13 layout adjust. START */
    	if(workspacePaddingTopLand != Integer.MIN_VALUE){
    		return getWorkspacePaddingCustom(orientation);
    	}
    	/* Lenovo-SW 20150316 zhaoxin5 for blade8,10,13 layout adjust. START */
    	
    	Rect searchBarBounds = getSearchBarBounds(orientation);
        Rect padding = new Rect();
        if (orientation == CellLayout.LANDSCAPE &&
                transposeLayoutWithOrientation) {
            // Pad the left and right of the workspace with search/hotseat bar sizes
            if (isLayoutRtl) {
                padding.set(hotseatBarHeightPx, edgeMarginPx,
                        searchBarBounds.width(), edgeMarginPx);
            } else {
                padding.set(searchBarBounds.width(), edgeMarginPx,
                        hotseatBarHeightPx, edgeMarginPx);
            }
        } else {
            if (isTablet()) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                float gapScale = 1f + (dragViewScale - 1f) / 2f;
                int width = (orientation == CellLayout.LANDSCAPE)
                        ? Math.max(widthPx, heightPx)
                        : Math.min(widthPx, heightPx);
                int height = (orientation != CellLayout.LANDSCAPE)
                        ? Math.max(widthPx, heightPx)
                        : Math.min(widthPx, heightPx);
                int paddingTop = searchBarBounds.bottom;
                int paddingBottom = hotseatBarHeightPx + pageIndicatorHeightPx;
                int availableWidth = Math.max(0, width - (int) ((numColumns * cellWidthPx) +
                        (numColumns * gapScale * cellWidthPx)));
                int availableHeight = Math.max(0, height - paddingTop - paddingBottom
                        - (int) (2 * numRows * cellHeightPx));
                padding.set(availableWidth / 2, paddingTop + availableHeight / 2,
                        availableWidth / 2, paddingBottom + availableHeight / 2);
            } else {
                // Pad the top and bottom of the workspace with search/hotseat bar sizes
                /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
                padding.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
                        0,
                        desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right,
                        hotseatBarHeightPx + Math.min(pageIndicatorHeightPx, 0));
                /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */
            }
        }
        return padding;
    }

    int getWorkspacePageSpacing(int orientation) {
        if ((orientation == CellLayout.LANDSCAPE &&
                transposeLayoutWithOrientation) || isLargeTablet()) {
            // In landscape mode the page spacing is set to the default.
            return defaultPageSpacingPx;
        } else {
            // In portrait, we want the pages spaced such that there is no
            // overhang of the previous / next page into the current page viewport.
            // We assume symmetrical padding in portrait mode.
            return Math.max(defaultPageSpacingPx, 2 * getWorkspacePadding().left);
        }
    }

    Rect getOverviewModeButtonBarRect() {
        int zoneHeight = (int) (overviewModeIconZoneRatio * availableHeightPx);
        zoneHeight = Math.min(overviewModeMaxIconZoneHeightPx,
                Math.max(overviewModeMinIconZoneHeightPx, zoneHeight));
        return new Rect(0, availableHeightPx - zoneHeight, 0, availableHeightPx);
    }

    float getOverviewModeScale() {
        Rect workspacePadding = getWorkspacePadding();
        Rect overviewBar = getOverviewModeButtonBarRect();
        int pageSpace = availableHeightPx - workspacePadding.top - workspacePadding.bottom;
        return (overviewModeScaleFactor * (pageSpace - overviewBar.height())) / pageSpace;
    }

    // The rect returned will be extended to below the system ui that covers the workspace
    Rect getHotseatRect() {
        if (isVerticalBarLayout()) {
            return new Rect(availableWidthPx - hotseatBarHeightPx, 0,
                    Integer.MAX_VALUE, availableHeightPx);
        } else {
            return new Rect(0, availableHeightPx - hotseatBarHeightPx,
                    availableWidthPx, Integer.MAX_VALUE);
        }
    }

    int calculateCellWidth(int width, int countX) {
        return width / countX;
    }
    int calculateCellHeight(int height, int countY) {
        return height / countY;
    }

    boolean isPhone() {
        return !isTablet && !isLargeTablet;
    }
    boolean isTablet() {
        return isTablet;
    }
    boolean isLargeTablet() {
        return isLargeTablet;
    }

    boolean isVerticalBarLayout() {
        return isLandscape && transposeLayoutWithOrientation;
    }

    boolean shouldFadeAdjacentWorkspaceScreens() {
        return isVerticalBarLayout() || isLargeTablet();
    }

    int getVisibleChildCount(ViewGroup parent) {
        int visibleChildren = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).getVisibility() != View.GONE) {
                visibleChildren++;
            }
        }
        return visibleChildren;
    }

    int calculateOverviewModeWidth(int visibleChildCount) {
        return visibleChildCount * overviewModeBarItemWidthPx +
                (visibleChildCount-1) * overviewModeBarSpacerWidthPx;
    }

    public void layout(Launcher launcher) {
        // Update search bar for live settings
        searchBarVisible = isSearchBarEnabled(launcher);
        searchBarSpaceHeightPx = 2 * edgeMarginPx + (searchBarVisible ? searchBarHeightPx : 3 * edgeMarginPx);
        FrameLayout.LayoutParams lp;
        Resources res = launcher.getResources();
        boolean hasVerticalBarLayout = isVerticalBarLayout();

        // Layout the search bar space
        View searchBar = launcher.getSearchBar();
        LinearLayout dropTargetBar = (LinearLayout) launcher.getSearchBar().getDropTargetBar();
        lp = (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        if (hasVerticalBarLayout) {
            // If search bar is invisible add some extra padding for the drop targets
            searchBarSpaceHeightPx = searchBarVisible ? searchBarSpaceHeightPx
                    : searchBarSpaceHeightPx + 5 * edgeMarginPx;
            // Vertical search bar space
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.width = searchBarSpaceHeightPx;
            lp.height = LayoutParams.WRAP_CONTENT;
            searchBar.setPadding(
                    0, 2 * edgeMarginPx, 0,
                    2 * edgeMarginPx);

            LinearLayout targets = (LinearLayout) searchBar.findViewById(R.id.drag_target_bar);
            targets.setOrientation(LinearLayout.VERTICAL);
        } else {
            // Horizontal search bar space
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.width = searchBarSpaceWidthPx;
            lp.height = searchBarSpaceHeightPx;
            searchBar.setPadding(
                    2 * edgeMarginPx,
                    getSearchBarTopOffset(),
                    2 * edgeMarginPx, 0);
        }
        searchBar.setLayoutParams(lp);

        // Layout the drop target icons
        if (hasVerticalBarLayout) {
            dropTargetBar.setOrientation(LinearLayout.VERTICAL);
        } else {
            dropTargetBar.setOrientation(LinearLayout.HORIZONTAL);
        }

        // Layout the search bar
        View qsbBar = launcher.getQsbBar();
        qsbBar.setVisibility(searchBarVisible ? View.VISIBLE : View.GONE);
        LayoutParams vglp = qsbBar.getLayoutParams();
        vglp.width = LayoutParams.MATCH_PARENT;
        vglp.height = LayoutParams.MATCH_PARENT;
        qsbBar.setLayoutParams(vglp);

        // Layout the voice proxy
        View voiceButtonProxy = launcher.findViewById(R.id.voice_button_proxy);
        if (voiceButtonProxy != null) {
            if (hasVerticalBarLayout) {
                // TODO: MOVE THIS INTO SEARCH BAR MEASURE
            } else {
                lp = (FrameLayout.LayoutParams) voiceButtonProxy.getLayoutParams();
                lp.gravity = Gravity.TOP | Gravity.END;
                lp.width = (widthPx - searchBarSpaceWidthPx) / 2 +
                        2 * iconSizePx;
                lp.height = searchBarSpaceHeightPx;
            }
        }

        // Layout the workspace
        PagedView workspace = (PagedView) launcher.findViewById(R.id.workspace);
        lp = (FrameLayout.LayoutParams) workspace.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        int orientation = isLandscape ? CellLayout.LANDSCAPE : CellLayout.PORTRAIT;
        Rect padding = getWorkspacePadding(orientation);
        workspace.setLayoutParams(lp);
        workspace.setPadding(padding.left, padding.top, padding.right, padding.bottom);
        workspace.setPageSpacing(getWorkspacePageSpacing(orientation));

        // Layout the hotseat
        View hotseat = launcher.findViewById(R.id.hotseat);
        lp = (FrameLayout.LayoutParams) hotseat.getLayoutParams();
        if (hasVerticalBarLayout) {
            // Vertical hotseat
            lp.gravity = Gravity.END;
            lp.width = hotseatBarHeightPx;
            lp.height = LayoutParams.MATCH_PARENT;
            hotseat.findViewById(R.id.layout).setPadding(0, 2 * edgeMarginPx, 0, 2 * edgeMarginPx);
        } else if (isTablet()) {
            // Pad the hotseat with the workspace padding calculated above
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            /* Lenovo-SW zhaoxin5 20150210 Modified for Lenovo blade2_10 make the hotseat left and right same as workspace START */
            hotseat.setPadding(getWorkspacePadding().left, 0,
            		getWorkspacePadding().right,
                    /*2 * edgeMarginPx*/
            		0);
            /* Lenovo-SW zhaoxin5 20150210 Modified for Lenovo blade2_10 make the hotseat left and right same as workspace END */
        } else {
            // For phones, layout the hotseat without any bottom margin
            // to ensure that we have space for the folders
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            /* Lenovo-SW zhaoxin5 make the hotseat same width as workspace */
            hotseat.findViewById(R.id.layout).setPadding(getWorkspacePadding().left, 0,
            		getWorkspacePadding().right,
                    /*2 * edgeMarginPx*/
            		0);
            /* Lenovo-SW zhaoxin5 make the hotseat same width as workspace */
        }
        hotseat.setLayoutParams(lp);
        // Layout the page indicators
        View pageIndicator = launcher.findViewById(R.id.page_indicator);
        if (pageIndicator != null) {
            if (hasVerticalBarLayout) {
                // Hide the page indicators when we have vertical search/hotseat
                pageIndicator.setVisibility(View.GONE);
            } else {
                // Put the page indicators above the hotseat
                lp = (FrameLayout.LayoutParams) pageIndicator.getLayoutParams();
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                lp.width = LayoutParams.WRAP_CONTENT;
                /* Lenovo-SW zhaoxin5 20150326 config for balde layout START */
                /*lp.height = LayoutParams.WRAP_CONTENT;*/
                lp.height = pageIndicatorHeightPx;
                /* Lenovo-SW zhaoxin5 20150326 config for balde layout START */
                /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 START */
                lp.bottomMargin = Math.max(hotseatBarHeightPx - 10, lp.bottomMargin);
                /** Lenovo-SW zhaoxin5 20150910 XTHREEROW-1616 END */
                pageIndicator.setLayoutParams(lp);
            }
        }

        // Layout AllApps
        AppsCustomizeTabHost host = (AppsCustomizeTabHost)
                launcher.findViewById(R.id.apps_customize_pane);
        if (host != null) {
            // Center the all apps page indicator
            int pageIndicatorHeight = (int) (pageIndicatorHeightPx * Math.min(1f,
                    (allAppsIconSizePx / DynamicGrid.DEFAULT_ICON_SIZE_PX)));
            pageIndicator = host.findViewById(R.id.apps_customize_page_indicator);
            if (pageIndicator != null) {
                LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) pageIndicator.getLayoutParams();
                lllp.width = LayoutParams.WRAP_CONTENT;
                lllp.height = pageIndicatorHeight;
                pageIndicator.setLayoutParams(lllp);
            }

            AppsCustomizePagedView pagedView = (AppsCustomizePagedView)
                    host.findViewById(R.id.apps_customize_pane_content);

            FrameLayout fakePageContainer = (FrameLayout)
                    host.findViewById(R.id.fake_page_container);
            FrameLayout fakePage = (FrameLayout) host.findViewById(R.id.fake_page);

            padding = new Rect();
            if (pagedView != null) {
                pagedView.updateGridSize();
                // Constrain the dimensions of all apps so that it does not span the full width
                int paddingLR = (availableWidthPx - (allAppsCellWidthPx * allAppsNumCols)) /
                        (2 * (allAppsNumCols + 1));
                int paddingTB = (availableHeightPx - (allAppsCellHeightPx * allAppsNumRows)) /
                        (2 * (allAppsNumRows + 1));
                paddingLR = Math.min(paddingLR, (int)((paddingLR + paddingTB) * 0.75f));
                paddingTB = Math.min(paddingTB, (int)((paddingLR + paddingTB) * 0.75f));
                int maxAllAppsWidth = (allAppsNumCols * (allAppsCellWidthPx + 2 * paddingLR));
                int gridPaddingLR = (availableWidthPx - maxAllAppsWidth) / 2;
                // Only adjust the side paddings on landscape phones, or tablets
                if ((isTablet() || isLandscape) && gridPaddingLR > (allAppsCellWidthPx / 4)) {
                    padding.left = padding.right = gridPaddingLR;
                }

                // The icons are centered, so we can't just offset by the page indicator height
                // because the empty space will actually be pageIndicatorHeight + paddingTB
                padding.bottom = Math.max(0, pageIndicatorHeight - paddingTB);

                pagedView.setWidgetsPageIndicatorPadding(pageIndicatorHeight);
                Drawable bg = res.getDrawable(R.drawable.quantum_panel);
                if (bg != null) {
                	// Lenovo-SW zhaoxin5 20150714 change allApps background to full white
                    // bg.setAlpha(128);
                    fakePage.setBackground(bg);
                }

                // Horizontal padding for the whole paged view
                int pagedFixedViewPadding =
                        res.getDimensionPixelSize(R.dimen.apps_customize_horizontal_padding);

                padding.left += pagedFixedViewPadding;
                padding.right += pagedFixedViewPadding;

                pagedView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
                fakePageContainer.setPadding(padding.left, padding.top, padding.right, padding.bottom);

                // Layout the dockview
                LauncherLog.d("XDockView", "layout, dockviewTopPaddingPx: " + dockviewTopPaddingPx);
                LauncherLog.d("XDockView", "layout, dockviewBarHeightPx: " + dockviewBarHeightPx);
                XDockView dockview = (XDockView) launcher.findViewById(R.id.multi_select_panel);
                lp = (FrameLayout.LayoutParams) dockview.getLayoutParams();
                if (isTablet()) {
                    lp.gravity = Gravity.BOTTOM;
                    lp.width = LayoutParams.MATCH_PARENT;
                    lp.height = dockviewBarHeightPx;
                    dockview.findViewById(R.id.dock_layout).setPadding(2 * edgeMarginPx,
                            dockviewTopPaddingPx,
                            2 * edgeMarginPx, 0);
                } else {
                    lp.gravity = Gravity.BOTTOM;
                    lp.width = LayoutParams.MATCH_PARENT;
                    lp.height = dockviewBarHeightPx;
                    dockview.findViewById(R.id.dock_layout).setPadding(2 * edgeMarginPx,
                            dockviewTopPaddingPx,
                            2 * edgeMarginPx, 0);
                }
                dockview.setLayoutParams(lp);

            }
        }
    }

    private boolean isSearchBarEnabled(Context context) {
        boolean searchActivityExists = Utilities.searchActivityExists(context);

        boolean isSearchEnabled = SettingsProvider.getBoolean(context,
                SettingsProvider.SETTINGS_UI_HOMESCREEN_SEARCH,
                R.bool.preferences_interface_homescreen_search_default);

        if (searchActivityExists) {
            return isSearchEnabled;
        } else {
            if (isSearchEnabled) {
                // Disable search bar
                SettingsProvider.putBoolean(context,
                        SettingsProvider.SETTINGS_UI_HOMESCREEN_SEARCH, false);
            }

            return false;
        }
    }
}
