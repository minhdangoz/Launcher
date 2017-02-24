package com.klauncher.kinflow.views.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.ResourceUtils;
import com.klauncher.kinflow.views.PopupWindowDialog;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewAdapter;
import com.klauncher.kinflow.views.recyclerView.viewHolder.BaseRecyclerViewHolder;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;

import java.util.List;

/**
 * Created by xixionghui on 16/8/24.
 */
public class GlobalCategoryAdapter extends BaseRecyclerViewAdapter<Navigation, GlobalCategoryAdapter.GlobalCategoryAdapterViewHolder<Navigation>> {

    /**
     * 构造方法
     * 此方法需要在子类中实现
     *
     * @param context
     * @param elementList
     */
    public GlobalCategoryAdapter(Context context, List<Navigation> elementList) {
        super(context, elementList);
        try {
            mPopupWindowDialog = getPopupWindowDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建ViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public GlobalCategoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        try {
            View itemRootView = mInflater.inflate(R.layout.adapter_navigation_content, parent, false);
            return new GlobalCategoryAdapterViewHolder(itemRootView);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ViewHolder绑定
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(GlobalCategoryAdapterViewHolder holder, int position) {
        try {
            holder.bundData2View(mElementList.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int pixels(int dpi) {
        int multiple = dpi / 160;//一个像素所占长度扩大或者缩小的倍数
        return multiple * 24;//在160dpi的屏幕上navigation的icon大概需要24个像素
    }

    public class GlobalCategoryAdapterViewHolder<T> extends BaseRecyclerViewHolder<Navigation> implements View.OnClickListener {
        private TextView navigationName;
        private ImageView navigationIcon;

        public GlobalCategoryAdapterViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemRootView) {
            try {
                navigationIcon = (ImageView) itemRootView.findViewById(R.id.navigation_icon);
                navigationName = (TextView) itemRootView.findViewById(R.id.navigation_name);
                itemRootView.setOnClickListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void bundData2View(Navigation navigation) {

            try {
                this.modelData = navigation;
                int position = mElementList.indexOf(navigation);
                navigationName.setText(navigation.getNavName());
//                int pixels = pixels(Dips.deviceDpi(mContext));
                Drawable drawable = null;
                String navIcon = navigation.getNavIcon();
                if (navIcon.startsWith("default_content")) {
    //                drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(navIcon.replace("default_content/","")));
                    switch (position) {
                        case 0:
                            drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(R.drawable.notification_tool_light_web));
                            break;
                        case 1:
                            drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(R.drawable.notification_tool_light_novel));
                            break;
                        case 2:
                            drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(R.drawable.notification_tool_light_video));
                            break;
                        case 3:
                            drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(R.drawable.notification_tool_light_funny));
                            break;
                        case 4:
                            drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(R.drawable.notification_tool_light_app));
                            break;
                    }
                } else {
                    /*
                     byte[] decodedString = Base64.decode(navIcon, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (null != decodedByte) {
                Drawable drawable = new BitmapDrawable(context.getResources(), decodedByte);
                drawable.setBounds(0, 0, pixels,pixels);
                holder.navigationIcon.setImageDrawable(drawable);
            }
                     */
                    byte[] decodedString = Base64.decode(navIcon, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (null != decodedByte) {
                        drawable = new BitmapDrawable(mContext.getResources(), decodedByte);
//                        drawable.setBounds(0, 0, pixels,pixels);
                    }
                }
                navigationIcon.setImageDrawable(drawable);
                navigationName.setText(navigation.getNavName());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onClick(View v) {
            try {
                boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);//用户允许信息流使用网络
                if (!userAllowKinflowUseNet) {
                    showFirstConnectedNetHint(this.modelData);
                    return;
                }
                clickNavigation(this.modelData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void clickNavigation(Navigation navigation) {

        try {
            Bundle extras = new Bundle();
            String navUrl = navigation.getNavUrl();
            if (!navUrl.startsWith("http://"))
                navUrl = "http://"+ navUrl;
            extras.putString(OpenMode.OPEN_URL_KEY, navUrl);
            KinflowLog.w(navigation.getNavigationList());
//            navigation.open(mContext, extras);
            String finalOpenComponent = navigation.openByOrder(mContext);
//            PingManager.getInstance().reportUserAction4Navigation(navigation);
            PingManager.getInstance().reportUserAction4NavigationOpen(PingManager.VALUE_NAVIGATION_TYPE_CONTENT,navigation,finalOpenComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showFirstConnectedNetHint(final Navigation navigation) {
        try {
//            onCompleted();//如果正在刷新,则停止
//            getPopupWindowDialog().showPopupWindowDialog();
            getPopupWindowDialog().showPopupWindowDialog(new PopupWindowDialog.PopupWindowDialogListener() {
                @Override
                public void cancleClick() {
                    //第一次联网---->true
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, true);
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
                }

                @Override
                public void okClick() {
                    //第一次联网---->false
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, mPopupWindowDialog.getCompoundState() ? true : false);//用户允许始终使用网络
                    CommonShareData.putBoolean(CommonShareData.FIRST_CONNECTED_NET, false);
                    mPopupWindowDialog.dismissPopupWindowDialog();
//                    requestKinflowData(msgWhats);
                    clickNavigation(navigation);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    PopupWindowDialog mPopupWindowDialog;

    public PopupWindowDialog getPopupWindowDialog() {
        try {
            if (null == mPopupWindowDialog) mPopupWindowDialog = new PopupWindowDialog(mContext);
        } catch (Exception e) {
            mPopupWindowDialog = new PopupWindowDialog(mContext);
        } finally {
            return mPopupWindowDialog;
        }
    }

}
