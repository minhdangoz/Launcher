/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.ContentValues;
import android.content.Context;

import com.android.launcher3.compat.UserHandleCompat;
import com.klauncher.biddingos.distribute.data.AppInfoDataManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {
    public String folderId;

    /**
     * 设置文件夹id 请求广告
     */
    public void setFolderId() {
        if ("系统工具".equals(title.toString().trim())) {
            folderId = AppInfoDataManager.AD_PLACEMENT_MODULE_XI_TONG_GONG_JU;
        } else if ("便捷生活".equals(title.toString().trim())) {
            folderId = AppInfoDataManager.AD_PLACEMENT_MODULE_BIAN_JIE_SHENG_HUO;
        } else if ("新闻阅读".equals(title.toString().trim())) {
            folderId = AppInfoDataManager.AD_PLACEMENT_MODULE_XIN_WEN_YUE_DU;
        } else if ("影音娱乐".equals(title.toString().trim())) {
            folderId = AppInfoDataManager.AD_PLACEMENT_MODULE_YING_YIN_YU_LE;
        } else {
            folderId = AppInfoDataManager.AD_PLACEMENT_MODULE_MO_REN;
        }
    }

    /**
     * Whether this folder has been opened
     */
    boolean opened;

    /**
     * The apps and shortcuts and hidden status
     */
    ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();
    Boolean hidden = false;

    ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
        user = UserHandleCompat.myUserHandle();
    }

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
        contents.add(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAdd(item);
        }
        itemsChanged();
    }

    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     */
    public void remove(ShortcutInfo item) {
        contents.remove(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onRemove(item);
        }
        itemsChanged();
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onTitleChanged(title);
        }
    }

    @Override
    void onAddToDatabase(Context context, ContentValues values) {
        super.onAddToDatabase(context, values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
        values.put(LauncherSettings.Favorites.HIDDEN, hidden ? 1 : 0);
    }

    void addListener(FolderListener listener) {
        listeners.add(listener);
    }

    void removeListener(FolderListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    void itemsChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged();
        }
    }

    @Override
    void unbind() {
        super.unbind();
        listeners.clear();
    }

    interface FolderListener {
        public void onAdd(ShortcutInfo item);
        public void onRemove(ShortcutInfo item);
        public void onTitleChanged(CharSequence title);
        public void onItemsChanged();
        public void onRemoveAll();
    }

    /**
     * 记录这个文件夹前一次的位置是在Workspace还是
     * Hotseat上，如果前后位置不一致，则强制重新计算
     * 当前文件夹的Preview的参数
     */
    long previousContainer = -901;
    
    @Override
    public String toString() {
        return "FolderInfo(id=" + this.id + " type=" + this.itemType
                + " container=" + this.container + " screen=" + screenId
                + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
                + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos) + ")";
    }

    // Lenovo-sw:yuanyl2, Add edit mode function. Begin.
    public FolderInfo copy() {
        FolderInfo copy = new FolderInfo();
        copy.id = this.id;
        copy.cellX = this.cellX;
        copy.cellY = this.cellY;
        copy.spanX = this.spanX;
        copy.spanY = this.spanY;
        copy.screenId = this.screenId;
        copy.itemType = this.itemType;
        copy.container = this.container;
        copy.title = this.title;
        copy.contents = new ArrayList<>(contents);
        copy.listeners = new ArrayList<>(listeners);
        return copy;
    }

    public int getCount(){
        return contents.size();
    }

    public void removeAll(){
        contents.clear();
        for (int j = 0; j < listeners.size(); j++) {
            listeners.get(j).onRemoveAll();
        }
    }
    // Lenovo-sw:yuanyl2, Add edit mode function. End.
}
