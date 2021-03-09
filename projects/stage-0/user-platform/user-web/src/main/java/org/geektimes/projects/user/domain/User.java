package org.geektimes.projects.user.domain;

import org.geektimes.projects.user.domain.group.ValidateStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * 用户领域对象
 *
 * @since 1.0
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    //@NotNull(groups = {ValidateStrategy.UserRegister.class}, message = "id不能为空")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @NotNull(groups = {ValidateStrategy.UserRegister.class, ValidateStrategy.UserLogin.class}, message = "用户名不得为空")
    @NotBlank(groups = {ValidateStrategy.UserRegister.class, ValidateStrategy.UserLogin.class}, message = "用户名不得为空")
    private String name;

    @Column
    @Size(groups = {ValidateStrategy.UserRegister.class, ValidateStrategy.UserLogin.class},
            max = 32,
            min = 6,
            message = "密码位数不能小于6位且不能超过32")
    private String password;

    @Column
    @Email(groups = {ValidateStrategy.UserRegister.class}, message = "电子邮件地址不合法")
    private String email;

    @Column
    @Pattern(groups = {ValidateStrategy.UserRegister.class}, regexp = "\\d{11}|\\d{4}-\\d{7,8}", message = "电话号码格式不正确(格式: 18002327777 或 0937-8950190)")
    private String phoneNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(password, user.password) && Objects.equals(email, user.email) && Objects.equals(phoneNumber, user.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, password, email, phoneNumber);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
