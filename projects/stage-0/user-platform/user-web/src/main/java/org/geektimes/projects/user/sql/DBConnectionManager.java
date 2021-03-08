package org.geektimes.projects.user.sql;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DBConnectionManager {

    private Connection connection;

    public DBConnectionManager(boolean isJndi) {
        initConnection(isJndi);
    }

    private void initConnection(boolean isJndi) {
        try {
            Connection conn;
            if(!isJndi) {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                String databaseURL = "jdbc:derby:db/user-platform;create=true";
                conn = DriverManager.getConnection(databaseURL);
            } else {
                Context ctx = new InitialContext();
                DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/UserPlatformDB");
                conn = dataSource.getConnection();
            }
            // 判断users表是否存在
            // 如果users表存在,则什么都不做, 如果不存在,则创建
            confirmTable(conn, "USERS");
            setConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断表是否存在
     * @param conn
     * @throws Exception
     */
    private void confirmTable(Connection conn, String tableName) throws Exception{
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rsTables = metaData.getTables(null, null, null, new String[]{"TABLE"});
        HashSet<String> set=new HashSet<String>();
        while (rsTables.next()) {
            set.add(rsTables.getString("TABLE_NAME"));
        }
        if(!set.contains(tableName)) {
            Statement statement = conn.createStatement();
            statement.execute(CREATE_USERS_TABLE_DDL_SQL);
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void releaseConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    public static final String DROP_USERS_TABLE_DDL_SQL = "DROP TABLE users";

    public static final String CREATE_USERS_TABLE_DDL_SQL = "CREATE TABLE users(" +
            "id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
            "name VARCHAR(16) NOT NULL, " +
            "password VARCHAR(64) NOT NULL, " +
            "email VARCHAR(64) NOT NULL, " +
            "phoneNumber VARCHAR(64) NOT NULL" +
            ")";

    public static final String INSERT_USER_DML_SQL = "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
            "('A','******','a@gmail.com','1') , " +
            "('B','******','b@gmail.com','2') , " +
            "('C','******','c@gmail.com','3') , " +
            "('D','******','d@gmail.com','4') , " +
            "('E','******','e@gmail.com','5')";


    public static void main(String[] args) throws Exception {
//      调用setLogWriter时, 会通过 ClassLoader 加载 java.sql.DriverManager -> 先调用了静态代码块的代码
//      调用loadInitialDrivers时, 方法里的日志不会被打印出来,因为还没有执行setLogWriter
//        DriverManager.setLogWriter(new PrintWriter(System.out));
//        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
//        Driver driver = DriverManager.getDriver("jdbc:derby:/db/user-platform;create=true");
//        Connection connection = driver.connect("jdbc:derby:/db/user-platform;create=true", new Properties());
        String databaseURL = "jdbc:derby:db/user-platform;create=true";
        Connection conn = DriverManager.getConnection(databaseURL);
        Statement statement = conn.createStatement();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rsTables = metaData.getTables(null, null, null, new String[]{"TABLE"});
        HashSet<String> set=new HashSet<String>();
        while (rsTables.next()) {
            set.add(rsTables.getString("TABLE_NAME"));
        }
        System.out.println(set);
        // System.out.println(statement.execute(DROP_USERS_TABLE_DDL_SQL)); // 删除users表格 - false
        //System.out.println(statement.execute(CREATE_USERS_TABLE_DDL_SQL)); // 创建users表 - false
        //System.out.println(statement.executeUpdate(INSERT_USER_DML_SQL)); // 新增数据到users表 - 5

        // 执行查询语句
//        ResultSet resultSet = statement.executeQuery("SELECT id, name, password, email, phoneNumber FROM users");
//
//        BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
//        ResultSetMetaData metaData = resultSet.getMetaData(); // 获取表的元数据
//        System.out.println("当前表的列个数: " + metaData.getColumnCount());
//        System.out.println("当前表名: " + metaData.getTableName(1));
//        for (int i = 1; i <= metaData.getColumnCount(); i++) {
//            System.out.println("列名: " + metaData.getColumnLabel(i) + ", 列类型: " + metaData.getColumnType(i));
//        }
//        // ORM 映射核心
//        // 性能较慢, 因为要使用反射来生成执行代码
//        while (resultSet.next()) { // 如果存在并且游标滚动
//            User user = new User();
//
//            for (PropertyDescriptor descriptor : userBeanInfo.getPropertyDescriptors()) {
//                // getName返回实体类的字段名, getPropertyType返回实体类的类型
//                String fieldName = descriptor.getName(); // 字段名
//                Class fieldType = descriptor.getPropertyType(); // 字段类型
//                String methodName = typeMethodMappings.get(fieldType); // 根据字段类型获取resultSet的getter方法
//                // 字段名和实体类的映射关系(此处是相同的)
//                String columnLabel = mapColumnLabel(fieldName);
//                Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class); // 根据映射获取ResultSet的方法名
//                Object resultVal = resultSetMethod.invoke(resultSet, columnLabel); // 通过反射调用 getXXX方法
//                // 获取实体类的setter方法
//                Method setterMethodFromUser = descriptor.getWriteMethod();
//                setterMethodFromUser.invoke(user, resultVal); // 调用实体类的set方法, 参数是从resultSet中获取的
//            }
//
//            System.out.println(user);
//        }
        conn.close();
    }

    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> typeMethodMappings = new HashMap<>();

    static {
        typeMethodMappings.put(Long.class, "getLong");
        typeMethodMappings.put(String.class, "getString");
    }
}
