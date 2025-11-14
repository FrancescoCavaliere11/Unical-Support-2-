package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import unical_support.unicalsupport2.commands.EmailCommand;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.SingleCategoryDto;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;
import unical_support.unicalsupport2.service.interfaces.EmailSender;
import unical_support.unicalsupport2.service.interfaces.JudgerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
class EmailCommandIntegrationTest {

    @MockitoBean
    private EmailReceiver emailReceiver;

    @MockitoBean
    private EmailClassifier emailClassifier;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private JudgerService  judgerService;


    @Test
    void emailNotRecognizedShouldBeForwarded() {
        EmailMessage fakeEmail = new EmailMessage();
        fakeEmail.setTo(List.of("mittente@esempio.it"));
        fakeEmail.setSubject("Domanda strana");
        fakeEmail.setBody("Corpo della mail");

        when(emailReceiver.receiveEmails()).thenReturn(List.of(fakeEmail));

        ClassificationResultDto fakeResult =
                new ClassificationResultDto(List.of(new SingleCategoryDto("NON_RICONOSCIUTA", 0.1, "testo")), "Categoria non trovata");
        when(emailClassifier.classifyEmail(anyList())).thenReturn(List.of(fakeResult));

        EmailCommand emailCommand = new EmailCommand(emailReceiver, emailClassifier, emailSender,judgerService);
        emailCommand.fetchEmailAndClassify();

        var captor = org.mockito.ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, times(1)).sendEmail(captor.capture());

        EmailMessage forwarded = captor.getValue();
        assertThat(forwarded.getTo()).containsExactly("misentouncavallo@gmail.com");
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
                new ClassificationResultDto(List.of(new SingleCategoryDto("ISCRIZIONE", 0.95, "parte relativa all’iscrizione")), "Corrisponde alla categoria Iscrizione");
        when(emailClassifier.classifyEmail(anyList())).thenReturn(List.of(recognized));

        EmailCommand emailCommand = new EmailCommand(emailReceiver, emailClassifier, emailSender,judgerService);
        emailCommand.fetchEmailAndClassify();

        verify(emailSender, never()).sendEmail(any());
    }
}
