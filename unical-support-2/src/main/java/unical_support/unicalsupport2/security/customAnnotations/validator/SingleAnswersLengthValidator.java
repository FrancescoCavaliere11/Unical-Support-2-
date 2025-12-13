package unical_support.unicalsupport2.security.customAnnotations.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import unical_support.unicalsupport2.data.dto.email.UpdateAnswerDto;
import unical_support.unicalsupport2.data.repositories.AnswersRepository;
import unical_support.unicalsupport2.security.customAnnotations.annotation.ValidSingleAnswersLength;

@RequiredArgsConstructor
public class SingleAnswersLengthValidator implements ConstraintValidator<ValidSingleAnswersLength, UpdateAnswerDto> {
    private final AnswersRepository answersRepository;

    @Override
    public boolean isValid(UpdateAnswerDto updateAnswerDto, ConstraintValidatorContext constraintValidatorContext) {
        // todo non so se devo controllare se l'id esiste o meno
        if(updateAnswerDto == null || updateAnswerDto.getSingleAnswers() == null) return true;

        int expectedSize = answersRepository.countById(updateAnswerDto.getId());
        int actualSize = updateAnswerDto.getSingleAnswers().size();
        return expectedSize == actualSize;
    }
}
