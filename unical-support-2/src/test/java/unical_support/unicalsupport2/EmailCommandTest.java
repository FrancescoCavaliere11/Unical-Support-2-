package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import unical_support.unicalsupport2.commands.EmailCommand;
import unical_support.unicalsupport2.data.dto.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailClassifier;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;
import unical_support.unicalsupport2.service.interfaces.EmailSender;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
public class EmailCommandTest {

   @Test
    void emailNotRecognizedShouldBeForwarded() {
        EmailReceiver receiver = mock(EmailReceiver.class);
        EmailClassifier classifier = mock(EmailClassifier.class);
        EmailSender sender = mock(EmailSender.class);

        EmailMessage fakeEmail = new EmailMessage();
        fakeEmail.setTo(List.of("mittente@esempio.it"));
        fakeEmail.setSubject("Domanda strana");
        fakeEmail.setBody("Corpo della mail");
        when(receiver.receiveEmails()).thenReturn(List.of(fakeEmail));

        ClassificationResultDto fakeResult =
                new ClassificationResultDto("NON_RICONOSCIUTA", 0.1, "Categoria non trovata");
        when(classifier.classifyEmail(anyList())).thenReturn(List.of(fakeResult));

        EmailCommand command = new EmailCommand(receiver, classifier, sender);

        command.fetchEmailAndClassify();

        var captor = org.mockito.ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender, times(1)).sendEmail(captor.capture());

        EmailMessage forwarded = captor.getValue();
        assertThat(forwarded.getTo()).containsExactly("unical-scarti@tuodominio.it");
        assertThat(forwarded.getSubject()).startsWith("NON_RICONOSCIUTA");
        assertThat(forwarded.getBody()).contains("mittente@esempio.it");
    }

}
