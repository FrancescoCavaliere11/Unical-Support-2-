package unical_support.unicalsupport2;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import unical_support.unicalsupport2.data.dto.EmailMessage;
import unical_support.unicalsupport2.service.implementation.GmailSenderImpl;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
public class GmailSenderImplTest {
    private JavaMailSender mailSender;
    private GmailSenderImpl gmailSender;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        gmailSender = new GmailSenderImpl(mailSender);

        // imposto il campo @Value per il test
        ReflectionTestUtils.setField(gmailSender, "fromEmail", "noreply@test.com");
    }

    @Test
    void shouldSendEmailSuccessfully() {
        MimeMessage mockMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        EmailMessage email = new EmailMessage();
        email.setTo(List.of("test@example.com"));
        email.setSubject("Test Subject");
        email.setBody("Hello, World!");

        gmailSender.sendEmail(email);

        verify(mailSender, times(1)).send(mockMessage);
    }

//    Test reale che invia una mail vera, commentare il codice sopra prima di eseguirlo e assicurarsi
//    di volerlo fare (successivamente decommentare il codice sopra ricommentando questo)
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Autowired
//    private GmailSenderImpl gmailSender;
//
//    @Test
//    void shouldSendRealEmail() {
//        EmailMessage email = new EmailMessage();
//        email.setTo(List.of("")); // inserire la tua mail
//        email.setSubject("Test reale");
//        email.setBody("Ciao, questa è una mail di test reale!");
//
//        gmailSender.sendEmail(email);
//    }
}
