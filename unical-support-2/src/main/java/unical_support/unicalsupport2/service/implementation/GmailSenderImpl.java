package unical_support.unicalsupport2.service.implementation;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailSender;

/**
 * Service that sends email messages using the configured {@code JavaMailSender}.
 *
 * <p>This implementation is active when the application property {@code mail.provider}
 * is set to {@code gmail} (or when the property is missing). The sender address is
 * injected from {@code spring.mail.username}.</p>
 *
 * <p>Responsibilities:
 * - Build a {@code MimeMessage} from an {@code EmailMessage}.
 * - Set sender, recipients, subject and plain text body.
 * - Send the message via {@code JavaMailSender} and log the outcome.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "gmail", matchIfMissing = true)
public class GmailSenderImpl implements EmailSender {
    /**
     * Spring's mail sender used to create and send MIME messages.
     */
    private final JavaMailSender javaMailSender;

    /**
     * Default sender address injected from {@code spring.mail.username}.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends the provided {@code EmailMessage} as a plain-text MIME email.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Create a {@code MimeMessage} and a {@code MimeMessageHelper} with UTF-8.</li>
     *   <li>Set the {@code from} address, recipients, subject and body (plain text).</li>
     *   <li>Send the message via {@code JavaMailSender} and log success or error.</li>
     * </ul>
     *
     * <p>Errors are logged and rethrown as a {@code RuntimeException}</p>
     *
     * @param emailMessage message containing recipients, subject and body
     */
    @Override
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.getTo().toArray(new String[0]));

            String subject = emailMessage.getSubject();
            if (subject != null) {
                if (!subject.toLowerCase().startsWith("re:")) {
                    subject = "Re: " + subject;
                }
                emailMessage.setSubject(subject);
            } else {
                emailMessage.setSubject("Re: (No Subject)");
            }

            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getBody(), false);

            if (emailMessage.getInReplyToHeader() != null && !emailMessage.getInReplyToHeader().isEmpty()) {
                message.setHeader("In-Reply-To", emailMessage.getInReplyToHeader());
            }

            if (emailMessage.getReferencesHeader() != null && !emailMessage.getReferencesHeader().isEmpty()) {
                message.setHeader("References", emailMessage.getReferencesHeader());
            }

            javaMailSender.send(message);
            log.info("Email inviata a {}", emailMessage.getTo());
        } catch (Exception e) {
            log.error("Errore nell'invio email: {}", e.getMessage(), e);
            // TODO fare una custom exception
            throw new RuntimeException("Failed to send email:" + e);
        }
    }
}
