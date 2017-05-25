package com.kapp.kinflow.view.recyclerview.model;


import com.kapp.kinflow.view.recyclerview.IItemType;

/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public  abstract class BaseItemBean implements IItemType {
    protected int type = IItemType.TYPE_NORMAL_MIN;
    @Override
    public int getItemType() {
        return type;
    }

    public void setItemType(int type) {
        if (!isLegal(type)) {
            new IllegalArgumentException(" setItemType(type) type非法参数异常！！！");
        }
        this.type = type;
    }

    private boolean isLegal(int type) {
        if (type < IItemType.TYPE_NORMAL_MIN || type > IItemType.TYPE_NORMAL_MAX) {
            return false;
        }
        return true;
    }
}
