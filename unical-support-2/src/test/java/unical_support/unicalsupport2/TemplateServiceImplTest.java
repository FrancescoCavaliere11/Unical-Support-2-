package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unical_support.unicalsupport2.data.embeddables.TemplateParameter;
import unical_support.unicalsupport2.data.entities.Template;
import unical_support.unicalsupport2.data.repositories.TemplateRepository;
import unical_support.unicalsupport2.service.implementation.TemplateServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TemplateServiceImplTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateServiceImpl templateService;

    @Test
    void renderTemplate_shouldReplacePlaceholdersCorrectly() {
        Template template = new Template();
        template.setName("WelcomeTemplate");
        template.setContent("Hello {{username}}, welcome to {{platform}}!");

        TemplateParameter p1 = new TemplateParameter();
        p1.setName("username");
        p1.setRequired(true);

        TemplateParameter p2 = new TemplateParameter();
        p2.setName("platform");
        p2.setRequired(true);

        template.setParameters(List.of(p1, p2));

        when(templateRepository.findByNameIgnoreCase(anyString()))
                .thenReturn(Optional.of(template));

        Map<String, String> params = Map.of(
                "username", "Alice",
                "platform", "UnicalSupport"
        );

        String result = templateService.renderTemplate("WelcomeTemplate", params);

        assertEquals("Hello Alice, welcome to UnicalSupport!", result);
    }

    @Test
    void renderTemplate_shouldThrowExceptionWhenMissingRequiredParam() {
        Template template = new Template();
        template.setName("TestTemplate");
        template.setContent("Hi {{name}}!");

        TemplateParameter param = new TemplateParameter();
        param.setName("name");
        param.setRequired(true);

        template.setParameters(List.of(param));

        lenient().when(templateRepository.findByNameIgnoreCase(anyString()))
                .thenReturn(Optional.of(template));

        assertThrows(IllegalArgumentException.class, () ->
                templateService.renderTemplate("TestTemplate", Collections.emptyMap()));
    }

    @Test
    void validateParameterOccurrences_shouldReturnTrueWhenMatching() throws Exception {
        // given
        String content = "Dear {{firstName}}, your ID is {{userId}}.";
        List<TemplateParameter> params = new ArrayList<>();

        TemplateParameter p1 = new TemplateParameter();
        p1.setName("firstName");
        p1.setRequired(true);

        TemplateParameter p2 = new TemplateParameter();
        p2.setName("userId");
        p2.setRequired(true);

        params.add(p1);
        params.add(p2);

        // when
        boolean result = callValidateParameterOccurrences(templateService, content, params);

        // then
        assertTrue(result);
    }

    @Test
    void validateParameterOccurrences_shouldReturnFalseWhenMismatched() throws Exception {
        // given
        String content = "Hello {{name}}!";
        TemplateParameter param = new TemplateParameter();
        param.setName("username");
        param.setRequired(true);
        List<TemplateParameter> params = List.of(param);

        // when
        boolean result = callValidateParameterOccurrences(templateService, content, params);

        // then
        assertFalse(result);
    }

    // Serve per testae i metodi private
    private boolean callValidateParameterOccurrences(
            TemplateServiceImpl service, String content, List<TemplateParameter> params
    ) throws Exception {
        var method = TemplateServiceImpl.class
                .getDeclaredMethod("validateParameterOccurrences", String.class, List.class);
        method.setAccessible(true);
        return (boolean) method.invoke(service, content, params);
    }
}
