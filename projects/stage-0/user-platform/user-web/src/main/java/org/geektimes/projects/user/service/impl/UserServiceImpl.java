package org.geektimes.projects.user.service.impl;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.DatabaseUserRepository;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.sql.DBConnectionManager;
import org.geektimes.projects.user.sql.LocalTransactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/1 15:06
 */
public class UserServiceImpl implements UserService {


//    private DatabaseUserRepository databaseUserRepository;

//    public UserServiceImpl() {
//        DBConnectionManager dbConnectionManager = new DBConnectionManager(true); // 使用jndi
//        databaseUserRepository = new DatabaseUserRepository(dbConnectionManager);
//    }

    @Resource(name = "bean/EntityManager")
    private EntityManager entityManager;

    /**
     * 注册用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    //@Override
    //@LocalTransactional // 默认需要事务
    //public boolean register(User user) {
        // before persist
        //EntityTransaction transaction = entityManager.getTransaction();
        //transaction.begin();

        //实际调用
        //entityManager.persist(user);
        //其他方法调用
        // 如果调用update(user), register和update方法属于同一个线程
        // update方法也涉及事务(并且传播行为和隔离级别相同)
        // 则二者共享一个物理事务, 但存在两个逻辑事务
        // register方法属于outer事务, 对于update方法而言,属于inner事务

        // Case 1: 两个方法都涉及事务, 并且事务传播行为和隔离级别相同
        // outer和inner事务都属于逻辑事务, 他们其实属于同一个物理事务
        // 利用TreadLocal管理一个物理事务
        // rollback 情况:
        // update方法(inner事务), 无法主动调用rollback, 但是会设置rollback only状态, 说明update方法可能存在执行异常,
        // 或者触发了数据库约束 当outerTX接收到innerTX状态, 让outerTX来执行rollback
        // A -> B -> C -> D -> E 方法调用链, 可以认为A方法包含了B,C,D,E这些方法, 任何一部分代码
        // 都会出现rollback现象, A就是事务触发管理器,其他都是它的子过程
        // 核心要义: 物理事务是谁(哪个地方)创建的
        // 其他的调用链的事务传播行为一致时, 都是逻辑事务

        // Case 2: register方法是required(事务创建者), update方法的传播行为是PROPAGATION_REQUIRES_NEW
        // 这种情况 update方法也是事务创建者 update方法 rollback-only状态不会影响register方法.

        // Case 3: register传播行为: PROPAGATION_NESTED(事务创建者), update方法同样共享register方法的物理事务
        // 并且通过SavePoint来实现局部提交和回滚
        // after persist
        //transaction.commit();
        //return false;
    //}

    /**
     * 注册用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    @Override
    public boolean register(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(user);
        transaction.commit();
        return true;
    }

    /**
     * 注销用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    @Override
    public boolean deregister(User user) {
        return false;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return
     */
    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}
