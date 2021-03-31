package org.geektimes.reactive.streams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.logging.Logger;

/**
 * 装饰器模式
 * @author xuejz
 * @description
 * @Time 2021/3/30 16:56
 */
public class DecoratingSubscriber<T> implements Subscriber<T> {

    private final Subscriber<T> source;

    private final Logger logger;

    private long maxRequest = -1;

    private boolean completed = false;

    private boolean canceled = false;

    private int requestCount = 0;

    public DecoratingSubscriber(Subscriber<T> source) {
        this.source = source;
        this.logger = Logger.getLogger(source.getClass().getName());
    }

    @Override
    public void onSubscribe(Subscription s) {
        source.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        assertRequest(); // 校验maxRequest

        if (isCompleted()) {
            logger.severe("The data subscription was completed, This method should not be invoked again!");
            return;
        }

        if (isCanceled()) {
            logger.warning(String.format("The Subscriber has canceled the data subscription," +
                    " current data[%s] will be ignored!", t));
            return;
        }

        if (requestCount == maxRequest && maxRequest < Long.MAX_VALUE) {
            onComplete();
            logger.warning(String.format("The number of requests is up to the max threshold[%d]," +
                    " the data subscription is completed!", maxRequest));
            return;
        }

        next(t);
    }

    private void assertRequest() {
        if (maxRequest < 1) {
            throw new IllegalArgumentException("the number of request must be initialized before " +
                    "Subscriber#onNext(Object) method, please set the positive number to " +
                    "Subscription#request(int) method on Publisher#subscribe(Subscriber) phase.");
        }
    }

    private void next(T t) {
        try {
            source.onNext(t);
        } catch (Throwable throwable) {
            onError(throwable);
        } finally {
            requestCount++;
        }
    }

    public void setMaxRequest(long maxRequest) {
        this.maxRequest = maxRequest;
    }

    public Subscriber<T> getSource() {
        return source;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void onError(Throwable t) {
        source.onError(t);
    }

    @Override
    public void onComplete() {
        source.onComplete();
        this.completed = true;
    }

    public void cancel() {
        canceled = true;
    }
}
