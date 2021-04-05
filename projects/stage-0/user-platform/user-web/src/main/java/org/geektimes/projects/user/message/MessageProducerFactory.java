package org.geektimes.projects.user.message;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/29 14:58
 */
public class MessageProducerFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {

        /**
         * 获取Resource中注入的节点数据参考 BeanFactory
         * @see org.apache.naming.factory.BeanFactory
         */
        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;

        // 获取 queueName
        String queueName = getAttribute(ref, "queueName");
        // 获取 connectionFactoryJndiName
        String conenctionFactoryJndiName = getAttribute(ref, "connectionFactoryJndiName");
        ConnectionFactory connectionFactory = (ConnectionFactory) nameCtx.lookup(conenctionFactoryJndiName);

        Connection connection = connectionFactory.createConnection();
        connection.start();

        // create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // create the destination
        Destination destination = session.createQueue(queueName);

        // create a MessageProducer from the Session to the topic or queue
        return session.createProducer(destination);
    }

    private String getAttribute(Reference ref, String connectionFactoryJndiName) {
        RefAddr refAddr = ref.get(connectionFactoryJndiName);
        return refAddr == null ? null : String.valueOf(refAddr.getContent());
    }
}
