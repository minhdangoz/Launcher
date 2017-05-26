package com.kapp.kinflow.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.business.beans.WordLinkBean;
import com.kapp.knews.base.imagedisplay.glide.GlideDisplay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/27 16:55
 */

public class DetailRightLayout extends FrameLayout implements View.OnClickListener {

    private RelativeLayout containerCategory;
    private ImageView ivCategory;
    private TextView tvCategory;
    private TextView tvUpOne;
    private TextView tvUpTwo;
    private TextView tvUpThree;
    private TextView tvDownOne;
    private TextView tvDownTwo;
    private TextView tvDownThree;
    private LinearLayout containerExtra;
    private TextView tvExtraOne;
    private TextView tvExtraTwo;
    private TextView tvExtraThree;
    private TextView tvExtraFour;

    private OnDetailRightItemClickListener listener;
    private LinearLayout containerRight;
    private DetailRightData data;
    private View lineExtra;

    public DetailRightLayout(Context context) {
        super(context);
    }

    public DetailRightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailRightLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetailRightLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        intiViews(getContext());
    }

    private void intiViews(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.detail_right_layout, null);
        findViews(root);
    }


    private void findViews(View root) {
        containerCategory = (RelativeLayout) root.findViewById(R.id.container_category);
        ivCategory = (ImageView) root.findViewById(R.id.iv_category);
        tvCategory = (TextView) root.findViewById(R.id.tv_category);
        containerRight = (LinearLayout) root.findViewById(R.id.container_right);
        tvUpOne = (TextView) root.findViewById(R.id.tv_up_one);
        tvUpTwo = (TextView) root.findViewById(R.id.tv_up_two);
        tvUpThree = (TextView) root.findViewById(R.id.tv_up_three);
        tvDownOne = (TextView) root.findViewById(R.id.tv_down_one);
        tvDownTwo = (TextView) root.findViewById(R.id.tv_down_two);
        tvDownThree = (TextView) root.findViewById(R.id.tv_down_three);

        containerExtra = (LinearLayout) root.findViewById(R.id.container_extra);
        lineExtra = root.findViewById(R.id.line_extra);

        tvExtraOne = (TextView) root.findViewById(R.id.tv_extra_one);
        tvExtraTwo = (TextView) root.findViewById(R.id.tv_extra_two);
        tvExtraThree = (TextView) root.findViewById(R.id.tv_extra_three);
        tvExtraFour = (TextView) root.findViewById(R.id.tv_extra_four);

        containerCategory.setOnClickListener(this);
        tvUpOne.setOnClickListener(this);
        tvUpTwo.setOnClickListener(this);
        tvUpThree.setOnClickListener(this);
        tvDownOne.setOnClickListener(this);
        tvDownTwo.setOnClickListener(this);
        tvDownThree.setOnClickListener(this);
        tvExtraOne.setOnClickListener(this);
        tvExtraTwo.setOnClickListener(this);
        tvExtraThree.setOnClickListener(this);
        tvExtraFour.setOnClickListener(this);

        addView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setData(DetailRightData data) {
        this.data = data;
        fillData(data);
    }

    private void fillData(DetailRightData data) {
        if (null != data) {
            if (null == data.extraDatas) {
                containerCategory.setBackgroundResource(R.drawable.launcher_navigation_category_bg);
            } else {
                containerCategory.setBackgroundResource(R.drawable.launcher_navigation_category_bg_more);
            }
            if (0 != data.bigImageResId) {
                ivCategory.setImageResource(data.bigImageResId);
            }
            if (null != data.bigImageUrl && !"".equals(data.bigImageUrl)) {
                GlideDisplay.getInstance().display(getContext(), ivCategory, data.bigImageUrl);
            }
            if (null != data.bigText) {
                tvCategory.setText("[" + data.bigText + "]");
            }
            if (0 != data.bigTextResId) {
                tvCategory.setText("[" + getResources().getString(data.bigTextResId) + "]");
            }
            fillRightData(data.rightDatas);
            fillExtraData(data.extraDatas);
        }
    }

    private void fillExtraData(List<WordLinkBean> extraDatas) {
        if (null == extraDatas) {
            return;
        }
        int size = extraDatas.size();
        for (int i = 0; i < size; i++) {
            WordLinkBean item = extraDatas.get(i);
            if (null == item) {
                continue;
            }
            if (0 == i) {
                tvExtraOne.setText(item.text);
            } else if (1 == i) {
                tvExtraTwo.setText(item.text);
            } else if (2 == i) {
                tvExtraThree.setText(item.text);
            } else if (3 == i) {
                tvExtraFour.setText(item.text);
            } else {
                break;
            }
        }
    }

    private void fillRightData(List<WordLinkBean> rightDatas) {
        if (null != rightDatas) {
            int size = rightDatas.size();
            for (int i = 0; i < size; i++) {
                WordLinkBean item = rightDatas.get(i);
                if (null == item) {
                    continue;
                }
                if (0 == i) {
                    tvUpOne.setText(item.text);
                } else if (1 == i) {
                    tvUpTwo.setText(item.text);
                } else if (2 == i) {
                    tvUpThree.setText(item.text);
                } else if (3 == i) {
                    tvDownOne.setText(item.text);
                } else if (4 == i) {
                    tvDownTwo.setText(item.text);
                } else if (5 == i) {
                    tvDownThree.setText(item.text);
                } else {
                    break;
                }
            }
        }
    }

    public void setOnDetailRightItemClickListener(OnDetailRightItemClickListener listener) {
        this.listener = listener;
    }

    public static class DetailRightData {
        public int bigImageResId;
        public String bigText;
        public int bigTextResId;
        public List<WordLinkBean> rightDatas;
        public List<WordLinkBean> extraDatas;
        public String bigImageUrl;
        private static final int MAX_RIGHT_SIZE = 6;
        private static final int MAX_EXTRA_SIZE = 4;


        public DetailRightData(String bigImageUrl, String bigText, List<WordLinkBean> rightDatas, List<WordLinkBean>
                extraDatas) {
            this.bigImageUrl = bigImageUrl;
            this.bigText = bigText;
            this.rightDatas = rightDatas;
            this.extraDatas = extraDatas;
        }

        public DetailRightData(JSONObject jsonObject) {
            try {
                bigText = jsonObject.getString("category");
                bigImageUrl = jsonObject.getString("icon");
                JSONArray childrens = jsonObject.getJSONArray("children");
                int length = childrens.length();
                length = length > MAX_RIGHT_SIZE + MAX_EXTRA_SIZE ? MAX_RIGHT_SIZE + MAX_EXTRA_SIZE : length;
                int realRightSize = length > MAX_RIGHT_SIZE ? MAX_RIGHT_SIZE : length;
                int realExtraSize = length - realRightSize;
                rightDatas = new ArrayList<>(realRightSize);
                if (realExtraSize > 0) {
                    extraDatas = new ArrayList<>(realExtraSize);
                }
                for (int i = 0; i < length; i++) {
                    JSONObject itemJson = childrens.getJSONObject(i);
                    String name = itemJson.getString("name");
                    String landingpage = itemJson.getString("landingpage");
                    WordLinkBean wordLinkBean = new WordLinkBean(name, landingpage);
                    if (i < MAX_RIGHT_SIZE) {
                        rightDatas.add(wordLinkBean);
                    } else {
                        extraDatas.add(wordLinkBean);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public int getRightItemCount() {
        if (null == data) {
            return 0;
        }
        List<WordLinkBean> rightDatas = data.rightDatas;
        return null == rightDatas ? 0 : rightDatas.size();
    }

    public int getExtraItemCount() {
        if (null == data) {
            return 0;
        }
        List<WordLinkBean> extraDatas = data.extraDatas;
        return null == extraDatas ? 0 : extraDatas.size();
    }

    @Override
    public void onClick(View view) {
        if (null == listener) {
            return;
        }
//        switch (view.getId()) {
//            case R2.id.container_category:
//                listener.onBigItemClick(view);
//                toggleVisibleExtraViews();
//                break;
//            case R2.id.tv_up_one:
//                listener.onRightItemClick(containerRight, 0, getRightItemCount(), view);
//                break;
//            case R2.id.tv_up_two:
//                listener.onRightItemClick(containerRight, 1, getRightItemCount(), view);
//                break;
//            case R2.id.tv_up_three:
//                listener.onRightItemClick(containerRight, 2, getRightItemCount(), view);
//                break;
//            case R2.id.tv_down_one:
//                listener.onRightItemClick(containerRight, 3, getRightItemCount(), view);
//                break;
//            case R2.id.tv_down_two:
//                listener.onRightItemClick(containerRight, 4, getRightItemCount(), view);
//                break;
//            case R2.id.tv_down_three:
//                listener.onRightItemClick(containerRight, 5, getRightItemCount(), view);
//                break;
//            case R2.id.tv_extra_one:
//                listener.onExtraItemClick(containerExtra, 0, getExtraItemCount(), view);
//                break;
//            case R2.id.tv_extra_two:
//                listener.onExtraItemClick(containerExtra, 1, getExtraItemCount(), view);
//                break;
//            case R2.id.tv_extra_three:
//                listener.onExtraItemClick(containerExtra, 2, getExtraItemCount(), view);
//                break;
//            case R2.id.tv_extra_four:
//                listener.onExtraItemClick(containerExtra, 3, getExtraItemCount(), view);
//                break;
//        }

        int id = view.getId();
        if (id == R.id.container_category) {
            listener.onBigItemClick(view);
            toggleVisibleExtraViews();
        } else if (id == R.id.tv_up_one) {
            listener.onRightItemClick(containerRight, 0, getRightItemCount(), view);
        } else if (id == R.id.tv_up_two) {
            listener.onRightItemClick(containerRight, 1, getRightItemCount(), view);
        } else if (id == R.id.tv_up_three) {
            listener.onRightItemClick(containerRight, 2, getRightItemCount(), view);
        } else if (id == R.id.tv_down_one) {
            listener.onRightItemClick(containerRight, 3, getRightItemCount(), view);
        } else if (id == R.id.tv_down_two) {
            listener.onRightItemClick(containerRight, 4, getRightItemCount(), view);
        } else if (id == R.id.tv_down_three) {
            listener.onRightItemClick(containerRight, 5, getRightItemCount(), view);
        } else if (id == R.id.tv_extra_one) {
            listener.onExtraItemClick(containerExtra, 0, getExtraItemCount(), view);
        } else if (id == R.id.tv_extra_two) {
            listener.onExtraItemClick(containerExtra, 1, getExtraItemCount(), view);
        } else if (id == R.id.tv_extra_three) {
            listener.onExtraItemClick(containerExtra, 2, getExtraItemCount(), view);
        } else if (id == R.id.tv_extra_four) {
            listener.onExtraItemClick(containerExtra, 3, getExtraItemCount(), view);
        }
    }

    private void toggleVisibleExtraViews() {
        if (null == data.extraDatas) {
            return;
        }
        int visibility = containerExtra.getVisibility();
        if (visibility == GONE) {
            containerExtra.setVisibility(VISIBLE);
            lineExtra.setVisibility(VISIBLE);
        } else if (visibility == VISIBLE) {
            containerExtra.setVisibility(GONE);
            lineExtra.setVisibility(GONE);
        }
    }


    public interface OnDetailRightItemClickListener {
        void onBigItemClick(View view);

        void onRightItemClick(ViewGroup parent, int position, int itemCount, View view);

        void onExtraItemClick(ViewGroup parent, int position, int itemCount, View view);
    }

}
