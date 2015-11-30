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

import java.lang.reflect.Field;

import com.lenovo.launcher.ext.LauncherLog;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;
    private ActivityInfo mActivityInfo;
    private String TAG = "LauncherActivityInfoCompatVL";

    LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        super();
        mLauncherActivityInfo = launcherActivityInfo;
    }

    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(mLauncherActivityInfo.getUser());
    }

    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density) {
        return mLauncherActivityInfo.getIcon(density);
    }

    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    public Drawable getBadgedIcon(int density) {
        return mLauncherActivityInfo.getBadgedIcon(density);
    }

    /* Lenovo-SW zhaoxin5 20150410 add new interface to get ActivityInfo to avoid all
     * activities in one application to get same icon in iconcache START*/
	@Override
	public ActivityInfo getActivityInfo() {
		if(null != mActivityInfo) return mActivityInfo;
		try{
			Class<LauncherActivityInfo> cls = LauncherActivityInfo.class;
			//访问私有变量
			Field field = cls.getDeclaredField("mActivityInfo");
			field.setAccessible(true);
			mActivityInfo = (ActivityInfo) field.get(mLauncherActivityInfo);
		}catch(Exception ex){
			LauncherLog.i(TAG,"exception in getActivityInfo", ex);
			mActivityInfo = null;
		}
		if(mActivityInfo != null && LauncherLog.DEBUG){
			LauncherLog.i(TAG, mActivityInfo.packageName + ", " + mActivityInfo.name + "; getActivityInfo successful");
		}
		if(mActivityInfo == null && LauncherLog.DEBUG){
			LauncherLog.i(TAG, mLauncherActivityInfo.getComponentName() + "; getActivityInfo failed");
		}
		return mActivityInfo;
	}
    /* Lenovo-SW zhaoxin5 20150410 add new interface to get ActivityInfo to avoid all
     * activities in one application to get same icon in iconcache END*/
}
