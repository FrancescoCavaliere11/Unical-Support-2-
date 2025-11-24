package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.service.implementation.OrchestratorServiceImpl;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;
import unical_support.unicalsupport2.service.interfaces.EmailResponder;
import unical_support.unicalsupport2.service.interfaces.EmailSender;
import unical_support.unicalsupport2.service.interfaces.JudgerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
class OrchestratorTest {

    @MockitoBean
    private EmailReceiver emailReceiver;

    @MockitoBean
    private EmailClassifier emailClassifier;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private EmailResponder emailResponder;

    @MockitoBean
    private JudgerService  judgerService;

    @Mock
    ModelMapper modelMapper;


    @Test
    void emailNotRecognizedShouldBeForwarded() {
        EmailMessage fakeEmail = new EmailMessage();
        fakeEmail.setTo(List.of("mittente@esempio.it"));
        fakeEmail.setSubject("Domanda strana");
        fakeEmail.setBody("Corpo della mail");

        when(emailReceiver.receiveEmails()).thenReturn(List.of(fakeEmail));

        ClassificationResultDto fakeResult =
                new ClassificationResultDto(List.of(new SingleCategoryDto("NON_RICONOSCIUTA", 0.1, "testo")), "Categoria non trovata", 0);
        when(emailClassifier.classifyEmail(anyList())).thenReturn(List.of(fakeResult));

        OrchestratorServiceImpl orchestrator = new OrchestratorServiceImpl(emailReceiver, emailClassifier, emailSender, emailResponder, judgerService, modelMapper);
        orchestrator.start();

        var captor = org.mockito.ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, times(1)).sendEmail(captor.capture());

        EmailMessage forwarded = captor.getValue();
        assertThat(forwarded.getTo()).containsExactly("lorenzo.test.04112025@gmail.com");
        assertThat(forwarded.getSubject()).startsWith("Email non riconosciuta:");
        assertThat(forwarded.getBody()).contains("mittente@esempio.it");
    }

    @Test
    void recognizedEmailsShouldNotBeForwarded() {
        EmailMessage email = new EmailMessage();
        email.setTo(List.of("utente@esempio.it"));
        email.setSubject("Richiesta iscrizione");
        email.setBody("Vorrei iscrivermi al corso di AI.");

        when(emailReceiver.receiveEmails()).thenReturn(List.of(email));

        ClassificationResultDto recognized =
                new ClassificationResultDto(List.of(new SingleCategoryDto("ISCRIZIONE", 0.95, "parte relativa all’iscrizione")), "Corrisponde alla categoria Iscrizione", 0);
        when(emailClassifier.classifyEmail(anyList())).thenReturn(List.of(recognized));

        OrchestratorServiceImpl orchestrator = new OrchestratorServiceImpl(emailReceiver, emailClassifier, emailSender, emailResponder, judgerService, modelMapper);
        orchestrator.start();

        verify(emailSender, never()).sendEmail(any());
    }
}
