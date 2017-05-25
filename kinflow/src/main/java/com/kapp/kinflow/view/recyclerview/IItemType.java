package com.kapp.kinflow.view.recyclerview;

/**
 * description：ItemType
 * <br>author：caowugao
 * <br>time： 2017/04/1 12:04
 */

public interface IItemType {
    int getItemType();

    int TYPE_HEADER_MIN = 1;
    int TYPE_HEADER_MAX = 111;
    int TYPE_FOOTER_MIN = 900;
    int TYPE_FOOTER_MAX = 999;
    int TYPE_NORMAL_MIN = 222;
    int TYPE_NORMAL_MAX = 899;

    int TYPE_ILLEGAL=-110;
}
