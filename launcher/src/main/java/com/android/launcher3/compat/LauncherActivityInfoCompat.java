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

package com.android.launcher3.compat;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public abstract class LauncherActivityInfoCompat {

    LauncherActivityInfoCompat() {
    }

    public abstract ComponentName getComponentName();
    public abstract UserHandleCompat getUser();
    public abstract CharSequence getLabel();
    public abstract Drawable getIcon(int density);
    public abstract ApplicationInfo getApplicationInfo();
    public abstract long getFirstInstallTime();
    public abstract Drawable getBadgedIcon(int density);
    /* Lenovo-SW zhaoxin5 20150410 add new interface to get ActivityInfo to avoid all
     * activities in one application to get same icon in iconcache START*/
    //error 待查
    // Unable to resume activity {com.webeye.launcher/com.webeye.launcher.ext.WeLauncher}: java.lang.NullPointerException: Attempt to invoke virtual method 'android.content.pm.ActivityInfo com.android.launcher3.b.d.f()' on a null object reference
    public abstract ActivityInfo getActivityInfo();
    /* Lenovo-SW zhaoxin5 20150410 add new interface to get ActivityInfo to avoid all
     * activities in one application to get same icon in iconcache END*/
}
