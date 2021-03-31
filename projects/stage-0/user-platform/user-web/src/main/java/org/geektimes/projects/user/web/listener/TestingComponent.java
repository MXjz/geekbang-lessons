/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geektimes.projects.user.web.listener;

import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
@Deprecated
public class TestingComponent {

    @Resource(name = "jms/activemq-topic")
    private Topic topic;

    @Resource(name = "jms/message-producer")
    private MessageProducer messageProducer;

    @PostConstruct
    public void init() {
        System.out.println(topic);
    }

    @PostConstruct
    public void testMessageProducer() throws Throwable {
        String message = "Hello xuejz";
        TextMessage textMessage = new ActiveMQTextMessage();
        textMessage.setText(message);
        messageProducer.send(textMessage);
    }
}
