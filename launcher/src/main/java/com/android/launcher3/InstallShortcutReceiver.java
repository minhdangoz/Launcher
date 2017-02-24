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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.launcher3.compat.UserHandleCompat;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;
import com.klauncher.theme.ThemeUtils;
import com.klauncher.theme.Utilities;
import com.klauncher.theme.ziptheme.ZipThemeUtils;
import com.klauncher.utilities.LogUtil;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = "InstallShortcutReceiver";
    private static final boolean DBG = false;

    public static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_INSTALL_KLAUNCHER_SHORTCUT =
            "com.klauncher.launcher.action.INSTALL_SHORTCUT";
    public static final String DATA_INTENT_KEY = "intent.data";
    public static final String LAUNCH_INTENT_KEY = "intent.launch";
    public static final String NAME_KEY = "name";
    public static final String ICON_KEY = "icon";
    public static final String ICON_RESOURCE_NAME_KEY = "iconResource";
    public static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";
    // The set of shortcuts that are pending install
    public static final String APPS_PENDING_INSTALL = "apps_to_install";

    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 85;

    private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0;
    private static final int INSTALL_SHORTCUT_IS_DUPLICATE = -1;

    // A mime-type representing shortcut data
    public static final String SHORTCUT_MIMETYPE =
            "com.android.launcher3/shortcut";

    private static Object sLock = new Object();

    private static void addToStringSet(SharedPreferences sharedPrefs,
            SharedPreferences.Editor editor, String key, String value) {
        Set<String> strings = sharedPrefs.getStringSet(key, null);
        if (strings == null) {
            strings = new HashSet<String>(0);
        } else {
            strings = new HashSet<String>(strings);
        }
        strings.add(value);
        editor.putStringSet(key, strings);
    }

    private static void addToInstallQueue(
            SharedPreferences sharedPrefs, PendingInstallShortcutInfo info) {
        synchronized(sLock) {
            try {
                JSONStringer json = new JSONStringer()
                    .object()
                    .key(DATA_INTENT_KEY).value(info.data.toUri(0))
                    .key(LAUNCH_INTENT_KEY).value(info.launchIntent.toUri(0))
                    .key(NAME_KEY).value(info.name);
                if (info.icon != null) {
                    byte[] iconByteArray = ItemInfo.flattenBitmap(info.icon);
                    json = json.key(ICON_KEY).value(
                        Base64.encodeToString(
                            iconByteArray, 0, iconByteArray.length, Base64.DEFAULT));
                }
                if (info.iconResource != null) {
                    json = json.key(ICON_RESOURCE_NAME_KEY).value(info.iconResource.resourceName);
                    json = json.key(ICON_RESOURCE_PACKAGE_NAME_KEY)
                        .value(info.iconResource.packageName);
                }
                json = json.endObject();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                if (DBG) Log.d(TAG, "Adding to APPS_PENDING_INSTALL: " + json);
                addToStringSet(sharedPrefs, editor, APPS_PENDING_INSTALL, json.toString());
                editor.commit();
            } catch (org.json.JSONException e) {
                Log.d(TAG, "Exception when adding shortcut: " + e);
            }
        }
    }

    public static void removeFromInstallQueue(SharedPreferences sharedPrefs,
                                              ArrayList<String> packageNames) {
        if (packageNames.isEmpty()) {
            return;
        }
        synchronized(sLock) {
            Set<String> strings = sharedPrefs.getStringSet(APPS_PENDING_INSTALL, null);
            if (DBG) {
                Log.d(TAG, "APPS_PENDING_INSTALL: " + strings
                        + ", removing packages: " + packageNames);
            }
            if (strings != null) {
                Set<String> newStrings = new HashSet<String>(strings);
                Iterator<String> newStringsIter = newStrings.iterator();
                while (newStringsIter.hasNext()) {
                    String json = newStringsIter.next();
                    try {
                        JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                        Intent launchIntent = Intent.parseUri(object.getString(LAUNCH_INTENT_KEY), 0);
                        String pn = launchIntent.getPackage();
                        if (pn == null) {
                            pn = launchIntent.getComponent().getPackageName();
                        }
                        if (packageNames.contains(pn)) {
                            newStringsIter.remove();
                        }
                    } catch (org.json.JSONException e) {
                        Log.d(TAG, "Exception reading shortcut to remove: " + e);
                    } catch (java.net.URISyntaxException e) {
                        Log.d(TAG, "Exception reading shortcut to remove: " + e);
                    }
                }
                sharedPrefs.edit().putStringSet(APPS_PENDING_INSTALL,
                        new HashSet<String>(newStrings)).commit();
            }
        }
    }

    private static ArrayList<PendingInstallShortcutInfo> getAndClearInstallQueue(
            SharedPreferences sharedPrefs) {
        synchronized(sLock) {
            Set<String> strings = sharedPrefs.getStringSet(APPS_PENDING_INSTALL, null);
            if (DBG) Log.d(TAG, "Getting and clearing APPS_PENDING_INSTALL: " + strings);
            if (strings == null) {
                return new ArrayList<PendingInstallShortcutInfo>();
            }
            ArrayList<PendingInstallShortcutInfo> infos =
                new ArrayList<PendingInstallShortcutInfo>();
            for (String json : strings) {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                    Intent data = Intent.parseUri(object.getString(DATA_INTENT_KEY), 0);
                    Intent launchIntent =
                            Intent.parseUri(object.getString(LAUNCH_INTENT_KEY), 0);
                    String name = object.getString(NAME_KEY);
                    String iconBase64 = object.optString(ICON_KEY);
                    String iconResourceName = object.optString(ICON_RESOURCE_NAME_KEY);
                    String iconResourcePackageName =
                        object.optString(ICON_RESOURCE_PACKAGE_NAME_KEY);
                    if (iconBase64 != null && !iconBase64.isEmpty()) {
                        byte[] iconArray = Base64.decode(iconBase64, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(iconArray, 0, iconArray.length);
                        data.putExtra(Intent.EXTRA_SHORTCUT_ICON, b);
                    } else if (iconResourceName != null && !iconResourceName.isEmpty()) {
                        Intent.ShortcutIconResource iconResource =
                            new Intent.ShortcutIconResource();
                        iconResource.resourceName = iconResourceName;
                        iconResource.packageName = iconResourcePackageName;
                        data.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
                    }
                    data.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
                    PendingInstallShortcutInfo info =
                        new PendingInstallShortcutInfo(data, name, launchIntent);
                    infos.add(info);
                } catch (org.json.JSONException e) {
                    Log.d(TAG, "Exception reading shortcut to add: " + e);
                } catch (java.net.URISyntaxException e) {
                    Log.d(TAG, "Exception reading shortcut to add: " + e);
                }
            }
            sharedPrefs.edit().putStringSet(APPS_PENDING_INSTALL, new HashSet<String>()).commit();
            return infos;
        }
    }

    // Determines whether to defer installing shortcuts immediately until
    // processAllPendingInstalls() is called.
    private static boolean mUseInstallQueue = false;

    private static class PendingInstallShortcutInfo {
        Intent data;
        Intent launchIntent;
        String name;
        Bitmap icon;
        Intent.ShortcutIconResource iconResource;

        public PendingInstallShortcutInfo(Intent rawData, String shortcutName,
                Intent shortcutIntent) {
            data = rawData;
            name = shortcutName;
            launchIntent = shortcutIntent;
        }
    }

    public void onReceive(Context context, Intent data) {
        LogUtil.e("InstallShortcutReceiver",data.getAction());
        Log.e("hw","InstallShortcutReceiver getAction : " + data.getAction());
        if (data.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = data.getDataString().replace("package:","");
            LogUtil.e("InstallShortcutReceiver", "安装了:" +packageName);
            PingManager.getInstance().reportUserAction4App(
                    PingManager.USER_ACTION_INSTALL, packageName);
        }
        //接收卸载广播
        if (data.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = data.getDataString().replace("package:","");;
            LogUtil.e("InstallShortcutReceiver", "卸载了:"  +packageName);
//            PingManager.getInstance().reportUserAction4App(
//                    PingManager.USER_ACTION_UNINSTALL, packageName);

        }
//        ||!ACTION_INSTALL_KLAUNCHER_SHORTCUT.equals(data.getAction()
        //监听不到 ACTION_INSTALL_SHORTCUT 广播
        if (!ACTION_INSTALL_KLAUNCHER_SHORTCUT.equals(data.getAction())) {
            return;
        }

        if (DBG) Log.d(TAG, "Got INSTALL_SHORTCUT: " + data.toUri(0));

        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        LogUtil.d("InstallShortcutReceiver",data.getAction());
        if (intent == null) {
            return;
        }

        // This name is only used for comparisons and notifications, so fall back to activity name
        // if not supplied
        String name = ensureValidName(context, intent,
                data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)).toString();
        /* Lenovo-sw luyy1 change start to fix XTHREEROW-1043 2015-08-21*/
        Bitmap icon = null;
        Intent.ShortcutIconResource iconResource = null;
        try {
            icon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
            icon=correctShortcutIcon(context,icon);
            iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        }catch (ClassCastException e) {
            return;
        }
        /* Lenovo-sw luyy1 change end to fix XTHREEROW-1043 2015-08-21*/

        // Queue the item up for adding if launcher has not loaded properly yet
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        boolean launcherNotLoaded = (app.getDynamicGrid() == null);

        PendingInstallShortcutInfo info = new PendingInstallShortcutInfo(data, name, intent);
        info.icon = icon;
        info.iconResource = iconResource;

        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        addToInstallQueue(sp, info);
        if (!mUseInstallQueue && !launcherNotLoaded) {
            flushInstallQueue(context);
        }
    }
    /**
     * 修正快捷方式图标
     *
     * @param context
     * @param icon
     * @return Bitmap
     */
    private Bitmap correctShortcutIcon(Context context, Bitmap icon) {
        Resources resources = context.getResources();
        Bitmap[] themes = ThemeUtils.getThemeIconBg();
        if(null==themes[0]||null==themes[2]){
            ZipThemeUtils.setThemeIconBg(context);
        }
        Bitmap result=null;
        if(null!=themes[2]){
            result=Utilities.composeShortcutIcon(new BitmapDrawable(resources,icon),themes[0],themes[1],themes[2],context);
        }
        else{
            themes = ThemeUtils.getThemeScIconBg();
            if(null==themes[0]||null==themes[2]){
                ZipThemeUtils.setThemeScIconBg(context);
            }
            if (null != themes[2]) {
                result = Utilities.composeShortcutIcon(new BitmapDrawable(resources, icon), themes[0], themes[1], themes[2], context);
            } else {
                Bitmap themeBitmap = Utilities.createIconBitmapForZipTheme(new BitmapDrawable(
                        resources, icon), context);
                Bitmap themeBitmap1 = com.android.launcher3.Utilities.createIconBitmap(new BitmapDrawable(resources, themeBitmap), context);
                Bitmap cornerBitmap = getRoundedCornerBitmap(themeBitmap1, 20f);
                result=cornerBitmap;
            }
        }
        return result;
    }
    /**
     * @features 功    能：转化为圆角图片
     * @param bitmap
     * @param roundPx 角度
     * @return Bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    static void enableInstallQueue() {
        mUseInstallQueue = true;
    }
    static void disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        flushInstallQueue(context);
    }
    static void flushInstallQueue(Context context) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = context.getSharedPreferences(spKey, Context.MODE_PRIVATE);
        ArrayList<PendingInstallShortcutInfo> installQueue = getAndClearInstallQueue(sp);
        if (!installQueue.isEmpty()) {
            Iterator<PendingInstallShortcutInfo> iter = installQueue.iterator();
            ArrayList<ItemInfo> addShortcuts = new ArrayList<ItemInfo>();
            int result = INSTALL_SHORTCUT_SUCCESSFUL;
            String duplicateName = "";
            while (iter.hasNext()) {
                final PendingInstallShortcutInfo pendingInfo = iter.next();
                //final Intent data = pendingInfo.data;
                final Intent intent = pendingInfo.launchIntent;
                final String name = pendingInfo.name;

                if (LauncherAppState.isDisableAllApps() && !isValidShortcutLaunchIntent(intent)) {
                    if (DBG) Log.d(TAG, "Ignoring shortcut with launchIntent:" + intent);
                    continue;
                }

                /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	START */
                final boolean exists = LauncherModel.shortcutExists(context, name, intent, true);
                /** Lenovo-SW zhaoxin5 20151014 PASSIONROW-2195	END */

                //final boolean allowDuplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);

                // If the intent specifies a package, make sure the package exists
                String packageName = intent.getPackage();
                if (packageName == null) {
                    packageName = intent.getComponent() == null ? null :
                        intent.getComponent().getPackageName();
                }
                if (packageName != null && !packageName.isEmpty()) {
                    UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
                    if (!LauncherModel.isValidPackage(context, packageName, myUserHandle)) {
                        if (DBG) Log.d(TAG, "Ignoring shortcut for absent package:" + intent);
                        continue;
                    }
                }

                if (!exists) {
                    // Generate a shortcut info to add into the model
                    ShortcutInfo info = getShortcutInfo(context, pendingInfo.data,
                            pendingInfo.launchIntent);
                    addShortcuts.add(info);
//                    PingManager.getInstance().reportUserAction4App(
//                            PingManager.USER_ACTION_INSTALL, packageName);
                }

            }

            // Notify the user once if we weren't able to place any duplicates
            if (result == INSTALL_SHORTCUT_IS_DUPLICATE) {
                Toast.makeText(context, context.getString(R.string.shortcut_duplicate,
                        duplicateName), Toast.LENGTH_SHORT).show();
            }

            // Add the new apps to the model and bind them
            if (!addShortcuts.isEmpty()) {
                LauncherAppState app = LauncherAppState.getInstance();
                app.getModel().addAndBindAddedWorkspaceApps(context, addShortcuts);
            }
        }
    }

    /**
     * Returns true if the intent is a valid launch intent for a shortcut.
     * This is used to identify shortcuts which are different from the ones exposed by the
     * applications' manifest file.
     *
     * When DISABLE_ALL_APPS is true, shortcuts exposed via the app's manifest should never be
     * duplicated or removed(unless the app is un-installed).
     *
     * @param launchIntent The intent that will be launched when the shortcut is clicked.
     */
    static boolean isValidShortcutLaunchIntent(Intent launchIntent) {
        if (launchIntent != null
                && Intent.ACTION_MAIN.equals(launchIntent.getAction())
                && launchIntent.getComponent() != null
                && launchIntent.getCategories() != null
                && launchIntent.getCategories().size() == 1
                && launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && launchIntent.getExtras() == null
                && TextUtils.isEmpty(launchIntent.getDataString())) {
            return false;
        }
        return true;
    }

    private static ShortcutInfo getShortcutInfo(Context context, Intent data,
                                                Intent launchIntent) {
        if (launchIntent.getAction() == null) {
            launchIntent.setAction(Intent.ACTION_VIEW);
        } else if (launchIntent.getAction().equals(Intent.ACTION_MAIN) &&
                launchIntent.getCategories() != null &&
                launchIntent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        LauncherAppState app = LauncherAppState.getInstance();
        ShortcutInfo info = app.getModel().infoFromShortcutIntent(context, data, null);
        info.title = ensureValidName(context, launchIntent, info.title);
        return info;
    }

    /**
     * Ensures that we have a valid, non-null name.  If the provided name is null, we will return
     * the application name instead.
     */
    private static CharSequence ensureValidName(Context context, Intent intent, CharSequence name) {
        if (name == null) {
            try {
                PackageManager pm = context.getPackageManager();
                ActivityInfo info = pm.getActivityInfo(intent.getComponent(), 0);
                name = info.loadLabel(pm).toString();
            } catch (PackageManager.NameNotFoundException nnfe) {
                return "";
            }
        }
        return name;
    }
}
