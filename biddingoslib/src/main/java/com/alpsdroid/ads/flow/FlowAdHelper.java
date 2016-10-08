package com.alpsdroid.ads.flow;

import android.content.Context;


/**
 * Created by Edward on 2015/11/26.
 */
public interface FlowAdHelper {

    public void init(Context context, String mid, String afid, String secret, String kid, boolean debuggable);
    
    public FlowAdInfo getDownloadInfo(String listId, int position, String packageName, int versionCode);
    
    public void notifyImpression(String listId, int position, String packageName);
    
    public void notifyClick(String listId, int position, String packageName);
    
    public void notifyConversion(String packageName);
    
	public void destroy();
    
}
