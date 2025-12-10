package unical_support.unicalsupport2.security.customAnnotations.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotations.validator.ParametersValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ParametersValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
public @interface ValidParameters {
    String message() default "Invalid parameters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
