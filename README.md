# geekbang-lessons
极客时间课程工程

## 配置管理

### 设计技巧

- 优先读取**外部配置**（存在多来源，可能存在优先级）；将**内部配置**作为默认（兜底）配置
- 当存在多源外部配置时，源作用范围越小，通常越优先；比如`java System#getProperties()`（当前jvm进程）和操作系统环境变量（当前用户或者系统共享）
- **远程配置**来自于配置应用端程序进程物理环境以外的环境

### java标准外部化配置

#### Java SE

* Java系统属性 - System#getProperties()
* 操作系统环境变量 - System#getEnv()
* 偏好设置 - java.util.prefs.Preferences
  * 树形层次结构	

#### Java EE

* Servlet 上下文配置 - ServletContext#getInitParameter(String)
* Servlet 配置 - ServletConfig#getInitParameter(String)
* JSP配置
* JNDI配置 - Context#lookup()

### 整合MicroProfile Config

应用程序仅仅知道配置名称，不关心数据来源。比如只知道配置名称“application.name”，配置可能来源于很多地方，比如说`System#getProperties()`，OS环境变量，文件或者JNDI，`ServletConfig`，`ServletContext`。我们只需要调用`Config#getValue("application.name")`，**不需要管这个配置到底是从哪里来的**

#### 配置来源 - ConfigSource

- 属性键值对信息 - getProperties()
- ordinal（当前配置的绝对顺序）- getOrdinal()
- 获取配置值方法getValue()

#### 配置同一门面 - Config

- 与配置来源是1：N的关系
- 相较于ConfigSource获取配置而言有类型转换

#### 配置SPI - ConfigProviderResolver

利用java spi来加载对应的配置源实现类



## 日志管理（Java Logging）

从java 1.4引入，部分设计参考了log4j的设计

### 日志数据类型 - LogRecord

> 在log4j里面称之为LogEvent



### 日志过滤器 - filter

决定当前日志是否要输出

#### 日志级别 - java.util.logging

每个日志消息都有一个关联的日志级别。日志级对象封装了一个整数值，值越大表示优先级越高

Q. 为什么日志级别没有用枚举？

A. 因为枚举是java 1.5才出现，而java logging在1.4就引入了。

### 日志处理器 - handler

Java SE提供以下日志处理器实现：

- Streamhandler - 将格式化记录写入OutputStream
- COnsoleHandler - 写入格式记录的简单处理程序System.err
- FIleHandler  - 将已格式化的日志写入一个文件或一组循环日志文件的处理程序
- SocketHandler - 将已格式化的日志写入远程TCP端口的处理程序
- MemoryHandler - 在内存中缓冲日志记录的处理程序

### 日志对象（Loggers）

Logger可以从其在记录者名称空间中的父类继承各种属性。

### 日志管理器（LogManager）

全局的LogManager对象，用于跟踪全局日志记录信息。LogManager对象可以使用静态LogManager.getLogManager方法获得。在LogManager初始化期间根据系统属性创建。此属性允许容器应用程序用其自身的LogManager子类代替莫人类

LogManager对象包括：

- 命名的Logger分层名称空间
- 从配置文件中读取的一组日志记录控制属性

## 监控管理

Java需要一套统一的近程和远程监控与管理的架构 - JMX

三个层次:

- 装配层 -> 将被管理资源（对象）封装成JMX认可的结构。
- 代理层 - JMX注册适配层资源服务器，并且能够暴露成相关的通讯协议给分布式服务层
- 分布式服务层 - 通过JMX连接和协议适配来实现的应用**客户端**

### JMX（`Java Management Extensions`）基础

### 基本架构

- Distributed Services Level - 分布式服务层
- Agent Level - 代理层（接口层）
- Instrumentation Level - 设备级别（接口实现层）

### ObjectName

例如：Tomcat:type=ThreadPool.name="http-bio-8080"

### JMX MBean属性

类同于Java对象属性

JMX API - `MBeanAttributeInfo`

JavaBeans API - `PropertyDescriptor` 

### JMX MBean操作

类同于JavaBeans Method

JMX-API - `MBeanOperationInfo`

JavaBeans API - `MethodDescriptor`

### JMX - Agent Level

#### MBean服务器

是一个在代理上的MBean的注册器，仅用作暴露MBean的管理接口，而非其引用对象

#### 代理服务

是在MBean服务器上能够执行已注册MBean的管理操作，包括以下代理服务

- 动态类加载
- 监控
- 定时器
- 服务关系

### JMX - Instrumentation Level

- 管理Bean（MBeans）
  - 标准MBeans
  - 动态MBeans
  - 开发MBeans
  - 模型MBeans
- 通知模型（Notification Model）
- MBean元数据类（MetaData Class）

#### JMX管理Bean（MBeans）

- 标准MBeans - Bean的管理通过接口方法来描述
- 动态MBeans - 必须事先指定的接口，不过他在运行时能让管理接口发挥最大弹性
- 开放MBeans - 动态MBean，提供通用管理所依赖的基本数据类型以及用户友好的自描述信息
- 模型MBeans - 动态MBean，在运行时能够完全可配置和字描述，为动态的设备资源提供带有默认行为的MBean泛型类

#### 标准MBeans

规范：**标准MBean接口必须以`MBean`为后缀，如：`com.example.MyClassMBean`，同时，其实现类的名称必须是MyClass**。与Java Beans内省类似， 以上MBean接口检查和设计模式的应用的处理称之为内省（Introspection）

- **JMX内省**：对预定MBean接口进行解析，解析出接口的元信息（`MBeanInfo`）：
  - 属性 - MBeanAttributeInfo
  - 操作 - MBeanOperationInfo
  - 构造器 - MBeanConstructorInfo
  - 通知 - MBeanNotificationInfo

- **JavaBeans内省**：将预定类型进行解析，解析出元信息（`BeanInfo`）：    
  - 属性 - PropertyDescriptor
  - 方法 - MethodDescriptor
  - 构造器 - 
  - 监听器 - EventSetDescriptor  

#### JavaBean和MBean的区别？

在JavaBean中，对私有的域用getXXX和setXXX来实现存取，并且必须有一个无参数的构造函数

MBean也是一个JavaBean，它有着和JavaBean相同的要求

#### MBean和MXBean的区别？

MXBean会把被`@MXBean`标注的接口变成DynamicBean

#### 动态MBean

没有固定的结构，通过实现`javax.management.DynamicMBean`接口由自己来描述

静态的MBean最终还是会变成动态MBean:

 通过方法：`com.sun.jmx.mbeanserver.Introspector#makeDynamicMBean`

#### 开放MBean

基本数据类型除了java基本数据类型和其包装类，还有下面几个：

- `ObjectName`
- `CompositeData` - 组合类型
- `TabularData` - 扁平化的结构，类似于Map结构 

开放MBean中的用处：

服务端：jconsole，客户端写的MBean中有除了基本数据类型及其包装类之外的数据类型，那么服务端应该怎么才能识别的出来这些数据类型呢？

需要服务端和客户端达成一个一致的约定，如`CompositeData`和`TabularData`

- CompositeData -> 键值对
- TabularData  -> 扁平结构

类的结构是树形，而扁平结构只有单一维度，通过TabularData把类转换成了一种扁平结构

#### 模型MBean

 JMX装配层觉得实现MBean或者MXBean接口很繁杂，需要通过POJO注册MBean，因此引入模型Bean



## 参考资料

-  Tomcat关于JMX的监控管理

















