package com.android.launcher3;

import java.util.ArrayList;

/**
 * Created by LEI on 2015/6/17.
 * Multi-Select items
 */
public class LayoutInfo extends ItemInfo {
    /**
     * The apps and shortcuts
     */
    public ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();

    public LayoutInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_LAYOUT;
    }

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
        contents.add(item);
    }

    /**
     * Remove an app or shortcut.
     *
     * @param item
     */
    public void remove(ShortcutInfo item) {
        contents.remove(item);
    }

    public void removeAll() {
        contents.clear();
    }

    public int getCount() {
        return contents.size();
    }

    public ShortcutInfo getInfoAt(int index) {
        return contents.get(index);
    }
}
