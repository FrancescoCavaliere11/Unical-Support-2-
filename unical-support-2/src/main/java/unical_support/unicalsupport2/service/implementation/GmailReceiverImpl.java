package unical_support.unicalsupport2.service.implementation;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailReceiver;

import java.io.IOException;
import java.util.*;


/**
 * Email receiver implementation that fetches messages from an IMAP Gmail account.
 *
 * <p>This service is active when the application property {@code mail.provider}
 * is set to {@code gmail} (or when the property is missing). Credentials and IMAP
 * connection details are injected from application properties.</p>
 *
 * <p>Responsibilities:
 * - Connect to the IMAP server using provided credentials,
 * - Read messages from the INBOX,
 * - Convert each {@code Message} into the project's {@code EmailMessage} DTO,
 * - Return the collected messages.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "gmail", matchIfMissing = true)
public class GmailReceiverImpl implements EmailReceiver {
    /**
     * Username (email) used to authenticate to the IMAP server.
     */
    @Value("${spring.mail.username}")
    private String username;
    /**
     * Password (app-specific password) used to authenticate to the IMAP server.
     */
    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.imap.host}")
    private String imapHost;

    @Value("${spring.mail.imap.port}")
    private int imapPort;

    /**
     * Connects to the configured IMAP server, reads messages from the INBOX and
     * converts them to {@code EmailMessage} instances.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Create a {@code Session} and {@code Store} for IMAPS.</li>
     *   <li>Open the INBOX in read-write mode and iterate messages.</li>
     *   <li>Build {@code EmailMessage} objects with sender, subject and plain text body.</li>
     *   <li>Close folder and store and return the collected list.</li>
     * </ul>
     *
     * <p>Side effects: logs info/errors and may throw a {@code RuntimeException} on errors.</p>
     *
     * @return list of received {@code EmailMessage} (empty if none)
     */
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
                EmailMessage emailMessage = new EmailMessage();

                String originalMessageId = m.getHeader("Message-ID")[0];
                if (originalMessageId != null) {
                    emailMessage.setInReplyToHeader(originalMessageId);

                    String[] refs = m.getHeader("References");
                    String existingReferences = (refs != null && refs.length > 0) ? refs[0] : null;

                    String newReferences = (existingReferences != null ? existingReferences + " " : "") + originalMessageId;
                    emailMessage.setReferencesHeader(newReferences);
                }

                Address[] replyTo = m.getReplyTo();
                String senderAddress;
                if (replyTo != null && replyTo.length > 0) {
                    senderAddress = (replyTo[0] instanceof InternetAddress)
                            ? ((InternetAddress) replyTo[0]).getAddress()
                            : replyTo[0].toString();
                } else {
                    Address[] from = m.getFrom();
                    senderAddress = (from != null && from.length > 0)
                            ? ((from[0] instanceof InternetAddress) ? ((InternetAddress) from[0]).getAddress() : from[0].toString())
                            : "sconosciuto@domain.com";
                }
                emailMessage.setTo(Collections.singletonList(senderAddress));

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

    /**
     * Extracts a plain-text representation from the given {@code Message}.
     *
     * <p>Supports:
     * - text/plain messages,
     * - multipart messages (delegates to {@link #getTextFromMimeMultipart}).</p>
     *
     * @param message the email message to extract text from
     * @return extracted plain text or an empty string if unsupported
     * @throws MessagingException on mail parsing errors
     * @throws IOException on IO errors reading parts
     */
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

    /**
     * Walks a {@code MimeMultipart} and concatenates plain-text parts.
     *
     * <p>Returns the combined text of the multipart parts. For parts that are themselves
     * multipart, recursion is applied.</p>
     *
     * @param mimeMultipart the multipart container
     * @return concatenated plain text content
     * @throws MessagingException on mail parsing errors
     * @throws IOException on IO errors reading parts
     */
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

    /**
     * Parses a single body part, returning plain text if the part contains nested multipart content.
     *
     * @param bodyPart the body part to parse
     * @return extracted text or an empty string if unsupported
     * @throws MessagingException on mail parsing errors
     * @throws IOException on IO errors reading the body part
     */
    private String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
        if (bodyPart.getContent() instanceof MimeMultipart){
            return getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
        }

        return "";
    }
}
