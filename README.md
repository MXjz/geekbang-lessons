# geekbang-lessons
极客时间课程工程

## 第五次作业

测试接口使用SpringBoot 编写：

POST请求的Content-Type设置为：application/json，所以使用@RequestBody来接收POST 请求体参数

```java
@PostMapping("/post/test")
public String postTest(@RequestBody TestEntity testEntity) {
  return "POST - hello, " + testEntity.user;
}
```



测试代码：

TestEntity

```java
public class TestEntity implements Serializable {
    private static final long serialVersionUID = 4303910943165037662L;

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
```



main

```java
public static void main(String[] args) {
  Client client = ClientBuilder.newClient();
  // POST 请求
  TestEntity testEntity = new TestEntity();
  testEntity.setUser("darling");
  Entity<TestEntity> entity = Entity.json(testEntity);
  Response response = client
    .target("http://127.0.0.1:9095/post/test")
    .request()
    .header("Content-Type", "application/json")
    .post(entity);
  String content = response.readEntity(String.class);
  System.out.println(content);
}
```



post请求代码实现见：`org.geektimes.rest.client.HttpPostInvocation`

