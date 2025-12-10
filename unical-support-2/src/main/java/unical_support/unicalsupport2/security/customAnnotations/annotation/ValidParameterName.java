package unical_support.unicalsupport2.security.customAnnotations.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotations.validator.ParameterNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ParameterNameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ValidParameterName {
    String message() default "Invalid parameter name format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
