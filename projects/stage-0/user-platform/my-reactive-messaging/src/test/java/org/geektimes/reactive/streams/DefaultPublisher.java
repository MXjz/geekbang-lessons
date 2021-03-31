package org.geektimes.reactive.streams;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.LinkedList;
import java.util.List;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 14:01
 */
public class DefaultPublisher<T> implements Publisher<T> {

    private List<SubscriberWrapper> subscribers = new LinkedList<>();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        DefaultSubscription subscription = new DefaultSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        subscribers.add(new SubscriberWrapper(subscriber, subscription));
    }

    public void publish(T data) {
        // 广播
        subscribers.forEach(subscriberWrapper -> {
            // 判断当前订阅者是否需要停止发送消息
            DefaultSubscription subscription = subscriberWrapper.getSubscription();
            if (subscription.isCancel()) {
                System.err.println("本次数据发布忽略: " + data);
                return;
            }
            subscriberWrapper.onNext(data);
        });
    }

    public void error(Throwable t) {
        // 广播
        subscribers.forEach(subscriberWrapper -> {
            subscriberWrapper.getSubscriber().onError(t);
        });
    }

    public void complete() {
        // 广播
        subscribers.forEach(subscriberWrapper -> {
            subscriberWrapper.getSubscriber().onComplete();
        });
    }

    public static void main(String[] args) {
        DefaultPublisher publisher = new DefaultPublisher();
        publisher.subscribe(new DefaultSubscriber());
        for (int i = 0; i < 5; i++) {
            publisher.publish(i);
        }
        publisher.complete();
    }
}
