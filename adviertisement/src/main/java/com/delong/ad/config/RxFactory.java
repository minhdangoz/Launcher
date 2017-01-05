package com.delong.ad.config;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Bison.Wensent on 16/8/12.
 */
public final class RxFactory {

    static class RxTransformer<T> implements Observable.Transformer<T,T>{

        @Override
        public Observable<T> call(Observable<T> tObservable) {
            return tObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    final static Observable.Transformer SCHEDULE_TRANSFORMER = new RxTransformer();

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> callerSchedulers() {
        return SCHEDULE_TRANSFORMER;
    }
}
