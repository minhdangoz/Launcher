package com.klauncher.kinflow.views.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.klauncher.kinflow.model.GlobalCategory;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.utilities.ResourceUtils;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewAdapter;
import com.klauncher.kinflow.views.recyclerView.adapter.BaseRecyclerViewHolder;
import com.klauncher.launcher.R;

import java.util.List;

/**
 * Created by xixionghui on 16/8/24.
 */
public class GlobalCategoryAdapter extends BaseRecyclerViewAdapter<GlobalCategory,GlobalCategoryAdapter.GlobalCategoryAdapterViewHolder<GlobalCategory>> {

    /**
     * 构造方法
     * 此方法需要在子类中实现
     *
     * @param context
     * @param elementList
     */
    public GlobalCategoryAdapter(Context context, List<GlobalCategory> elementList) {
        super(context, elementList);
    }

    /**
     * 创建ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public GlobalCategoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemRootView = mInflater.inflate(R.layout.category_button,parent,false);
        return new GlobalCategoryAdapterViewHolder(itemRootView);
    }

    /**
     * ViewHolder绑定
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(GlobalCategoryAdapterViewHolder holder, int position) {
        holder.bundData2View(mElementList.get(position));
    }

    private int pixels (int dpi) {
        int multiple = dpi/160;//一个像素所占长度扩大或者缩小的倍数
        return multiple*24;//在160dpi的屏幕上navigation的icon大概需要24个像素
    }

    public class GlobalCategoryAdapterViewHolder<T> extends BaseRecyclerViewHolder<GlobalCategory> {
        Button categoryButton;
        public GlobalCategoryAdapterViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemRootView) {
            categoryButton = (Button) itemView.findViewById(R.id.shortcut_button);
        }

        @Override
        public void bundData2View(GlobalCategory globalCategory) {
            if (globalCategory.allFieldsIsNotNull()) {
                categoryButton.setText(globalCategory.getCategoryName());
                //button.setCompoundDrawables(left, top, right, bottom);

                int pixels = pixels(Dips.deviceDpi(mContext));
                Drawable drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(globalCategory.getCategoryIcon()));
                drawable.setBounds(0, 0, pixels,pixels);
                categoryButton.setCompoundDrawables(null,
                        drawable,
                        null,
                        null);
            }
        }
    }

    /**
     * 定义ViewHolder
     */
//    public class GlobalCategoryAdapterViewHolder extends RecyclerView.ViewHolder {
//        Button categoryButton;
//
//        public GlobalCategoryAdapterViewHolder(View itemView) {
//            super(itemView);
//            categoryButton = (Button) itemView.findViewById(R.id.category_button);
//        }
//        public void bindData(GlobalCategory globalCategory) {
//            if (globalCategory.allFieldsIsNotNull()) {
//                categoryButton.setText(globalCategory.getCategoryName());
//                //button.setCompoundDrawables(left, top, right, bottom);
//
//                int pixels = pixels(Dips.deviceDpi(mContext));
//                Drawable drawable = ResourceUtils.instance.resId2Drawable(Integer.valueOf(globalCategory.getCategoryIcon()));
//                drawable.setBounds(0, 0, pixels,pixels);
//                categoryButton.setCompoundDrawables(null,
//                        drawable,
//                        null,
//                        null);
//            }
//        }
//    }

}
