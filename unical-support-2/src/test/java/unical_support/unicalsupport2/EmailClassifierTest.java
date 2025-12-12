package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.implementation.EmailClassifierImpl;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.LlmClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailClassifierTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LlmClient geminiApiClient;

    @Mock
    private PromptService promptService;

    private EmailClassifier classifierService;

    @BeforeEach
    void setup() {
        classifierService = new EmailClassifierImpl(categoryRepository, geminiApiClient, promptService);
    }

    @Test
    @DisplayName("Gestisce correttamente email con oggetto/corpo nulli o vuoti")
    void acceptsEmptyOrNullSubjectBody() throws Exception {
        when(promptService.buildClassifyPrompt(anyList())).thenReturn("system prompt");

        when(geminiApiClient.chat(anyString())).thenReturn("[]");

        ClassificationEmailDto e1 = new ClassificationEmailDto(); e1.setSubject(null); e1.setBody("");
        ClassificationEmailDto e2 = new ClassificationEmailDto(); e2.setSubject(""); e2.setBody(null);
        ClassificationEmailDto e3 = new ClassificationEmailDto(); e3.setSubject(" "); e3.setBody("   ");

        List<ClassificationResultDto> out = classifierService.classifyEmail(List.of(e1, e2, e3));

        assertThat(out).hasSize(3);

        out.forEach(r -> assertThat(r.getCategories())
                .as("Deve contenere solo la categoria di fallback")
                .hasSize(1)
                .first()
                .extracting(SingleCategoryDto::getCategory)
                .isEqualTo("NON RICONOSCIUTA"));
    }

    @Test
    @DisplayName("Supporta il vecchio formato single-label (retrocompatibilità)")
    void acceptsOldSingleLabelFormat() throws Exception {
        when(promptService.buildClassifyPrompt(anyList())).thenReturn("system");

        // CORREZIONE 1: Aggiunto campo "text" necessario per la validazione
        // Assicurati che il nome "RECLAMO" corrisponda esattamente a quello nel mock del repository
        String oldJsonFormat = """
        [
          {
            "id":0,
            "category":"RECLAMO",
            "confidence":0.9,
            "explanation":"ok",
            "text": "contenuto estratto dal testo email"
          }
        ]
        """;
        when(geminiApiClient.chat(anyString())).thenReturn(oldJsonFormat);

        Category c1 = new Category(); c1.setName("RECLAMO");
        // Importante: il mock del repository deve restituire la categoria che ci aspettiamo
        when(categoryRepository.findAll()).thenReturn(List.of(c1));

        ClassificationEmailDto e = new ClassificationEmailDto();
        e.setSubject("Test");
        e.setBody("Test");

        List<ClassificationResultDto> out = classifierService.classifyEmail(List.of(e));

        assertThat(out).hasSize(1);
        var cats = out.getFirst().getCategories();
        assertThat(cats).hasSize(1);

        // Ora dovrebbe passare perché il JSON ha "category" e il Repository ha "RECLAMO"
        assertThat(cats.getFirst().getCategory()).isEqualTo("RECLAMO");
    }

    @Test
    @DisplayName("Supporta il formato multi-label correttamente")
    void multiLabelParsingWorksCorrectly() throws Exception {
        when(promptService.buildClassifyPrompt(anyList())).thenReturn("system");

        String multiLabelJson = """
        [
          {
            "id":0,
            "categories":[
               {"category":"ESAMI_E_APPELLI","confidence":0.8,"text":"prenotazione esame"},
               {"category":"SERVIZI_CAMPUS","confidence":0.6,"text":"rete wifi"}
            ],
            "explanation":"Riguarda più aree"
          }
        ]
        """;
        when(geminiApiClient.chat(anyString())).thenReturn(multiLabelJson);

        Category c1 = new Category(); c1.setName("ESAMI_E_APPELLI");
        Category c2 = new Category(); c2.setName("SERVIZI_CAMPUS");
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        ClassificationEmailDto e = new ClassificationEmailDto();
        e.setSubject("Multi");
        e.setBody("Body");

        List<ClassificationResultDto> out = classifierService.classifyEmail(List.of(e));

        assertThat(out).hasSize(1);
        List<SingleCategoryDto> cats = out.getFirst().getCategories();

        assertThat(cats)
                .extracting(SingleCategoryDto::getCategory)
                .containsExactlyInAnyOrder("ESAMI_E_APPELLI", "SERVIZI_CAMPUS");
    }
}