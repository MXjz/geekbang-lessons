package org.geektimes.projects.user.service.impl;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.DatabaseUserRepository;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.projects.user.sql.DBConnectionManager;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/1 15:06
 */
public class UserServiceImpl implements UserService {


    private DatabaseUserRepository databaseUserRepository;

    public UserServiceImpl() {
        DBConnectionManager dbConnectionManager = new DBConnectionManager(true); // 使用jndi
        databaseUserRepository = new DatabaseUserRepository(dbConnectionManager);
    }

    /**
     * 注册用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    @Override
    public boolean register(User user) {
        return databaseUserRepository.save(user);
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
        return databaseUserRepository.getById(id);
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return databaseUserRepository.getByNameAndPassword(name, password);
    }
}
