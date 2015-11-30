package com.android.launcher3.lenovosearch;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ShortcutInfo;
import com.webeye.launcher.R;

public class SearchAppView extends RelativeLayout implements TextWatcher, View.OnClickListener {

	private LenovoAppSearchActivity mLenovoAppSearchActivity;
	private Context context;
	private GridView search_result_content;
	public TextView tv_search_title;
	public Handler mHandler;
	public SearchLocalAdapter adapter;
	private Launcher mLauncher;
	public static final int REFRESH_ADAPTER = 11;
	private static final boolean DBG = false;
	private static final String TAG = "SearchAppView";
	public String input = "";

	public SearchAppView(Context context, AttributeSet paramAttributeSet) {
		super(context);
		this.context = context;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterTextChanged(final Editable s) {
		input = s.toString();
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ArrayList<AppInfo> list;
				if (mLauncher != null) {
					list = mLauncher.getSearchItems(s.toString());
					mHandler.removeMessages(REFRESH_ADAPTER);
					Message msg = Message.obtain();
					if (TextUtils.isEmpty(s.toString())) {
						msg.arg1 = 1;
					} else {
						msg.arg1 = 2;
					}
					msg.obj = list;
					msg.what = REFRESH_ADAPTER;
					mHandler.sendMessage(msg);
				}

			}
		}.start();
	}

	public ArrayList<AppInfo> getSearchItems(String number) {
		return null;
	}

	public void setLenovoAppSearchActivity(LenovoAppSearchActivity LenovoAppSearchActivity, Launcher launcher) {
		this.mLenovoAppSearchActivity = LenovoAppSearchActivity;
		this.mLauncher = launcher;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		search_result_content = (GridView) findViewById(R.id.search_result_content);
		tv_search_title = (TextView) findViewById(R.id.search_title);
		tv_search_title.addTextChangedListener(this);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == REFRESH_ADAPTER) {
					ArrayList<AppInfo> list = (ArrayList<AppInfo>) msg.obj;
					if (list == null || (list != null && list.size() == 0)) {
						list = new ArrayList<AppInfo>();
					}
					adapter = new SearchLocalAdapter(context, list);
					if (search_result_content != null) {
						search_result_content.setAdapter(adapter);
					}
				}
			}
		};
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        LinearLayout.LayoutParams gridParams =  (LinearLayout.LayoutParams)search_result_content.getLayoutParams();
        Resources r = context.getResources();
        gridParams.height = grid.cellHeightPx * 3 + (int)r.getDimension(R.dimen.grid_search_padding) * 6;
        search_result_content.setLayoutParams(gridParams);
        if (TextUtils.isEmpty(tv_search_title.getText().toString())) {
			search_result_content.setVisibility(View.INVISIBLE);
		} else {
			search_result_content.setVisibility(View.VISIBLE);
		}
	}

	public void startSearchApp(ComponentName mComponetName) {
		mLenovoAppSearchActivity.finish();
		try {
			Intent intent = new Intent("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.LAUNCHER");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(mComponetName);
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
		}

	}

	class SearchLocalAdapter extends BaseAdapter {

		public Context context;
		public ArrayList<AppInfo> list;
		public LayoutInflater mInflater;
		public ArrayList<View> mViewList = new ArrayList<View>();

		public SearchLocalAdapter(Context context, ArrayList<AppInfo> list) {
			this.context = context;
			if (list != null) {
				if (DBG) {
					Log.i(TAG, "SearchLocalAdapter list.size: " + list.size());
				}
			}
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.list = list;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (this.list == null) {
				return 0;
			}
			return this.list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private int getLine1Color() {
			return getResources().getColor(R.color.quantum_panel_text_color);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo info = list.get(position);
			final BubbleTextView btv;
			if (convertView == null) {
				convertView = (BubbleTextView) mInflater.inflate(R.layout.application, null);
				mViewList.add(convertView);
			}
			btv = (BubbleTextView) convertView;
			btv.setTextColor(getLine1Color());
			ShortcutInfo si = new ShortcutInfo(info);

			si.title = TextUtilities.highlightTermsInText(si.title.toString(), input, getLine1Color(), 0, si.title.length());
			btv.applyFromShortcutInfo(si, LauncherAppState.getInstance().getIconCache(), false);
			btv.setOnClickListener(SearchAppView.this);

			if (list.size() != 0) {
				return btv;
			}
			return null;
		}

	}

	@Override
	public void onClick(View v) {

		// TODO Auto-generated method stub
		Animation mScaleAnimation = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f,// 整个屏幕就0.0到1.0的大小//缩放
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		final ShortcutInfo mShortcutInfo = ((ShortcutInfo) v.getTag());
		final ComponentName mComponetName = mShortcutInfo.getTargetComponent();
		mScaleAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 单击打开应用
				startSearchApp(mComponetName);
			}
		});
		mScaleAnimation.setDuration(50);
		mScaleAnimation.setFillAfter(true);
		v.startAnimation(mScaleAnimation);

	}
}
