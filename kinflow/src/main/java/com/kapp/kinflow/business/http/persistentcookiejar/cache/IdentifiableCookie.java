package com.kapp.kinflow.business.http.persistentcookiejar.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;


public class IdentifiableCookie {
    private Cookie cookie;

    static List<IdentifiableCookie> decorateAll(Collection<Cookie> cookies) {
        ArrayList identifiableCookies = new ArrayList(cookies.size());
        Iterator i$ = cookies.iterator();

        while(i$.hasNext()) {
            Cookie cookie = (Cookie)i$.next();
            identifiableCookies.add(new IdentifiableCookie(cookie));
        }

        return identifiableCookies;
    }

    IdentifiableCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    Cookie getCookie() {
        return this.cookie;
    }

    public boolean equals(Object other) {
        if(!(other instanceof IdentifiableCookie)) {
            return false;
        } else {
            IdentifiableCookie that = (IdentifiableCookie)other;
            return that.cookie.name().equals(this.cookie.name()) && that.cookie.domain().equals(this.cookie.domain()) && that.cookie.path().equals(this.cookie.path()) && that.cookie.secure() == this.cookie.secure() && that.cookie.hostOnly() == this.cookie.hostOnly();
        }
    }

    public int hashCode() {
        byte hash = 17;
        int hash1 = 31 * hash + this.cookie.name().hashCode();
        hash1 = 31 * hash1 + this.cookie.domain().hashCode();
        hash1 = 31 * hash1 + this.cookie.path().hashCode();
        hash1 = 31 * hash1 + (this.cookie.secure()?0:1);
        hash1 = 31 * hash1 + (this.cookie.hostOnly()?0:1);
        return hash1;
    }
}
