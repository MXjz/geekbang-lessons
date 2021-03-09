package org.geektimes.projects.user.repository;

import org.geektimes.function.ThrowableFunction;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

public class DatabaseUserRepository implements UserRepository {

    private static Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());

    /**
     * 通用处理方式
     */
    private static Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

    private static final String INSERT_USER_DML_SQL =
            "INSERT INTO USERS(name,password,email,phoneNumber) VALUES " +
                    "(?,?,?,?)";

    private static final String QUERY_ALL_USERS_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users";

    private final DBConnectionManager dbConnectionManager;

    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    private Connection getConnection() {
        return dbConnectionManager.getConnection();
    }

    private void releaseConnection() {
        dbConnectionManager.releaseConnection();
    }
    /**
     * 用户信息保存
     *
     * @param user
     * @return
     */
    @Override
    public boolean save(User user) {
        Connection conn = dbConnectionManager.getConnection();
        try {
            // 查询人员是否存在, 如果存在则不添加
            User userInTable = getByNameAndPassword(user.getName(), user.getPassword());
            if(userInTable.getId() != null) return false;
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_USER_DML_SQL);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getPhoneNumber());
            int row = preparedStatement.executeUpdate();
            return row == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null) {
                releaseConnection();
            }
        }
        return false;
    }

    /**
     * 根据id删除用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public boolean update(User user) {
        return false;
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public User getById(Long userId) {
        return executeQuery("SELECT id, name, password, email, phoneNumber FROM users WHERE id = ?",
                resultSet -> {
                    BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
                    User user = new User();
                    if (resultSet.next()) {
                        beanInfoHandler(userBeanInfo, resultSet, user); // 处理resultSet中的数据 -> User实体类
                    }
                    return user;
                }, COMMON_EXCEPTION_HANDLER, userId);
    }

    /**
     * 根据名称和密码查询用户信息
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery("SELECT id, name, password, email, phoneNumber FROM users WHERE name = ? AND password = ?",
                resultSet -> {
                    BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
                    User user = new User();
                    if (resultSet.next()) {
                        beanInfoHandler(userBeanInfo, resultSet, user);
                    }
                    return user;
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }

    /**
     * 全表查询
     *
     * @return
     */
    @Override
    public Collection<User> getAll() {
        return executeQuery(QUERY_ALL_USERS_DML_SQL, resultSet -> {
            BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
            List<User> users = new ArrayList<>();
            while (resultSet.next()) { // 如果存在并且游标滚动
                User user = new User();
                beanInfoHandler(userBeanInfo, resultSet, user); // 处理resultSet中的数据 -> User实体类
                users.add(user);
            }
            return users;
        }, COMMON_EXCEPTION_HANDLER);
    }

    private void beanInfoHandler(BeanInfo userBeanInfo, ResultSet resultSet, User user) throws Exception {
        for (PropertyDescriptor descriptor : userBeanInfo.getPropertyDescriptors()) {
            // getName返回实体类的字段名, getPropertyType返回实体类的类型
            String fieldName = descriptor.getName(); // 字段名
            Class fieldType = descriptor.getPropertyType(); // 字段类型
            String methodName = resultSetMethodMappings.get(fieldType); // 根据字段类型获取resultSet的getter方法
            // 字段名和实体类的映射关系(此处是相同的)
            String columnLabel = mapColumnLabel(fieldName);
            Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class); // 根据映射获取ResultSet的方法名
            Object resultVal = resultSetMethod.invoke(resultSet, columnLabel); // 通过反射调用 getXXX方法
            // 获取实体类的setter方法
            Method setterMethodFromUser = descriptor.getWriteMethod();
            setterMethodFromUser.invoke(user, resultVal); // 调用实体类的set方法, 参数是从resultSet中获取的
        }
    }

    /**
     * 执行查询方法
     *
     * @param sql
     * @param resultSetHandler 结果集处理方法 resultSet -> T
     * @param exceptionHandler 异常处理方法(可以用Consumer)
     * @param <T>
     * @return
     */
    protected <T> T executeQuery(String sql, ThrowableFunction<ResultSet, T> resultSetHandler, Consumer<Throwable> exceptionHandler, Object... args) {
        // 获取数据库连接
        Connection conn = getConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i]; // 获取参数值
                Class argType = arg.getClass(); // 获取参数类型
                // 如果argType不是基本数据类型, 需要做转换( 包装类型 -> primitive type )
                // 以方便preparedStatement.getClass().getMethod能获取到正确的setter方法
                Class wrapperType = wrapperToPrimitive(argType);
                if (wrapperType == null) {
                    // 如果是包装类型的情况, 就用原来的argType就行了
                    wrapperType = argType;
                }
                // 获取preparedStatement参数的setter方法
                Method preparedStatementMethod = preparedStatement.getClass().getMethod(preparedStatementMethodMappings.get(argType), int.class, wrapperType);
                preparedStatementMethod.invoke(preparedStatement, i + 1, arg); // 执行setter方法
                // preparedStatement.setObject(i + 1, args[i]); // 设置查询参数
            }
            ResultSet resultSet = preparedStatement.executeQuery(); // 获取结果集
            // 返回ResultSet -> user实体类, 在executQuery中不方便处理, 应当交给resultSetHandler来处理
            return resultSetHandler.apply(resultSet);
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return null;
    }

    /**
     * 执行更新,删除,插入方法
     *
     * @param sql
     * @param exceptionHandler
     * @param args
     * @return
     */
    protected boolean execute(String sql, Consumer<Throwable> exceptionHandler, Object... args) {
        return false;
    }


    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> resultSetMethodMappings = new HashMap<>();

    static Map<Class, String> preparedStatementMethodMappings = new HashMap<>();

    static {
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        preparedStatementMethodMappings.put(Long.class, "setLong"); // long
        preparedStatementMethodMappings.put(String.class, "setString"); //


    }
}
