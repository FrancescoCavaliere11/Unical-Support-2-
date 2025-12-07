package unical_support.unicalsupport2.security.customAnnotations.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import unical_support.unicalsupport2.security.customAnnotations.validator.MultipartExtensionValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MultipartExtensionValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMultipartExtension {
    String message() default "Invalid document file extension. Allowed extensions are: pdf, doc, docx, txt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
