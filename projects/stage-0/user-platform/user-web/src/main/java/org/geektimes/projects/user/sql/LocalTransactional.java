package org.geektimes.projects.user.sql;

import java.lang.annotation.*;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

/**
 * 本地事务
 *
 * @author xuejz
 * @description
 * @Time 2021/3/8 22:27
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalTransactional {

    int PROPAGATION_REQUIRED = 0;

    int PROPAGATION_REQUIRES_NEW = 3;

    int PROPAGATION_NESTED = 6;

    /**
     * 传播级别
     *
     * @return
     */
    int propagation() default PROPAGATION_REQUIRED;

    /**
     * 事务隔离级别
     * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
     * @return
     */
    int isolation() default TRANSACTION_READ_COMMITTED;
}
