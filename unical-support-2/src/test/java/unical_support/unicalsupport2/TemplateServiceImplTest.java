package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unical_support.unicalsupport2.data.embeddables.TemplateParameter;
import unical_support.unicalsupport2.service.implementation.TemplateServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TemplateServiceImplTest {

    @InjectMocks
    private TemplateServiceImpl templateService;

    @Test
    void validateParameterOccurrences_shouldReturnTrueWhenMatching() throws Exception {
        // given
        String content = "Dear {{first_ame}}, your ID is {{user_d}}.";
        List<TemplateParameter> params = new ArrayList<>();

        TemplateParameter p1 = new TemplateParameter();
        p1.setName("first_name");
        p1.setRequired(true);

        TemplateParameter p2 = new TemplateParameter();
        p2.setName("user_id");
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
