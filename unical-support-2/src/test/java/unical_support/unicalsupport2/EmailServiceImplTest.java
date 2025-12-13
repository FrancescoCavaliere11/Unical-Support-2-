package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.data.entities.Answers;
import unical_support.unicalsupport2.data.entities.Email;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.EmailRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.implementation.EmailServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSaveResponderResult() {
        String emailIdStr = "100";
        Email mockEmail = new Email();
        mockEmail.setId(emailIdStr);

        Template mockTemplate = new Template();
        mockTemplate.setName("Standard");
        when(templateRepository.findByNameIgnoreCase("Standard")).thenReturn(Optional.of(mockTemplate));

        SingleResponseDto responseDto = new SingleResponseDto(
                "Tasse",
                "Standard",
                "Testo risposta",
                Map.of("k", "v"),
                "0.95"
        );
        ResponderResultDto resultDto = new ResponderResultDto(100, List.of(responseDto));

        emailService.saveAnswers(mockEmail, resultDto);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository).save(emailCaptor.capture());

        Email savedEmail = emailCaptor.getValue();
        Answers answers = savedEmail.getAnswers();

        assertNotNull(answers);
        assertFalse(answers.getAnswered(), "Il flag answered deve essere true");
        assertEquals(1, answers.getSingleAnswers().size());

        var singleAnswer = answers.getSingleAnswers().getFirst();

        assertEquals("Testo risposta", singleAnswer.getAnswer());
        assertEquals(mockTemplate, singleAnswer.getTemplate());
    }

    @Test
    void testSaveResponderResult_EmptyList() {
        String emailIdStr = "100";
        Email mockEmail = new Email();
        mockEmail.setId(emailIdStr);

        ResponderResultDto resultDto = new ResponderResultDto(1, Collections.emptyList());

        emailService.saveAnswers(mockEmail, resultDto);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository).save(emailCaptor.capture());

        Answers answers = emailCaptor.getValue().getAnswers();
        assertFalse(answers.getAnswered(), "Se non ci sono risposte, answered deve essere false");
        assertTrue(answers.getSingleAnswers().isEmpty());
    }
}