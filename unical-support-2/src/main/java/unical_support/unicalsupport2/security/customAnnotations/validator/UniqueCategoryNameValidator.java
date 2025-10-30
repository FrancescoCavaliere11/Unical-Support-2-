package unical_support.unicalsupport2.security.customAnnotations.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.security.customAnnotations.annotation.UniqueCategoryName;

@RequiredArgsConstructor
public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryName, String> {
    private final CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return !categoryRepository.existsByNameIgnoreCase(name);
    }
}
