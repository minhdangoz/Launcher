package com.lenovo.launcher.ext;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Hack android.os.SystemProperties
 */
public class SystemProperties {

    private static Method sGetMethod = null;
    private static Method sGetBooleanMethod = null;
    private static Method sSetMethod = null;
    
    static {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method methods[] = cls.getMethods();

            for (Method method : methods) {
                String name = method.getName();
                if (name.equals("get")) {
                    sGetMethod = method;
                } else if (name.equals("getBoolean")) {
                    sGetBooleanMethod = method;
                }
                else if (name.equals("set")) {
                    sSetMethod = method;
                }
            }
        } catch (Exception e) {
            Log.e("SystemProperties", "Failed to init.");
            e.printStackTrace();
        }
    }
    
    public static String get(String name, String def) {
        if (sGetMethod == null) {
            return def;
        }

        try {
            return (String) sGetMethod.invoke(null, name, def);
        } catch (Exception e) {
            Log.e("SystemProperties", "Failed to get.");
            e.printStackTrace();
            return def;
        }
    }
    
    public static boolean getBoolean(String name, boolean def) {
        if (sGetBooleanMethod == null) {
            return def;
        }

        try {
            return (Boolean) sGetBooleanMethod.invoke(null, name, def);
        } catch (Exception e) {
            Log.e("SystemProperties", "Failed to getBoolean.");
            e.printStackTrace();
            return def;
        }
    }
    
    public static void set(String name, String value) {
        if (sSetMethod == null) {
            return;
        }
        try {
            sSetMethod.invoke(null, name, value);
        } catch (Exception e) {
            Log.e("SystemProperties", "Failed to set.");
            e.printStackTrace();
        }
    }
}