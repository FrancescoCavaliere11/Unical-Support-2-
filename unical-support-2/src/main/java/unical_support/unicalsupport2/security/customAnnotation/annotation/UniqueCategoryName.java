package unical_support.unicalsupport2.security.customAnnotation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotation.validator.UniqueCategoryNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = UniqueCategoryNameValidator.class)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCategoryName {

    String message() default "Name already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
