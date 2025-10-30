package unical_support.unicalsupport2.service.implementation;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
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

            for (Message message: inbox.getMessages()){
                System.out.println(message.getSubject());
            }

            // Qui prendo solo le emajl che hanno il flag SEEN impostato su false, per il momento le prendiamo tutte
            // Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            Message[] messages = inbox.getMessages();

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
                emailMessage.setBody(getTextFromMessage(m));
                result.add(emailMessage);

                // Quando ci sposteremo in una situazione reale con solo il fetch delle email non lette
                // lì allora potremmo valutare di segnarle come lette, ora è inutile
                //m.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(true);
            store.close();

            log.info("Ricevute {} email", messages.length);
        } catch (Exception e) {
            log.error("Errore durante la lettura delle email: {}", e.getMessage(), e);
            // TODO fare una custom exception
            throw new RuntimeException("Errore nel recupero email", e);
        }

        return result;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException{
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return result + "\n" + bodyPart.getContent();
            }
            result.append(this.parseBodyPart(bodyPart));
        }
        return result.toString();
    }

    private String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
        if (bodyPart.getContent() instanceof MimeMultipart){
            return getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
        }

        return "";
    }
}
