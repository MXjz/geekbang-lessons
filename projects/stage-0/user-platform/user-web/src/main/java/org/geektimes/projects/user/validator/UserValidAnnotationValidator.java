package org.geektimes.projects.user.validator;

import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author xuejz
 * @description
 * @Time 2021/3/9 15:12
 */
public class UserValidAnnotationValidator implements ConstraintValidator<UserValid, User> {

    private int idRange;

    @Override
    public void initialize(UserValid annotation) {
        this.idRange = annotation.idRange();
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
