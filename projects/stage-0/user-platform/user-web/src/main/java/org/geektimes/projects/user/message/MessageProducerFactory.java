package org.geektimes.projects.user.message;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/29 14:58
 */
public class MessageProducerFactory implements ObjectFactory {

    private String queueName; // 队列名称

    private String connectionFactoryJndiName;

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {

        ConnectionFactory connectionFactory = (ConnectionFactory) nameCtx.lookup("activemq-factory");

        Connection connection = connectionFactory.createConnection();
        connection.start();

        // create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // create the destination
        Destination destination = session.createQueue("TEST.FOO");

        // create a MessageProducer from the Session to the topic or queue
        return session.createProducer(destination);
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setConnectionFactoryJndiName(String connectionFactoryJndiName) {
        this.connectionFactoryJndiName = connectionFactoryJndiName;
    }
}
