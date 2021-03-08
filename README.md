# geekbang-lessons
极客时间课程工程
## 第一次作业

### 运行

进入项目目录user-platform，输入命令即可运行

```
$ mvn clean package -U
$ java -jar user-web/target/user-web-v1-SNAPSHOT-war-exec.jar 
```

访问网址：http://localhost:8080/

1. 点击注册按钮

   ![login](/Users/jzxue/IdeaProjects/mercyblitz-projects-in-github/geekbang-lessons/imgs/login.png)

2. 输入用户信息，点击注册

   ![](/Users/jzxue/IdeaProjects/mercyblitz-projects-in-github/geekbang-lessons/imgs/register.png)

### 遇到的问题

1. 使用嵌入式derby数据库，在DBConnectionManager类中的main方法执行以下代码没有问题，但是在提供给DatabaseUserRepository使用时初始化数据库连接，conn为null。（暂时解决，还需要研究一下）

   ```java
   String databaseURL = "jdbc:derby:db/user-platform;create=true";
   Connection conn = DriverManager.getConnection(databaseURL);
   ```

   调试时发现DriverManager的静态代码块中的loadInitDrivers()方法并没有执行，所以`registeredDrivers`为空，最后conn为null

   ```java
   static {
     loadInitialDrivers();
     println("JDBC DriverManager initialized");
   }
   ```

   最后加了代码：`Class.forName("org.apache.derby.jdbc.EmbeddedDrive");`才暂时解决。

   ```java
   Class.forName("org.apache.derby.jdbc.EmbeddedDrive");
   String databaseURL = "jdbc:derby:db/user-platform;create=true";
   Connection conn = DriverManager.getConnection(databaseURL);
   ```

2. 创建数据库连接供dao层操作时需要判断users表是否存在，如果不存在还要创建表，如果不创建会报错

   ```java
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
   ```

3. 如何在外置Tomcat配置jndi数据源？

   * 在Tomcat的`conf/context.xml`中配置数据源信息

     在`conf/server.xml`的`GlobalNamingResources`节点中配置好像不管用啊 = =，原因未知（需要研究）

   ```xml
   <Resource name="jdbc/UserPlatformDB"
             type="javax.sql.DataSource"  auth="Container"
             description="Derby database for User Platform"
             maxActive="100" maxIdle="30" maxWait="10000"
             username="" password="" 
             driverClassName="org.apache.derby.jdbc.EmbeddedDriver"
             url="jdbc:derby:db/user-platform"/>
   ```

   * 在项目中的web.xml配置如下信息**（注意：\<res-ref-name>节点定义的名字要和tomcat中Resource节点定义的名字相同）**

   ```xml
   <resource-ref>
     <description>DB Connection</description>
     <res-ref-name>jdbc/UserPlatformDB</res-ref-name>
     <res-type>javax.sql.DataSource</res-type>
     <res-auth>Container</res-auth>
   </resource-ref>
   ```

   * 加载数据源

   ```java
   Context ctx = new InitialContext();
   DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/UserPlatformDB");
   conn = dataSource.getConnection();
   ```

4. 如何在内嵌Tomcat中配置jndi数据源？

   * 在`webapp/META-INF`创建`context.xml`，内容即上述resource节点的内容。**注意：`context.xml`不能再项目中的`resource/META-INF`，而是要放在`webapp/META-INF`（原因？）**
   * 在内嵌Tomcat的配置中添加`<enableNaming>true</enableNaming>
