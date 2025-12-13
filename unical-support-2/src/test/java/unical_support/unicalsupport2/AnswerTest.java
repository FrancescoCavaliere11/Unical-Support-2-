package unical_support.unicalsupport2;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import unical_support.unicalsupport2.data.dto.email.UpdateAnswerDto;
import unical_support.unicalsupport2.data.dto.email.UpdateSingleAnswerDto;
import unical_support.unicalsupport2.data.repositories.AnswersRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
public class AnswerTest {
    @Autowired
    private Validator validator;

    @MockitoBean
    private AnswersRepository answersRepository;

    private UpdateAnswerDto validDto(){
        UpdateSingleAnswerDto singleAnswerDto = new UpdateSingleAnswerDto();
        singleAnswerDto.setAnswer("single-answer");

        UpdateAnswerDto dto = new UpdateAnswerDto();
        dto.setId("answer-id");
        dto.setSingleAnswers(List.of(singleAnswerDto));
        return dto;
    }

    @Test
    void validDto_shouldPassValidation(){
        when(answersRepository.existsById("answer-id")).thenReturn(true);
        when(answersRepository.countById("answer-id")).thenReturn(1);

        UpdateAnswerDto dto = validDto();

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidId_shouldFailValidation(){
        when(answersRepository.existsById("answer-id")).thenReturn(false);

        UpdateAnswerDto dto = validDto();

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void nullSingleAnswers_shouldFailValidation(){
        when(answersRepository.existsById("answer-id")).thenReturn(true);
        when(answersRepository.countById("answer-id")).thenReturn(1);

        UpdateAnswerDto dto = validDto();
        dto.setSingleAnswers(null);

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void mismatchedSingleAnswersLength_shouldFailValidation(){
        when(answersRepository.existsById("answer-id")).thenReturn(true);
        when(answersRepository.countById("answer-id")).thenReturn(2);

        UpdateAnswerDto dto = validDto();

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
}
