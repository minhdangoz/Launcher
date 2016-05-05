package com.klauncher.kinflow.navigation.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.launcher.R;
import com.klauncher.kinflow.common.utils.OpenMode;
import com.klauncher.kinflow.navigation.model.Navigation;

import java.util.List;

/**
 * Created by xixionghui on 2016/3/19.
 */
public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationAdapterViewHolder> {

    private Context context;
    private List<Navigation> navigationList;
    private LayoutInflater inflater;

    public NavigationAdapter(Context mContext, List<Navigation> navigationList) {
        this.context = mContext;
        this.navigationList = navigationList;
        inflater = LayoutInflater.from(mContext);
    }

    public void updateNavigationList(List<Navigation> list) {
        navigationList.clear();
        navigationList.addAll(list);
    }

    @Override
    public NavigationAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.adapter_navigation, parent, false);
        return new NavigationAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavigationAdapterViewHolder holder, int position) {
        Navigation navigation = navigationList.get(position);
        if (null==navigation||null==navigation.getNavName()||null==navigation.getNavIcon()) {
            this.navigationList.remove(position);
            navigation = CacheNavigation.getInstance().createDefaultNavigation(position);
            this.navigationList.add(position,navigation);
        }
        //navigation-name
        holder.tv_navigation.setText(navigation.getNavName());

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
            drawable.setBounds(0, 0, context.getResources().getInteger(R.integer.kinflow_integer_navigataion_icon_bound), context.getResources().getInteger(R.integer.kinflow_integer_navigataion_icon_bound));
            holder.tv_navigation.setCompoundDrawables(drawable, null, null, null);
            return;
        }
        byte[] decodedString = Base64.decode(navIcon, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if (null != decodedByte) {
            Drawable drawable = new BitmapDrawable(context.getResources(), decodedByte);
            drawable.setBounds(0, 0, context.getResources().getInteger(R.integer.kinflow_integer_navigataion_icon_bound), context.getResources().getInteger(R.integer.kinflow_integer_navigataion_icon_bound));
            holder.tv_navigation.setCompoundDrawables(drawable, null, null, null);
        }
    }

    @Override
    public int getItemCount() {
        return null == navigationList ? 0 : navigationList.size();
    }

    public class NavigationAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_navigation;

        public NavigationAdapterViewHolder(View itemView) {
            super(itemView);
            tv_navigation = (TextView) itemView.findViewById(R.id.adapter_navigation);
            tv_navigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();//新版用getLayoutPosition
                    //用指定的方式打开浏览器
                    Navigation navigation = navigationList.get(position);
                    List<String> navOpenOptions = navigation.getNavOpenOptions();
//                    Log.d("NavigationAdapterViewHo", ("要打开的网页地址： " + navigation.getNavUrl()));
//                    CommonUtils.getInstance().openDetail(context,navOpenOptions,navigation.getNavUrl());

                    Bundle extras = new Bundle();
                    extras.putString(OpenMode.OPEN_URL_KEY, navigation.getNavUrl());
                    navigation.open(context,extras);

                }
            });
        }
    }
}
