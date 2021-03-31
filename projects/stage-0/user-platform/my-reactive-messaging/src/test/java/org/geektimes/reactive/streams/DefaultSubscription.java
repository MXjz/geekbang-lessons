package org.geektimes.reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 14:13
 */
public class DefaultSubscription implements Subscription {

    private boolean isCancel = false;

    private final Subscriber subscriber;

    public DefaultSubscription(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void request(long l) {

    }

    @Override
    public void cancel() {
        this.isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }
}
