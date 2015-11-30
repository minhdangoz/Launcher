package com.android.launcher3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.launcher3.Launcher.QuickShareState;
import com.android.launcher3.QuickShareDropTarget.ShareInfo;
import com.lenovo.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

/**
 * 快速分享PopupWindow
 * @author zhaoxin5
 *
 */
public class QuickShareGridView extends GridView {

	final String TAG = "QuickShareGridView";
	Launcher mLauncher;
	public QuickShareGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setupViews(Launcher context) {
		mLauncher = context;
		this.setHorizontalSpacing(20);
		this.setNumColumns(4);
		this.setVerticalSpacing(20);
		this.setScrollBarStyle(AbsListView.SCROLLBARS_INSIDE_OVERLAY);
	}

	private HashMap<Object, ActivityInfo> mShareToInfos = new HashMap<Object, ActivityInfo>();
	private List<Object> getSentToResolveInfos(Context context) {
		PackageManager pm = context.getPackageManager();
		final Intent sendToIntent = new Intent(Intent.ACTION_SEND);
		sendToIntent.setType("application/vnd.android.package-archive");
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(sendToIntent, PackageManager.MATCH_DEFAULT_ONLY);
		List<Object> infos = new ArrayList<Object>();
		for(int i=0; i<resolveInfos.size(); i++) {
			AppInfo appInfo = queryAppInfo(resolveInfos.get(i));
			LauncherLog.i(TAG, "share via " + i+ " , " + resolveInfos.get(i).activityInfo.toString());
			if(appInfo != null && !infos.contains(appInfo)) {
				infos.add(appInfo);
				mShareToInfos.put(appInfo, resolveInfos.get(i).activityInfo);
			}
			/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 START */
			if(appInfo == null) {
				infos.add(resolveInfos.get(i).activityInfo);
				mShareToInfos.put(resolveInfos.get(i).activityInfo, resolveInfos.get(i).activityInfo);
			}
			/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 END */
		}
		return infos;
	}
	
	private List<AppInfo> mAppList = null;
	public void setAppInfo(List<AppInfo> list) {
		mAppList = list;
		this.setAdapter(new GridViewAdapter(getSentToResolveInfos(mLauncher)));
	}
	
	AppInfo queryAppInfo(ResolveInfo rInfo) {
		for(int i=0; i<mAppList.size(); i++) {
			AppInfo aInfo = mAppList.get(i);
			if(aInfo.componentName.getPackageName().equals(rInfo.activityInfo.packageName)) {
				return aInfo;
			}
		}
		return null;
	}
	
	class ViewHolder {
		BubbleTextView textView;
	}
	
	class GridViewAdapter extends BaseAdapter {
		
		PackageManager mPm;
		List<Object> mList;
		public GridViewAdapter(List<Object> list) {
			this.mList = list;
			mPm = mLauncher.getPackageManager();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(mLauncher, R.layout.application, null);
				holder.textView = (BubbleTextView) convertView;
				convertView.setTag(R.id.tag_quick_share_view_holder, holder);
				convertView.setTag(R.id.tag_quick_share_view_app_info, mList.get(position));
			} else {
				holder = (ViewHolder) convertView.getTag(R.id.tag_quick_share_view_holder);
			}

			/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 START */
			Object o = mList.get(position);
			if(o instanceof AppInfo) {
				AppInfo aInfo = (AppInfo) o;
				holder.textView.applyFromApplicationInfo(aInfo);	
			} else {
		        LauncherAppState app = LauncherAppState.getInstance();
		        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
				ActivityInfo acInfo = (ActivityInfo) o;
				holder.textView.setText(acInfo.loadLabel(mPm));
				Drawable topDrawable = acInfo.loadIcon(mPm);
		        topDrawable.setBounds(0, 0, grid.allAppsIconSizePx, grid.allAppsIconSizePx);
		        holder.textView.setCompoundDrawables(null, topDrawable, null, null);
		        holder.textView.setCompoundDrawablePadding(grid.iconDrawablePaddingPx);
			}
			/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 END */
			
			holder.textView.setClickable(false);
			holder.textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ShareInfo sInfo = mLauncher.getSearchBar().getQuickShareDropTarget().getPendingShareInfo();
					if(null != sInfo) {
						Toast.makeText(mLauncher, Uri.fromFile(sInfo.apkFile).toString(), Toast.LENGTH_LONG).show();

						/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 START */
						Object shareToTarget = v.getTag(R.id.tag_quick_share_view_app_info);
						/** Lenovo-SW zhaoxin5 20150804 XTHREEROW-561 END */
						
						Intent shareIntent = new Intent();
						ComponentName cn = new ComponentName(mShareToInfos.get(shareToTarget).packageName, mShareToInfos.get(shareToTarget).name);
						shareIntent.setComponent(cn);
						shareIntent.setAction(Intent.ACTION_SEND);
						shareIntent.setType("application/vnd.android.package-archive");
						shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sInfo.apkFile));
						shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						((Launcher)mLauncher).startActivity(Intent.createChooser(shareIntent, "Share to"));
					}
					((Launcher)mLauncher).startQuickShareStateChange(QuickShareState.BODY_2_HIDE);
				}
			});
			return convertView;
		}
	}
}
