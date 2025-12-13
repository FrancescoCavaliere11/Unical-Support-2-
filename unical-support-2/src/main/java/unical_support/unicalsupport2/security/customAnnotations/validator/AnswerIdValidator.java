package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.repositories.AnswersRepository;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidAnswerId;

@RequiredArgsConstructor
public class AnswerIdValidator implements ConstraintValidator<ValidAnswerId, String> {
    private final AnswersRepository answersRepository;

    @Override
    public boolean isValid(String id, jakarta.validation.ConstraintValidatorContext context) {
        return answersRepository.existsById(id);
    }
}
