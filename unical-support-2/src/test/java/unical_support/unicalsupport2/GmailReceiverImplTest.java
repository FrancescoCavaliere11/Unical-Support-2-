package unical_support.unicalsupport2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.service.implementation.GmailReceiverImpl;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;


@SpringBootTest
public class GmailReceiverImplTest {
    private GmailReceiverImpl gmailReceiver;

    @BeforeEach
    void setUp() {
        gmailReceiver = Mockito.spy(new GmailReceiverImpl());
        ReflectionTestUtils.setField(gmailReceiver, "username", "noreply@test.com");
        ReflectionTestUtils.setField(gmailReceiver, "password", "fakepassword");
        ReflectionTestUtils.setField(gmailReceiver, "imapHost", "imap.test.com");
        ReflectionTestUtils.setField(gmailReceiver, "imapPort", 993);
    }

    @Test
    void shouldReturnFakeEmails() {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(List.of("to@test.it"));
        emailMessage.setSubject("Subject Test");
        emailMessage.setBody("Body Test");

        doReturn(List.of(emailMessage)).when(gmailReceiver).receiveEmails();

        List<EmailMessage> emails = gmailReceiver.receiveEmails();

        assertEquals(1, emails.size());
        assertTrue(emails.getFirst().getTo().contains("to@test.it"));
    }

//    @Autowired
//    private GmailReceiverImpl gmailReceiver;
//
//    @Test
//    void shouldReceiveRealEmails() {
//        List<EmailMessage> emails = gmailReceiver.receiveEmails();
//
//        assertThat(emails).isNotNull();
//        emails.forEach(System.out::println);
//    }
}
