# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\Eclipse\adt\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-useuniqueclassmembernames
-dontwarn com.squareup.picasso.OkHttpDownloader

#DELONG SDK
-keep class com.android.system.**{*;}
-keep class com.x91tec.statisticalanalysis.**{*;}
# -- end --

#Umeng SDK
-ignorewarnings
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.qq.e.*** {
    <fields>;
    <methods>;
}
-keep class com.baidu.** {public protected *;}
-keep class com.android.alsapkew.**{ *;}

###########adview混淆
-keep class com.kyview.** {
    *;
}
-keep class com.kuaiyou.** {
    *;
}

# 个推混淆
-dontwarn com.igexin.**
-keep class com.igexin.**{*;}
-keep class com.klauncher.getui.**{*;}

#广告SDK混淆
-keepattributes *Annotation*
-dontwarn com.klauncher.cplauncher.vajcvw.**
-keep public class com.klauncher.cplauncher.vajcvw.**{ *; }
-keep public class com.klauncher.cplauncher.vajcvw.**{ *; }
-keep public class com.klauncher.cplauncher.vxny.**{*;}
