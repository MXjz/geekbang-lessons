package org.geektimes.projects.user.validator.bean.validation;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.domain.group.ValidateStrategy;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/9 15:53
 */
public class BeanValidationDemo {

    public static void main(String[] args) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        User user = new User();
//        user.setId((long) 1);
        user.setName("111");
        user.setPassword("888119");
        user.setPhoneNumber("180223275572");
        user.setEmail("@sian.com");
        //校验分组.
        // 假设:@NotNull在User的name上标注, 但是存在以下情况:
        // 1. 如果在用户注册时, 需要校验(group = REG)
        // 2. 如果在用户登录时, 不需要校验(group = LOGIN)
        Set<ConstraintViolation<User>> validateRes = validator.validate(user, ValidateStrategy.UserRegister.class); // 校验结果
        validateRes.forEach(res -> {
            System.out.println(res.getMessage());
        });
    }
}
