package com.kapp.kinflow.business.http.persistentcookiejar;


import okhttp3.CookieJar;

public interface ClearableCookieJar extends CookieJar {
    void clear();
}

