package unical_support.unicalsupport2.service.implementation;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "gmail", matchIfMissing = true)
public class GmailReceiverImpl implements EmailReceiver {
    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.imap.host}")
    private String imapHost;

    @Value("${spring.mail.imap.port}")
    private int imapPort;

    @Override
    public List<EmailMessage> receiveEmails() {

        List<EmailMessage> result = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imapHost);
        props.put("mail.imaps.port", String.valueOf(imapPort));
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(imapHost, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message m : messages) {
                Address[] from = m.getFrom();
                EmailMessage emailMessage = new EmailMessage();
                if (from != null && from.length > 0) {
                    String sender;
                    if (from[0] instanceof InternetAddress) {
                        sender = ((InternetAddress) from[0]).getAddress();
                    } else {
                        sender = from[0].toString();
                    }
                    emailMessage.setTo(Collections.singletonList(sender));
                }
                emailMessage.setSubject(m.getSubject());
                emailMessage.setBody(extractText(m));
                result.add(emailMessage);

                m.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(true);
            store.close();

            log.info("Ricevute {} email", messages.length);
        } catch (Exception e) {
            log.error("❌ Errore durante la lettura delle email: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nel recupero email", e);
        }

        return result;
    }

    private String extractText(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }
        return "";
    }
}
