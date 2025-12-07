package unical_support.unicalsupport2.security.customAnnotations.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotations.validator.ValidCategoryIdValidator;
import unical_support.unicalsupport2.security.customAnnotations.validator.ValidCategoryNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidCategoryIdValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidCategoryId {
    String message() default "Category id does not exist";


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
