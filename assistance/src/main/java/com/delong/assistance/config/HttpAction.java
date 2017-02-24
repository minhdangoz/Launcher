package com.delong.assistance.config;


import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by oeager on 2015/11/26.
 * email:oeager@foxmail.com
 */
public abstract class HttpAction<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public final void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            onHttpError(httpException.response());
        } else {
            onHttpError(null);
        }

    }

    public abstract void onHttpError(Response response);

    public abstract void onHttpSuccess(T t);

    @Override
    public  void onNext(T t) {
        if (t != null) {
            onHttpSuccess(t);
        }
    }
}
    