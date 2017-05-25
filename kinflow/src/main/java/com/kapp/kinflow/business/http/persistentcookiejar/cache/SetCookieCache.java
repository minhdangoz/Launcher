package com.kapp.kinflow.business.http.persistentcookiejar.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import okhttp3.Cookie;


public class SetCookieCache implements CookieCache {
    private Set<IdentifiableCookie> cookies = new HashSet();

    public SetCookieCache() {
    }

    public void addAll(Collection<Cookie> newCookies) {
        this.updateCookies(IdentifiableCookie.decorateAll(newCookies));
    }

    private void updateCookies(Collection<IdentifiableCookie> cookies) {
        this.cookies.removeAll(cookies);
        this.cookies.addAll(cookies);
    }

    public void clear() {
        this.cookies.clear();
    }

    public Iterator<Cookie> iterator() {
        return new SetCookieCacheIterator();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {
        private Iterator<IdentifiableCookie> iterator;

        public SetCookieCacheIterator() {
            this.iterator = SetCookieCache.this.cookies.iterator();
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public Cookie next() {
            return ((IdentifiableCookie)this.iterator.next()).getCookie();
        }

        public void remove() {
            this.iterator.remove();
        }
    }
}
