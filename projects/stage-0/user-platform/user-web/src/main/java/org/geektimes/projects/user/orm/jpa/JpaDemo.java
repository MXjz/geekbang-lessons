package org.geektimes.projects.user.orm.jpa;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.geektimes.projects.user.domain.User;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/7 21:54
 */
public class JpaDemo {

    @PersistenceContext(name = "emf")
    EntityManager entityManager;

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory
                = Persistence.createEntityManagerFactory("emf", getProperties());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        User user = new User();
        user.setName("xuejz");
        user.setPassword("xx..xx55");
        user.setEmail("xxx@gmail.com");
        user.setPhoneNumber("123456789");
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(user);
        transaction.commit();

        System.out.println(entityManager.find(User.class, 1L));
    }

    private static Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
        properties.put("hibernate.id.new_generator_mappings", false);
        properties.put("hibernate.connection.datasource", getDataSource());
        return properties;
    }

    private static Object getDataSource() {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("db/user-platform");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }


}
