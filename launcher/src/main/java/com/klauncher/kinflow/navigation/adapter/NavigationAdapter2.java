package com.klauncher.kinflow.navigation.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.views.PopupWindowDialog;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;

import java.util.List;

/**
 * Created by xixionghui on 2016/3/19.
 */
public class NavigationAdapter2 extends RecyclerView.Adapter<NavigationAdapter2.NavigationAdapterViewHolder> {

    private Context context;
    private List<Navigation> mNavigationList;
    private LayoutInflater inflater;
    PopupWindowDialog mPopupWindowDialog;

    public NavigationAdapter2(Context mContext, List<Navigation> navigationList) {
        this.context = mContext;
        this.mNavigationList = navigationList;
        inflater = LayoutInflater.from(mContext);
        mPopupWindowDialog = getPopupWindowDialog();
    }

    public void updateNavigationList(List<Navigation> newNavigationList) {
        //如果不满足个数,则按照位置指定替换..如果超出指定个数则忽略
        int size = newNavigationList.size();
        Log.e("Kinflow","要更新的Navigation个数="+newNavigationList.size());
        if (size==10) {
            mNavigationList.clear();
            mNavigationList.addAll(newNavigationList);
        }else if (size<10) {//啥也不做
            mNavigationList.clear();
            mNavigationList.addAll(newNavigationList);
        }else if (size>10) {
            mNavigationList.clear();
            mNavigationList = newNavigationList.subList(0, 10);
        }
        notifyDataSetChanged();
    }

    private void replaceNavigation(Navigation navigation) {
        int position = navigation.getNavOrder();
        if (mNavigationList.size()>position) {
           mNavigationList.remove(position);
            mNavigationList.add(position,navigation);
        }
    }

    @Override
    public NavigationAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.adapter_navigation2, parent, false);
        return new NavigationAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavigationAdapterViewHolder holder, int position) {
        try {
            Navigation navigation = mNavigationList.get(position);
            if (null==navigation||null==navigation.getNavName()||null==navigation.getNavIcon()) {
                this.mNavigationList.remove(position);
                navigation = CacheNavigation.getInstance().createDefaultNavigation(position);
                this.mNavigationList.add(position, navigation);
            }
            //navigation-pixels
            int pixels = pixels(Dips.deviceDpi(context));

            //navigation-name
            holder.navigationName.setText(navigation.getNavName());

            //navigation-name-color
//            holder.navigationName.setTextColor(Color.BLACK);

            //navigation-icon
            String navIcon = navigation.getNavIcon();

            if (navIcon.startsWith("default/")) {
                Bitmap bitmap = null;
                switch (position) {
                    case 0://新浪
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.sina);
                        break;
                    case 1://淘宝
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.taobao);
                        break;
                    case 2://京东
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.jd);
                        break;
                    case 3://同城
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.tongcheng);
                        break;
                    case 4://美团
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.meituan);
                        break;
                    case 5://携程
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ctrip);
                        break;
                    case 6://优酷
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.youku);
                        break;
                    case 7://更多
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.more);
                        break;

                }
                Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                drawable.setBounds(0, 0, pixels,pixels);
                holder.navigationIcon.setImageDrawable(drawable);
                return;
            }

            byte[] decodedString = Base64.decode(navIcon, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (null != decodedByte) {
                Drawable drawable = new BitmapDrawable(context.getResources(), decodedByte);
                drawable.setBounds(0, 0, pixels,pixels);
                holder.navigationIcon.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int pixels (int dpi) {
        int multiple = dpi/160;//一个像素所占长度扩大或者缩小的倍数
        return multiple*24;//在160dpi的屏幕上navigation的icon大概需要24个像素
    }

    @Override
    public int getItemCount() {
        return null == mNavigationList ? 0 : mNavigationList.size();
    }

    public PopupWindowDialog getPopupWindowDialog() {
        try {
            if (null==mPopupWindowDialog) mPopupWindowDialog = new PopupWindowDialog(context);
        } catch (Exception e) {
            mPopupWindowDialog = new PopupWindowDialog(context);
        } finally {
            return mPopupWindowDialog;
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
                    CommonShareData.putBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, mPopupWindowDialog.getCompoundState()?true:false);//用户允许始终使用网络
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

    public void clickNavigation (Navigation navigation) {

        try {
            Bundle extras = new Bundle();
            extras.putString(OpenMode.OPEN_URL_KEY, navigation.getNavUrl());
            navigation.open(context, extras);
            PingManager.getInstance().reportUserAction4Navigation(navigation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class NavigationAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView navigationName;
        private ImageView navigationIcon;

        public NavigationAdapterViewHolder(View itemView) {
            super(itemView);

            try {
                navigationIcon = (ImageView) itemView.findViewById(R.id.navigation_icon);
                navigationName = (TextView) itemView.findViewById(R.id.navigation_name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getPosition();//新版用getLayoutPosition
                        Navigation navigation = mNavigationList.get(position);

                        boolean userAllowKinflowUseNet = CommonShareData.getBoolean(CommonShareData.KEY_USER_ALWAYS_ALLOW_KINFLOW_USE_NET, false);//用户允许信息流使用网络
                        if (!userAllowKinflowUseNet) {
                            showFirstConnectedNetHint(navigation);
                            return;
                        }
                        clickNavigation(navigation);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
