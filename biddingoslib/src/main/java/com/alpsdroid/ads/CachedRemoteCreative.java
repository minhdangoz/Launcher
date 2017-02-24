package com.alpsdroid.ads;

import java.util.Map;

/**
 * Created by Edward on 2015/11/25.
 */
public abstract class CachedRemoteCreative<T extends AppCreative> {
    
    public abstract Map<String, T> generateAppInfo(Map<String, String> appId_assets);
}
