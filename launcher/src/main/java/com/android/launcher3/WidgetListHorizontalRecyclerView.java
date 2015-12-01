package com.android.launcher3;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.launcher3.WidgetListViewSwitcher.ShowType;
import com.webeye.launcher.ext.LauncherLog;
import com.webeye.launcher.R;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WidgetListHorizontalRecyclerView extends RecyclerView {

	final static String TAG = WidgetListHorizontalRecyclerView.class.getSimpleName();
	private Context mContext = null;
	private AppInfo mGroupInfo = null; //如果是第二层记录当前显示的widget家族信息
    
    private OnWidgetListClickListener mClickListener = null;
    private ArrayList<AppInfo> mAllFirstList = new ArrayList<AppInfo>(); // 存放第一层所有的app 分组信息
    private ArrayList<Object> mCurrentSecondList = new ArrayList<Object>();// 存放第二层的widget的数组
    private HashMap<String, ArrayList<Object>> mAllSecondList = new HashMap<String, ArrayList<Object>>(); //存放所有第二层的widget信息

    private ShowType mMyShowType = ShowType.FIRST_APP;
    private PackageManager mPackageManager;
    
    private HorizontalRecyclerViewViewAdapter mAdatper = null;
    private WidgetPreviewLoader mWidgetPreviewLoader = null;
    
	public WidgetListHorizontalRecyclerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setupViews(context);
	}

	public WidgetListHorizontalRecyclerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setupViews(context);
	}

	public WidgetListHorizontalRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setupViews(context);
	}

	void setupViews(Context context) {
		mContext = context;
		mPackageManager = mContext.getPackageManager();
		LinearLayoutManager layoutManager = new LinearLayoutManager(context);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);// 水平
		this.setLayoutManager(layoutManager);
	}
    
	private void createAdapter() {
		// 创建Adapter，并指定数据集
		mAdatper = new HorizontalRecyclerViewViewAdapter();
		// 设置Adapter
		this.setAdapter(mAdatper);
	}
	
	public void updateAdapterForSecond(AppInfo groupInfo, ArrayList<Object> currentSecondeList,
			OnWidgetListClickListener listener, WidgetPreviewLoader w) {
		// 第二层
		mMyShowType = ShowType.SECOND_WIDGET;
		mGroupInfo = groupInfo;
		mCurrentSecondList = currentSecondeList;
		mClickListener = listener;
		mWidgetPreviewLoader = w;
		
		createAdapter();
	}
	
	public void updateAdapterForFirst(ArrayList<AppInfo> allFirstList, HashMap<String, ArrayList<Object>> allSecondList, 
			OnWidgetListClickListener listener) {
		// 第一层
		mMyShowType = ShowType.FIRST_APP;
		mAllFirstList = allFirstList;
		mAllSecondList = allSecondList;
		mClickListener = listener;
		
		if(false) {
        	for(int i=0; i<mAllFirstList.size(); i++){
        		String packageName = mAllFirstList.get(i).componentName.getPackageName();
        		StringBuilder sb = new StringBuilder();
        		sb.append((i+1) + "," + packageName + ":");
        		sb.append("\n");
        		
        		ArrayList<Object> second = mAllSecondList.get(packageName);
        		for(int j=0; j<second.size(); j++) {
        			// sb.append(((PendingAddItemInfo)second.get(j)).componentName.flattenToShortString());
        			sb.append(getObjectTitle(second.get(j)));
        			sb.append("\n");
        		}
        		sb.append("\n");
        		LauncherLog.i(TAG, sb.toString());
        	}
		}
		
		createAdapter();
	}
    
    private String getObjectPackage(Object o) {
        if (o instanceof AppWidgetProviderInfo) {
            return ((AppWidgetProviderInfo) o).provider.getPackageName();
        } else {
            ResolveInfo info = (ResolveInfo) o;
            return info.activityInfo.packageName;
        }
    }
    
    private String getObjectTitle(Object o){
        if (o instanceof AppWidgetProviderInfo) {
            return ((AppWidgetProviderInfo) o).label;
        } else {
            ResolveInfo info = (ResolveInfo) o;
            return (String) info.loadLabel(mPackageManager);
        }
    }
	
	public class HorizontalRecyclerViewViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewViewAdapter.ViewHolder>{

		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return mMyShowType == ShowType.FIRST_APP ? mAllFirstList.size() : mCurrentSecondList.size();
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int i) {
			LauncherLog.i(TAG, "onBindViewHolder");
			// 绑定数据到ViewHolder上
			switch (mMyShowType) {
				case FIRST_APP:
					/** Lenovo-SW zhaoxin5 修改这里,以免Widget中的APP图标显示的很诡异,细长细长的 START */
					int textSize = (int) mContext.getResources().getDimension(R.dimen.infomation_count_textsize);
			        Drawable drawable = Utilities.createIconDrawable(mAllFirstList.get(i).iconBitmap);
			        Rect bounds = new Rect();
			        drawable.copyBounds(bounds);
			        bounds.top = bounds.top - textSize/2;
			        bounds.right = bounds.right + textSize;
			        drawable.setBounds(bounds);	
					/** Lenovo-SW zhaoxin5 修改这里,以免Widget中的APP图标显示的很诡异,细长细长的 END */
					viewHolder.mFirst.imageView.setImageDrawable(drawable);
					int childSize = mAllSecondList.get(mAllFirstList.get(i).componentName.getPackageName()).size();
					String title = "";
					
					if(WidgetListViewSwitcher.isDirectlyShowWidget(mAllFirstList.get(i))) {
						AppWidgetProviderInfo widget = (AppWidgetProviderInfo) mAllSecondList.get(mAllFirstList.get(i).componentName.getPackageName()).get(0);
						int[] spanXY = Launcher.getSpanForWidget(mContext, widget);
						title = (String) mAllFirstList.get(i).title;
				        viewHolder.mRootView.setTag(mAllSecondList.get(mAllFirstList.get(i).componentName.getPackageName()).get(0));
					} else {
						title = mAllFirstList.get(i).title + " (" + childSize + ")";
				        viewHolder.mRootView.setTag(mAllFirstList.get(i));
					}
					
					viewHolder.mFirst.name.setText(title);
					break;
				case SECOND_WIDGET:
					viewHolder.mSecond.name.setText(getObjectTitle(mCurrentSecondList.get(i)));
					if(mCurrentSecondList.get(i) instanceof ResolveInfo) {
						viewHolder.mSecond.dims.setText("1 x 1");
						
						// 设置Widget的Preview图片
						ResolveInfo rInfo = (ResolveInfo) mCurrentSecondList.get(i);
						Drawable d = rInfo.loadIcon(mPackageManager);
						viewHolder.mSecond.imageView.setImageDrawable(d);
					} else if(mCurrentSecondList.get(i) instanceof AppWidgetProviderInfo) {
						AppWidgetProviderInfo widget = (AppWidgetProviderInfo) mCurrentSecondList.get(i);
						int[] spanXY = Launcher.getSpanForWidget(mContext, widget);
						viewHolder.mSecond.dims.setText(spanXY[0] + " x " + spanXY[1]);
						
						// 设置Widget的Preview图片
						AppWidgetProviderInfo wInfo = (AppWidgetProviderInfo) mCurrentSecondList.get(i);
				        Drawable d = Utilities.createIconDrawable(mWidgetPreviewLoader.getPreview(wInfo));
						viewHolder.mSecond.imageView.setImageDrawable(d);
					}
					viewHolder.mRootView.setTag(mCurrentSecondList.get(i)); // 第二层
					break;
				default:
					break;
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
			LauncherLog.i(TAG, "onCreateViewHolder");
	        // 创建一个View，简单起见直接使用系统提供的布局，就是一个TextView
			View view = null;
			switch (mMyShowType) {
			case FIRST_APP:
				view = View.inflate(viewGroup.getContext(), R.layout.widgetlist_first_appinfo, null);
				break;
			default:
				view = View.inflate(viewGroup.getContext(), R.layout.widgetlist_second_widget, null);
				break;
			}
	        // 创建一个ViewHolder
	        ViewHolder holder = new ViewHolder(view);
	        return holder;
		}
    	
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        	public View mRootView;
            public Second mSecond = new Second();
            public First mFirst = new First();
            
            class First {
            	LinearLayout root;
            	ImageView imageView;
            	TextView name;
            }
            
            class Second {
            	LinearLayout root;
            	ImageView imageView;
            	TextView name;
            	TextView dims;
            }
            
            public ViewHolder(View rootView) {
                super(rootView);
                mRootView = rootView;
                mRootView.setOnClickListener(this);
                mRootView.setOnLongClickListener(this);
                
                switch (mMyShowType) {
					case FIRST_APP:
						mFirst.root = (LinearLayout) rootView;
						mFirst.imageView = (ImageView) mFirst.root.findViewById(R.id.image_view);
						mFirst.name = (TextView) mFirst.root.findViewById(R.id.name);
						break;
					default:
						mSecond.root = (LinearLayout) rootView;
						mSecond.imageView = (ImageView) mSecond.root.findViewById(R.id.image_view);
						mSecond.name = (TextView) mSecond.root.findViewById(R.id.name);
						mSecond.dims = (TextView) mSecond.root.findViewById(R.id.dims);
						break;
				}
            }

    		@Override
    		public void onClick(View v) {
    			// TODO Auto-generated method stub
    			if(mClickListener != null) {
    				mClickListener.onClick(v);
    			}
    		}

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(mClickListener != null) {
    				mClickListener.onLongClick(v);
    			}
				return true;
			}
        }
    }
}
