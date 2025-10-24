import jakarta.mail.Message;
import jakarta.mail.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import unical_support.EmailSender;
import unical_support.config.EmailConfig;
import unical_support.model.EmailMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class EmailSenderTest {
    private EmailSender emailSender;

    @BeforeEach
    public void setUp() {
        EmailConfig emailConfig = new EmailConfig() {
            @Override
            public String getSmtpHost() {
                return "smtp.test.com";
            }

            @Override
            public int getSmtpPort() {
                return 587;
            }

            @Override
            public String getUsername() {
                return "test@example.com";
            }

            @Override
            public String getPassword() {
                return "password";
            }
        };
        emailSender = new EmailSender(emailConfig);
    }

    @Test
    public void testSendEmail() {
        EmailMessage email = new EmailMessage("retirver@example.com", "Subject", "Body");
        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            emailSender.send(email);

            transportMock.verify(() ->
                    Transport.send(any(Message.class)),
                    times(1));
        }
    }
}
