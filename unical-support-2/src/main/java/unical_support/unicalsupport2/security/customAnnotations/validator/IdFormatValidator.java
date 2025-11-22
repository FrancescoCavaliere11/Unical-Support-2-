package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidIdFormat;

public class IdFormatValidator implements ConstraintValidator<ValidIdFormat, String> {
    @Override
    public boolean isValid(String id, ConstraintValidatorContext constraintValidatorContext) {
        if (id == null) return false;
        return id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
