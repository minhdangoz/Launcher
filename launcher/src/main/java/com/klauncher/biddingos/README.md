biddingos目录为品效通合作方提供的源码,结合biddingoslibzhe这个module使用.
biddingos目录下的distribute子目录为我司结合klauncher项目定义的有关app分发内容.
<pre>
<code>
            new AppInfoDataManager(this,0,"5").requestAppInfoList(new AppInfoDataManager.AppInfoCallback() {
                @Override
                public void onSuccess(List<AppInfo> appList) {
                    if (null==appList||appList.size()==0) {
                        Toast.makeText(KLauncher.this, "获取AppInfoList失败", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(KLauncher.this, "获取AppInfoList成功", Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < appList.size(); i++) {
                            Log.e("Kinflow", "onSuccess: " + appList.get(i).toString());
                        }
                    }
                }

                @Override
                public void onFail() {
                    Toast.makeText(KLauncher.this, "获取AppInfoList失败", Toast.LENGTH_SHORT).show();
                }
            });
</code>
</pre></br>

读取缓存AppInfo的方法
List<AppInfo> appInfoList =  AdHelper.getCacheAppInfo(listId);

关于app下载
在AppInfo中,直接调用:downloadAPK()方法即可.

关于上报:
<pre>
<code>
//展现--->上报
 new AdHelplerImpl().notifyImpression(AppInfoDataManager.CURRENT_AD_PLACEMENT_MODULE,appInfo.getApp_id());
//点击--->上报
 new AdHelplerImpl().notifyClick(AppInfoDataManager.CURRENT_AD_PLACEMENT_MODULE, appInfo.getApp_id())
 //下载完成后--->上报
 这个不用管,已经集成到下载功能了.
<code/>
</pre>
