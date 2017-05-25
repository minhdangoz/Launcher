package com.kapp.kinflow.business.http.persistentcookiejar.persistence;

import java.util.Collection;
import java.util.List;

import okhttp3.Cookie;


public interface CookiePersistor {
    List<Cookie> loadAll();

    void saveAll(Collection<Cookie> var1);

    void removeAll(Collection<Cookie> var1);

    void clear();
}
