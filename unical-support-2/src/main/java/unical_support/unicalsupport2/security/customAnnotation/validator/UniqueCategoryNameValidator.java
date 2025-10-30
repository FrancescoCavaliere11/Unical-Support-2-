package unical_support.unicalsupport2.security.customAnnotation.validator;

import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.security.customAnnotation.annotation.UniqueCategoryName;


@RequiredArgsConstructor
public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryName, String> {
    private final CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        return !categoryRepository.existsByNameIgnoreCase(value);
    }
}
