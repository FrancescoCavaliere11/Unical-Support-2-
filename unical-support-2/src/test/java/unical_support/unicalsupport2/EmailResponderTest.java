package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.data.dto.responder.SingleResponseDto;
import unical_support.unicalsupport2.data.embeddables.TemplateParameter;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.implementation.EmailResponderImpl;
import unical_support.unicalsupport2.service.implementation.PromptServiceImpl;
import unical_support.unicalsupport2.service.interfaces.EmailResponder;
import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
public class EmailResponderTest {
    @Autowired
    private GeminiApiClient geminiApiClient;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    public void matchingTemplateTest() {
        ClassificationResultDto classificationResultDto = new ClassificationResultDto(
                List.of(
                        new SingleCategoryDto(
                                "Category1",
                                0.95,
                                "Buongiorno, sono Mario Rossi e vorrei richiedere il " +
                                        "riconoscimento dell'esame 'Matematica Avanzata' " +
                                        "sostenuto presso un altro corso di laurea."
                        )
                ),
                "Test explanation",
                0
        );

        Template template = getTemplate();

        when(templateRepository.findByCategoryNameIgnoreCase("Category1")).thenReturn(List.of(template));


        PromptService promptService = new PromptServiceImpl(categoryRepository, templateRepository);
        EmailResponder emailResponder = new EmailResponderImpl(geminiApiClient, promptService);

        List<ResponderResultDto> responses = emailResponder.generateEmailResponse(List.of(classificationResultDto));
        assertEquals(1, responses.size());

        ResponderResultDto responderResultDto = responses.getFirst();
        assertEquals(1, responderResultDto.getResponses().size());

        SingleResponseDto singleResponse = responderResultDto.getResponses().getFirst();
        assertEquals(template.getName(), singleResponse.getTemplate());
        assertEquals("Mario Rossi", singleResponse.getParameter().get("student_name"));
        assertEquals("Matematica Avanzata", singleResponse.getParameter().get("exam_name"));
        assertEquals("OK", singleResponse.getReason());

    }

    @Test
    public void noMatchingTemplateTest() {
        ClassificationResultDto classificationResultDto = new ClassificationResultDto(
                List.of(
                        new SingleCategoryDto(
                                "Category2",
                                0.90,
                                "Ciao, vorrei sapere come iscriversi ai corsi online."
                        )
                ),
                "Test explanation",
                0
        );

        when(templateRepository.findByCategoryNameIgnoreCase("Category2")).thenReturn(List.of());

        PromptService promptService = new PromptServiceImpl(categoryRepository, templateRepository);
        EmailResponder emailResponder = new EmailResponderImpl(geminiApiClient, promptService);

        List<ResponderResultDto> responses = emailResponder.generateEmailResponse(List.of(classificationResultDto));
        assertEquals(1, responses.size());

        ResponderResultDto responderResultDto = responses.getFirst();
        assertEquals(1, responderResultDto.getResponses().size());

        SingleResponseDto singleResponse = responderResultDto.getResponses().getFirst();
        assertEquals("NO_TEMPLATE_MATCH", singleResponse.getReason());
    }


    @Test
    public void requiredParameterMissingTest() {
        ClassificationResultDto classificationResultDto = new ClassificationResultDto(
                List.of(
                        new SingleCategoryDto(
                                "Category1",
                                0.95,
                                "Buongiorno, sono Mario Rossi e vorrei richiedere il " +
                                        "riconoscimento dell'esame sostenuto presso un altro corso di laurea."
                        )
                ),
                "Test explanation",
                0
        );

        Template template = getTemplate();

        when(templateRepository.findByCategoryNameIgnoreCase("Category1")).thenReturn(List.of(template));
        PromptService promptService = new PromptServiceImpl(categoryRepository, templateRepository);
        EmailResponder emailResponder = new EmailResponderImpl(geminiApiClient, promptService);

        List<ResponderResultDto> responses = emailResponder.generateEmailResponse(List.of(classificationResultDto));
        assertEquals(1, responses.size());

        ResponderResultDto responderResultDto = responses.getFirst();
        assertEquals(1, responderResultDto.getResponses().size());

        SingleResponseDto singleResponse = responderResultDto.getResponses().getFirst();
        assertEquals(template.getName(), singleResponse.getTemplate());
        assertEquals("Mario Rossi", singleResponse.getParameter().get("student_name"));
        assertNull(singleResponse.getParameter().get("exam_name"));
        assertTrue(singleResponse.getReason().startsWith("MISSING_REQUIRED_PARAMETER"));
    }


    private static Template getTemplate() {
        TemplateParameter param1 = new TemplateParameter();
        param1.setName("student_name");

        TemplateParameter param2 = new TemplateParameter();
        param2.setName("exam_name");

        Template template = new Template();
        template.setName("RISPOSTA_RICONOSCIMENTO_ESAMI");
        template.setContent("Gentile {{student_name}}, l'esame '{{exam_name}}' è stato riconosciuto e i CFU sono stati aggiornati nel suo piano di studi.");
        template.setParameters(List.of(param1, param2));
        return template;
    }
}
