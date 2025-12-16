package unical_support.unicalsupport2;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.service.implementation.GmailSenderImpl;
import org.thymeleaf.context.Context;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.shell.interactive.enabled=false"})
public class GmailSenderImplTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    private GmailSenderImpl gmailSender;

    @BeforeEach
    void setUp() {
        gmailSender = new GmailSenderImpl(mailSender, templateEngine);

        ReflectionTestUtils.setField(gmailSender, "fromEmail", "noreply@test.com");
    }

    @Test
    void shouldSendEmailSuccessfully() {
        MimeMessage mockMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>Mock Body</body></html>");

        EmailMessage email = new EmailMessage();
        email.setTo(List.of("test@example.com"));
        email.setSubject("Test Subject");
        email.setBody("Hello, World!");

        gmailSender.sendEmail(email);

        verify(mailSender, times(1)).send(mockMessage);

        verify(templateEngine).process(eq("email-reply"), any(Context.class));
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
