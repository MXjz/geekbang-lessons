package org.geektimes.reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 14:03
 */
public class DefaultSubscriber<T> implements Subscriber<T> {

    private Subscription subscription;

    private int count = 0;
    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(T t) {
//        count++;
        if(++count > 2) {
            this.subscription.cancel();
        }
        System.out.println("收到消息: " + t);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("遇到异常");
    }

    @Override
    public void onComplete() {
        System.out.println("接收完成");
    }
}
