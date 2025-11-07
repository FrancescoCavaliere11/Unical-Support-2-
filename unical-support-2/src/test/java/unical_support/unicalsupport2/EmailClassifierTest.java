package unical_support.unicalsupport2;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import unical_support.unicalsupport2.data.dto.ClassificationEmailDto;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.implementation.EmailClassifierImpl;
import unical_support.unicalsupport2.service.implementation.PromptServiceImpl;
import unical_support.unicalsupport2.service.interfaces.GeminiApiClient;
import unical_support.unicalsupport2.service.interfaces.PromptService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
@ExtendWith(MockitoExtension.class)
public class EmailClassifierTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GeminiApiClient geminiApiClient;

    @Mock
    private PromptService promptService;

    @Test
    void acceptsEmptyOrNullSubjectBody() throws Exception {
        when(promptService.buildSystemMessageBatch()).thenReturn("system");
        when(promptService.buildUserMessageBatch(anyList())).thenReturn("user");

        when(geminiApiClient.chat(anyString(), anyString())).thenReturn("[]");

        EmailClassifierImpl svc = new EmailClassifierImpl(categoryRepository, geminiApiClient, promptService);


        ClassificationEmailDto e1 = new ClassificationEmailDto();
        e1.setSubject(null);
        e1.setBody("");
        ClassificationEmailDto e2 = new ClassificationEmailDto();
        e2.setSubject("");
        e2.setBody(null);
        ClassificationEmailDto e3 = new ClassificationEmailDto();
        e3.setSubject(" ");
        e3.setBody("   ");

        List<ClassificationResultDto> out = svc.classifyEmail(List.of(e1, e2, e3));

        assertThat(out).hasSize(3);
        out.forEach(r -> assertThat(r.getCategories())
                .as("Mappa delle categorie deve contenere solo la fallback")
                .containsEntry("NON_RICONOSCIUTA", 0.0));

        verify(geminiApiClient, times(1)).chat(anyString(), anyString());
    }

    @Test
    void EveryResultHasCategoriesAndExplanation() throws Exception {

        PromptService ps = new PromptServiceImpl(categoryRepository);

        // ✅ risposta JSON con 10 elementi e i 3 campi richiesti
        when(geminiApiClient.chat(anyString(), anyString())).thenReturn("""
                  [
                    {"id":0,"category":"RECLAMO","confidence":0.75,"explanation":"ok"},
                    {"id":1,"category":"ESAMI_E_APPELLI","confidence":0.82,"explanation":"ok"},
                    {"id":2,"category":"LAUREA_E_TESI","confidence":0.90,"explanation":"ok"},
                    {"id":3,"category":"ERASMUS_E_MOBILITA_INTERNAZIONALE","confidence":0.88,"explanation":"ok"},
                    {"id":4,"category":"RICONOSCIMENTO_ESAMI_CFU","confidence":0.86,"explanation":"ok"},
                    {"id":5,"category":"SERVIZI_CAMPUS","confidence":0.80,"explanation":"ok"},
                    {"id":6,"category":"INFORMAZIONI_GENERICHE","confidence":0.70,"explanation":"ok"},
                    {"id":7,"category":"RECLAMO","confidence":0.90,"explanation":"ok"},
                    {"id":8,"category":"ESAMI_E_APPELLI","confidence":0.83,"explanation":"ok"},
                    {"id":9,"category":"SERVIZI_CAMPUS","confidence":0.79,"explanation":"ok"}
                  ]
                """);

        EmailClassifierImpl svc = new EmailClassifierImpl(categoryRepository, geminiApiClient, ps);

        // input costruito con setSubject/setBody (come fai tu)
        ClassificationEmailDto e1 = new ClassificationEmailDto();
        e1.setSubject("SERVIZI NON FUNZIONANTI");
        e1.setBody("I servizi web non funzionano.");
        ClassificationEmailDto e2 = new ClassificationEmailDto();
        e2.setSubject("Iscrizione appello");
        e2.setBody("Quando aprono le prenotazioni?");
        ClassificationEmailDto e3 = new ClassificationEmailDto();
        e3.setSubject("Tesi triennale");
        e3.setBody("Scadenze per consegna modulo?");
        ClassificationEmailDto e4 = new ClassificationEmailDto();
        e4.setSubject("Bando Erasmus");
        e4.setBody("Date per candidatura Erasmus?");
        ClassificationEmailDto e5 = new ClassificationEmailDto();
        e5.setSubject("Riconoscimento CFU");
        e5.setBody("Iter per esami svolti altrove.");
        ClassificationEmailDto e6 = new ClassificationEmailDto();
        e6.setSubject("Orari segreteria");
        e6.setBody("Aperture al pubblico.");
        ClassificationEmailDto e7 = new ClassificationEmailDto();
        e7.setSubject("Info generali");
        e7.setBody("Richiesta informazioni.");
        ClassificationEmailDto e8 = new ClassificationEmailDto();
        e8.setSubject("Reclamo certificato");
        e8.setBody("Certificato in ritardo.");
        ClassificationEmailDto e9 = new ClassificationEmailDto();
        e9.setSubject("Errore Esse3");
        e9.setBody("Prenotazione non va a buon fine.");
        ClassificationEmailDto e10 = new ClassificationEmailDto();
        e10.setSubject("Tasse");
        e10.setBody("Scadenza seconda rata?");

        List<ClassificationResultDto> out = svc.classifyEmail(List.of(
                e1, e2, e3, e4, e5, e6, e7, e8, e9, e10
        ));

        assertThat(out).hasSize(10);
        out.forEach(r -> {

            assertThat(r.getCategories())
                    .as("Ogni risultato deve avere almeno una categoria")
                    .isNotEmpty();


            r.getCategories().forEach((cat, conf) -> {
                assertThat(cat).isNotBlank();
                assertThat(conf)
                        .as("Confidenza deve essere tra 0 e 1")
                        .isBetween(0.0, 1.0);
            });

            assertThat(r.getExplanation())
                    .as("Spiegazione non deve essere vuota")
                    .isNotBlank();
        });
    }

    @Test
    void acceptsOldSingleLabelFormat() throws Exception {
        when(promptService.buildSystemMessageBatch()).thenReturn("system");
        when(promptService.buildUserMessageBatch(anyList())).thenReturn("user");

        when(geminiApiClient.chat(anyString(), anyString())).thenReturn("""
        [
          {"id":0,"category":"RECLAMO","confidence":0.9,"explanation":"ok"}
        ]
        """);

        Category c1 = new Category();
        c1.setId("1");
        c1.setName("RECLAMO");
        c1.setDescription("Segnalazioni e reclami vari");

        Category c2 = new Category();
        c2.setId("2");
        c2.setName("ESAMI_E_APPELLI");
        c2.setDescription("Gestione degli appelli e iscrizioni");

        Category c3 = new Category();
        c3.setId("3");
        c3.setName("SERVIZI_CAMPUS");
        c3.setDescription("Servizi e infrastrutture del campus");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2, c3));

        EmailClassifierImpl svc = new EmailClassifierImpl(categoryRepository, geminiApiClient, promptService);
        ClassificationEmailDto e = new ClassificationEmailDto();
        e.setSubject("Reclamo per certificato");
        e.setBody("Certificato non ricevuto.");

        List<ClassificationResultDto> out = svc.classifyEmail(List.of(e));

        assertThat(out).hasSize(1);
        assertThat(out.getFirst().getCategories())
                .as("Compatibilità con vecchio formato category/confidence")
                .containsEntry("RECLAMO", 0.9);
    }

}