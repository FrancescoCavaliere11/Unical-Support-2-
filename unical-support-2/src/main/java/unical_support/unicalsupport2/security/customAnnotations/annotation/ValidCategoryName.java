package unical_support.unicalsupport2.security.customAnnotations.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotations.validator.ValidCategoryNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidCategoryNameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidCategoryName {
    String message() default "Category name does not exist";


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
