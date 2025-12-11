package unical_support.unicalsupport2;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.data.dto.classifier.ClassificationResultDto;
import unical_support.unicalsupport2.data.dto.classifier.SingleCategoryDto;
import unical_support.unicalsupport2.data.dto.responder.ResponderResultDto;
import unical_support.unicalsupport2.service.implementation.OrchestratorServiceImpl;
import unical_support.unicalsupport2.service.interfaces.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
class OrchestratorTest {

    @MockitoBean
    private EmailReceiver emailReceiver;

    @MockitoBean
    private EmailClassifier emailClassifier;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private EmailResponder emailResponder;

    @MockitoBean
    private JudgerService judgerService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private OrchestratorServiceImpl orchestrator;

//    @Test
//    void emailNotRecognizedShouldBeForwarded() {
//        EmailMessage fakeEmail = new EmailMessage();
//        fakeEmail.setTo(List.of("mittente@esempio.it"));
//        fakeEmail.setSubject("Domanda strana");
//        fakeEmail.setBody("Corpo della mail");
//
//        when(emailReceiver.receiveEmails()).thenReturn(List.of(fakeEmail));
//
//        ClassificationResultDto fakeResult =
//                new ClassificationResultDto(List.of(new SingleCategoryDto("NON RICONOSCIUTA", 0.1, "testo")), "Categoria non trovata", 0);
//        when(emailClassifier.classifyEmail(anyList())).thenReturn(List.of(fakeResult));
//
//        when(judgerService.judge(anyList(), anyList())).thenReturn(new ArrayList<>());
//
//        ResponderResultDto fakeResponderResult = new ResponderResultDto(0, new ArrayList<>());
//        when(emailResponder.generateEmailResponse(anyList())).thenReturn(List.of(fakeResponderResult));
//
//
//        orchestrator.start(false);
//
//        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
//        verify(emailSender, times(1)).sendEmail(captor.capture());
//
//        EmailMessage forwarded = captor.getValue();
//
//        assertThat(forwarded.getTo()).contains("lorenzo.test.04112025@gmail.com");
//        assertThat(forwarded.getSubject()).contains("NON RICONOSCIUTA");
//        assertThat(forwarded.getBody()).contains("mittente@esempio.it");
//    }

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

        when(judgerService.judge(anyList(), anyList())).thenReturn(new ArrayList<>());

        ResponderResultDto fakeResp = new ResponderResultDto(0, new ArrayList<>());
        when(emailResponder.generateEmailResponse(anyList())).thenReturn(List.of(fakeResp));


        orchestrator.start(false);
    }
}