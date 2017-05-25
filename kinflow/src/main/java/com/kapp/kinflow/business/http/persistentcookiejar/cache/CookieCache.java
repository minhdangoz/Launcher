package com.kapp.kinflow.business.http.persistentcookiejar.cache;

import java.util.Collection;

import okhttp3.Cookie;


public interface CookieCache extends Iterable<Cookie> {
    void addAll(Collection<Cookie> var1);

    void clear();
}

