package org.geektimes.rest.demo;

import org.geektimes.rest.entity.TestEntity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestClientDemo {

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        // GET 请求
//        Response response = client
//                .target("http://127.0.0.1:9095/get/test")      // WebTarget
//                .request() // Invocation.Builder
//                .get();                                     //  Response
//        String content = response.readEntity(String.class);
//        System.out.println(content);
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
}
