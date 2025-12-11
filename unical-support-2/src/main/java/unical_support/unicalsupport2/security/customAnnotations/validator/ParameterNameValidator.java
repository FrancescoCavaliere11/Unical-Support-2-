package unical_support.unicalsupport2.security.customAnnotations.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidParameterName;

public class ParameterNameValidator implements ConstraintValidator<ValidParameterName, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return false;
        return value.matches("^[a-z]+(_[a-z]+)*$");
    }
}
