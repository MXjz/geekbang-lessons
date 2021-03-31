package org.geektimes.reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 17:24
 */
public class SubscriptionAdapter implements Subscription {

    private final DecoratingSubscriber subscriber;

    public SubscriptionAdapter(Subscriber subscriber) {
        this.subscriber = new DecoratingSubscriber(subscriber);
    }

    @Override
    public void request(long n) {
        if(n < 1) {
            throw new IllegalArgumentException("The number of elements to requests must be more than zero!");
        }
        this.subscriber.setMaxRequest(n);
    }

    @Override
    public void cancel() {
        this.subscriber.cancel();
    }

    public DecoratingSubscriber getSubscriber() {
        return subscriber;
    }

    public Subscriber getSourceSubscriber() {
        return subscriber.getSource();
    }
}
