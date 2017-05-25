package com.kapp.kinflow.business.http.persistentcookiejar.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import okhttp3.Cookie;

public class SharedPrefsCookiePersistor implements CookiePersistor {
    private SharedPreferences sharedPreferences;

    public SharedPrefsCookiePersistor(Context context) {
        String SHARED_PREFERENCES_NAME = "CookiePersistence";
        this.sharedPreferences = context.getSharedPreferences("CookiePersistence", 0);
    }

    public List<Cookie> loadAll() {
        ArrayList cookies = new ArrayList();
        Iterator i$ = this.sharedPreferences.getAll().entrySet().iterator();

        while(i$.hasNext()) {
            Entry entry = (Entry)i$.next();
            String serializedCookie = (String)entry.getValue();
            Cookie cookie = (new SerializableCookie()).decode(serializedCookie);
            cookies.add(cookie);
        }

        return cookies;
    }

    public void saveAll(Collection<Cookie> cookies) {
        Editor editor = this.sharedPreferences.edit();
        Iterator i$ = cookies.iterator();

        while(i$.hasNext()) {
            Cookie cookie = (Cookie)i$.next();
            if(cookie.persistent()) {
                editor.putString(createCookieKey(cookie), (new SerializableCookie()).encode(cookie));
            }
        }

        editor.apply();
    }

    public void removeAll(Collection<Cookie> cookies) {
        Editor editor = this.sharedPreferences.edit();
        Iterator i$ = cookies.iterator();

        while(i$.hasNext()) {
            Cookie cookie = (Cookie)i$.next();
            editor.remove(createCookieKey(cookie));
        }

        editor.apply();
    }

    private static String createCookieKey(Cookie cookie) {
        return (cookie.secure()?"https":"http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    public void clear() {
        this.sharedPreferences.edit().clear().apply();
    }
}
