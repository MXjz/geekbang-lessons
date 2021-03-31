package org.geektimes.reactive.message.demo;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/30 11:14
 */
public interface DefaultService {

    @Outgoing("my-channel")
    Publisher<Integer> data();
}
