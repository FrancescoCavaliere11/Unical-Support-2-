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
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.entities.Email;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.EmailRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.implementation.EmailServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSaveResponderResult() {
        String emailIdStr = "100";
        Email mockEmail = new Email();
        mockEmail.setId(emailIdStr);

        when(emailRepository.findById(emailIdStr)).thenReturn(Optional.of(mockEmail));

        Category mockCategory = new Category();
        mockCategory.setName("Tasse");
        when(categoryRepository.findByNameIgnoreCase("Tasse")).thenReturn(Optional.of(mockCategory));

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

        emailService.saveAnswers(resultDto);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository).save(emailCaptor.capture());

        Email savedEmail = emailCaptor.getValue();
        Answers answers = savedEmail.getAnswers();

        assertNotNull(answers);
        assertTrue(answers.getAnswered(), "Il flag answered deve essere true");
        assertEquals(1, answers.getSingleAnswers().size());

        var singleAnswer = answers.getSingleAnswers().getFirst();

        assertEquals("Testo risposta", singleAnswer.getAnswer());
        assertEquals(0.95, singleAnswer.getReason(), 0.001);
        assertEquals(mockCategory, singleAnswer.getCategory());
        assertEquals(mockTemplate, singleAnswer.getTemplate());
    }

    @Test
    void testNegativeSaveResponderResult() {
        when(emailRepository.findById(anyString())).thenReturn(Optional.of(new Email()));
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new Category()));
        when(templateRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new Template()));

        SingleResponseDto dirtyDto = new SingleResponseDto(
                "Cat", "Tmpl",
                null,
                null,
                "NonNumero"
        );
        ResponderResultDto resultDto = new ResponderResultDto(1, List.of(dirtyDto));

        emailService.saveAnswers(resultDto);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository).save(emailCaptor.capture());

        var savedAnswer = emailCaptor.getValue().getAnswers().getSingleAnswers().getFirst();

        assertNotNull(savedAnswer.getAnswer());
        assertEquals("", savedAnswer.getAnswer());
        assertEquals(0.0, savedAnswer.getReason(), 0.001);
    }

    @Test
    void testSaveResponderResult_EmptyList() {
        when(emailRepository.findById(anyString())).thenReturn(Optional.of(new Email()));

        ResponderResultDto resultDto = new ResponderResultDto(1, Collections.emptyList());

        emailService.saveAnswers(resultDto);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository).save(emailCaptor.capture());

        Answers answers = emailCaptor.getValue().getAnswers();
        assertFalse(answers.getAnswered(), "Se non ci sono risposte, answered deve essere false");
        assertTrue(answers.getSingleAnswers().isEmpty());
    }

    @Test
    void testSaveResponderResult_EmailNotFound() {
        when(emailRepository.findById("999")).thenReturn(Optional.empty());
        ResponderResultDto dto = new ResponderResultDto(999, List.of());

        assertThrows(RuntimeException.class, () -> emailService.saveAnswers(dto));
        verify(emailRepository, never()).save(any());
    }

    @Test
    void testSaveResponderResult_CategoryNotFound() {
        when(emailRepository.findById(anyString())).thenReturn(Optional.of(new Email()));
        when(categoryRepository.findByNameIgnoreCase("CategoriaInesistente")).thenReturn(Optional.empty());

        SingleResponseDto responseDto = new SingleResponseDto(
                "CategoriaInesistente", "Tmpl", "Content", null, "1.0"
        );
        ResponderResultDto resultDto = new ResponderResultDto(1, List.of(responseDto));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> emailService.saveAnswers(resultDto));
        assertTrue(ex.getMessage().contains("Category not found"));
    }
}