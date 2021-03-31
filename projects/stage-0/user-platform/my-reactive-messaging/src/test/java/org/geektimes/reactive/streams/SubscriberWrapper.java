package org.geektimes.reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * 装饰器模式 + 组合模式 配合
 * @author xuejz
 * @description
 * @Time 2021/3/30 14:39
 */
public class SubscriberWrapper<T> implements Subscriber<T> {

    private final Subscriber subscriber;

    private final DefaultSubscription subscription;

    public SubscriberWrapper(Subscriber subscriber, DefaultSubscription subscription) {
        this.subscriber = subscriber;
        this.subscription = subscription;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public DefaultSubscription getSubscription() {
        return subscription;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        subscriber.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }
}
