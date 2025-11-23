package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unical_support.unicalsupport2.data.enumerators.PromptStrategyName;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidStrategyName;

import java.util.Arrays;

public class StrategyNameValidator implements ConstraintValidator<ValidStrategyName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.stream(PromptStrategyName.values())
                .anyMatch(ps -> ps.name().equals(value) || ps.getBeanName().equals(value));
    }
}
