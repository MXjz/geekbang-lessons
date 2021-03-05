# geekbang-lessons
极客时间课程工程

## 重构数据库连接的逻辑

**在项目初始化时创建好数据库连接，并在需要时直接获取已有的数据库连接即可，而不是每次操作数据库时再去加载数据库连接**

那么应该如何在项目中操作数据库时无需加载数据库连接而直接获取？我们可以利用Tomcat容器来帮我们管理数据连接

Tomcat7文档关于jndi的描述：https://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html

> JNDI: Java Naming and Directory Interface（Java名称与字典接口）
>
> ENV: java:comp/env - （第一层）
>
> ​								   /bean
>
> ​											 /DBConnectionManager
>
> = java:comp/env/bean/DBConnectionManager
>
> ​								  /jdbc
>
> ​											/UserPlatformDB
>
> = java:comp/env/jdbc/UserPlatformDB
>
> Tomcat includes a series of standard resource factories that can provide services to your web applications, but give you configuration flexibility (via the [`<Context>`](https://tomcat.apache.org/tomcat-7.0-doc/config/context.html) element) without modifying the web application or the deployment descriptor. Each subsection below details the configuration and usage of the standard resource factories.
>
> Tomcat包括一系列标准资源工厂，它们可以为您的web应用程序提供服务，但是可以在不修改web应用程序或部署描述符的情况下灵活配置

让Tomcat来管理数据库连接管理类`DBConnectionManager`步骤

1. 在`WEB-INF/web.xml`增加对`DBConnectionManager`对象的声明

   ```xml
   <resource-env-ref>
     <description>
       Object factory for DBConnectionManager instances.
     </description>
     <resource-env-ref-name>
       bean/DBConnectionManager
     </resource-env-ref-name>
     <resource-env-ref-type>
       org.geektimes.projects.user.sql.DBConnectionManager
     </resource-env-ref-type>
   </resource-env-ref>
   ```

2. 配置Tomcat资源工厂

   ```xml
   <!-- FactoryBean -->
   <Resource name="bean/DBConnectionManager" auth="Container"
   			  	type="org.geektimes.projects.user.sql.DBConnectionManager"
   			  	factory="org.apache.naming.factory.BeanFactory" />
   ```

3. 在`java:comp/env`上下文中寻找`bean/DBConnectionManager`

   ```java
   Context initCtx = new InitialContext();
   Context envCtx = (Context) initCtx.lookup("java:comp/env");
   DBConnectionManager dbConnectionManager = (DBConnectionManager) envCtx.lookup("bean/DBConnectionManager");
   ```

【注】`<Resource>`节点的name属性：`bean/DBConnectionManager`必须和web.xml的`<resource-env-ref-name>`保持一致

### 对上述第三步做一些修改

#### 目的

我认为是职责划分，对java代码的操作进行分层，每一层做自己的事情

#### 步骤

##### 一、定义ComponentContext类

在容器启动时，初始化ComponentContext类，目的在于让ComponentContext类作为应用全局可用的（实现依赖注入），方式是利用`ServletContext`的`setAttribute`和`getAttribute`

```java
public class ComponentContext {

    public static final String CONTEXT_NAME = ComponentContext.class.getName();

    private static ServletContext servletContext; // 请注意
  
    /**
     * 获取 ComponentContext
     *
     * @return
     */
    public static ComponentContext getInstance() {
        return (ComponentContext) servletContext.getAttribute(CONTEXT_NAME);
    }

    private Context context;

    /**
     * 初始化上下文
     *
     * @throws RuntimeException
     */
    public void init(ServletContext servletContext) throws RuntimeException {
        try {
            this.context = (Context) new InitialContext().lookup("java:comp/env");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        servletContext.setAttribute(CONTEXT_NAME, this);
        ComponentContext.servletContext = servletContext;
        servletContext.log("初始化 java:comp/env 上下文成功!");
    }

    public void destroy() throws RuntimeException {
        if (this.context != null) {
            try {
                this.context.close();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 通过名称进行依赖查找
     *
     * @param name
     * @param <C>
     * @return
     */
    public <C> C getComponent(String name) {
        C component;
        try {
            component = (C) context.lookup(name);
        } catch (NamingException e) {
            throw new NoSuchElementException(name);
        }
        return component;
    }
}
```

