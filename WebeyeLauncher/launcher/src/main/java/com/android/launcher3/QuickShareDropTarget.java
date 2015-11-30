/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.io.File;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.Launcher.QuickShareState;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserHandleCompat;
import com.lenovo.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

interface OnShareToApkFileSearchEndListener {
	void onShareToApkFileSearchEndListener(ComponentName cn);
}

/**
 * 快速分享
 * @author zhaoxin5
 *
 */
public class QuickShareDropTarget extends ButtonDropTarget {

    private ColorStateList mOriginalTextColor;
    private TransitionDrawable mDrawable;
    private OnShareToApkFileSearchEndListener mListener;
    private LauncherAppsCompat mLauncherApps;
    private final String TAG = "QuickShareDropTarget";
    
    public QuickShareDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    	mLauncherApps = LauncherAppsCompat.getInstance(context);
    }

    public QuickShareDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    	mLauncherApps = LauncherAppsCompat.getInstance(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mOriginalTextColor = getTextColors();

        // Get the hover color
        Resources r = getResources();
        mHoverColor = r.getColor(R.color.info_target_hover_tint);
        mDrawable = (TransitionDrawable) getCurrentDrawable();

        if (mDrawable == null) {
            // TODO: investigate why this is ever happening. Presently only on one known device.
            mDrawable = (TransitionDrawable) r.getDrawable(R.drawable.info_target_selector);
            setCompoundDrawablesRelativeWithIntrinsicBounds(mDrawable, null, null, null);
        }

        if (null != mDrawable) {
            mDrawable.setCrossFadeEnabled(true);
        }

        // Remove the text in the Phone UI in landscape
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	/* Lenovo-SW zhaoxin5 20150403 BLADETL-1717 START */
            //if (!LauncherAppState.getInstance().isScreenLarge()) {
                setText("");
            //}
            /* Lenovo-SW zhaoxin5 20150403 BLADETL-1717 END */
        }
    }

    @Override
    public boolean acceptDrop(DragObject d) {
        // acceptDrop is called just before onDrop. We do the work here, rather than
        // in onDrop, because it allows us to reject the drop (by returning false)
        // so that the object being dragged isn't removed from the drag source.

    	// Lenovo-SW zhaoxin5 20150810 show InfoDropTarget instead of QuickShareDropTarget
    	if(true) {
    		return false;
    	} 
    	// Lenovo-SW zhaoxin5 20150810 show InfoDropTarget instead of QuickShareDropTarget
    	
        ComponentName componentName = null;
        Intent intent = null;
        if (d.dragInfo instanceof AppInfo) {
            componentName = ((AppInfo) d.dragInfo).componentName;
            intent = ((AppInfo) d.dragInfo).intent;
        } else if (d.dragInfo instanceof ShortcutInfo) {
            componentName = ((ShortcutInfo) d.dragInfo).intent.getComponent();
            intent = ((ShortcutInfo) d.dragInfo).intent;
        } else if (d.dragInfo instanceof PendingAddItemInfo) {
            componentName = ((PendingAddItemInfo) d.dragInfo).componentName;
            intent = null;
        }
        final UserHandleCompat user;
        if (d.dragInfo instanceof ItemInfo) {
            user = ((ItemInfo) d.dragInfo).user;
        } else {
            user = UserHandleCompat.myUserHandle();
        }

        if (componentName != null) {
           // mLauncher.startApplicationDetailsActivity(componentName, user);
        	// findAPKPathForComponent(componentName);
        	// mLauncher.getQuickShareBar().setVisibility(View.VISIBLE);
        	setPendingShareInfo(componentName, intent);
        	mLauncher.setNeedInterruptOnDragEnd(true);
        	mLauncher.startQuickShareStateChange(QuickShareState.HEAD_2_BODY);
        	// mLauncher.animateShowOrHideQuickShareBar(true, false);
        	
            Bitmap b = null;
            String title = "";
            ImageView appView = ((ImageView)mLauncher.getQuickShareZone().findViewById(R.id.quick_share_app_iv));
        	TextView appTip = (TextView) mLauncher.getQuickShareZone().findViewById(R.id.quick_share_app_tip);
            if(d.dragInfo instanceof ShortcutInfo) {
        		appView.setBackground(new BitmapDrawable(((ShortcutInfo)d.dragInfo).getIcon(mLauncher.getIconCache())));
        		title = (String) ((ShortcutInfo)d.dragInfo).title;
        		// mShareTo.applyFromShortcutInfo(((ShortcutInfo)d.dragInfo), mLauncher.getIconCache(), true);
        		// b = ((ShortcutInfo)d.dragInfo).getIcon(mLauncher.getIconCache());
        	} else if(d.dragInfo instanceof AppInfo) {
        		appView.setBackground(new BitmapDrawable(((AppInfo)d.dragInfo).iconBitmap));
        		title = (String) ((AppInfo)d.dragInfo).title;
        		// mShareTo.applyFromApplicationInfo(((AppInfo)d.dragInfo));
        		// b = ((AppInfo)d.dragInfo).iconBitmap;
        	} else if(d.dragInfo instanceof PendingAddItemInfo) {
        		
        	}
            Resources res = getResources();
            appTip.setText(/*"Share \"" + title + "\" to"*/res.getString(R.string.quick_share_to, title));
        }

        // There is no post-drop animation, so clean up the DragView now
        d.deferDragViewCleanupPostAnimation = false;
        return false;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 START */
    	super.onDragStart(source, info, dragAction);
    	/** Lenovo-SW zhaoxin5 20150702 LANCHROW-187 END */
        boolean isVisible = true;

        // Hide this button unless we are dragging something from AllApps
        /*if (!source.supportsAppInfoDropTarget()) {
            isVisible = false;
        }*/
        // Hide this button if the source is a folder
        if(info instanceof FolderInfo || mLauncher.getWorkspace().isInOverviewMode() || info instanceof LauncherAppWidgetInfo 
        		|| source instanceof AppsCustomizePagedView) {
        	isVisible = false;
        }

        /** Lenovo-SW zhaoxin5 20150729 KOLEOSROW-217 START */
        if(info instanceof ItemInfo) {
        	ItemInfo iInfo = (ItemInfo) info;
        	LauncherLog.i(TAG, "onDragStart itemType = " + iInfo.itemType);
        	if(iInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
        		// 添加的1*1小部件禁止用户分享
        		isVisible = false;
        	}
        }
        /** Lenovo-SW zhaoxin5 20150729 KOLEOSROW-217 END */

    	// Lenovo-SW zhaoxin5 20150810 show InfoDropTarget instead of QuickShareDropTarget
        if(true) {
        	isVisible = false;
        }
    	// Lenovo-SW zhaoxin5 20150810 show InfoDropTarget instead of QuickShareDropTarget
        
        mActive = isVisible;
        mDrawable.resetTransition();
        setTextColor(mOriginalTextColor);
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    public void onDragEnter(DragObject d) {
        super.onDragEnter(d);

        mDrawable.startTransition(mTransitionDuration);
        setTextColor(mHoverColor);
    	mLauncher.startQuickShareStateChange(QuickShareState.HIDE_2_HEAD);
    }

    public void onDragExit(DragObject d) {
        super.onDragExit(d);

        if (!d.dragComplete) {
            mDrawable.resetTransition();
            setTextColor(mOriginalTextColor);
        	mLauncher.startQuickShareStateChange(QuickShareState.HEAD_2_HIDE);
        }
    }
    
    class ShareInfo {
    	ComponentName cn;
    	String apkPath;
    	File apkFile;
    	public ShareInfo(ComponentName cn, String apkPath) {
    		this.cn = cn;
    		this.apkPath = apkPath;
    		this.apkFile = new File(apkPath);
    	}
    }
    
    private ShareInfo mPendingShareInfo = null;
    public void resetPendingShareInfo() {
    	mPendingShareInfo = null;
    }
    public ShareInfo getPendingShareInfo() {
    	return mPendingShareInfo;
    }
    
    /**
     * find the apk path for the specified ComponentName
     * @param cn
     */
    private void setPendingShareInfo(final ComponentName cn, final Intent intent) {
		new AsyncTask<Void, Void, Void>() {
            @Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
			}

			@Override
            protected Void doInBackground(Void... unused) {
				String apkPath = "";
				if(null != intent) {
			        final UserHandleCompat myUserHandle = UserHandleCompat.myUserHandle();
					LauncherActivityInfoCompat launcherActInfo = mLauncherApps.resolveActivity(intent, myUserHandle);
					apkPath = launcherActInfo.getApplicationInfo().publicSourceDir;
					LauncherLog.i(TAG, "setPendingShareInfo inten != null");
				} else {
					LauncherLog.i(TAG, "setPendingShareInfo inten == null");
					PackageManager pm = mLauncher.getPackageManager();
	            	List<ApplicationInfo> appInfos = pm.getInstalledApplications(0);
	            	for (int i=0; i<appInfos.size(); i++) {
	            		ApplicationInfo aInfo = appInfos.get(i);
	            		if(aInfo.packageName == null) {
	            			continue;
	            		}
	            		if(aInfo.packageName.equals(cn.getPackageName())) {
	            			apkPath = aInfo.publicSourceDir;
	            			break;
	            		}
	            	}
				}
    			mPendingShareInfo = new ShareInfo(cn, apkPath);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            	super.onPostExecute(result);
            	if(mListener != null) {
            		mListener.onShareToApkFileSearchEndListener(cn);
            	}
            }
        }.execute();
    }

	@Override
	void setLauncher(Launcher launcher) {
		// TODO Auto-generated method stub
		super.setLauncher(launcher);
		mListener = (OnShareToApkFileSearchEndListener)launcher;
	}
}
