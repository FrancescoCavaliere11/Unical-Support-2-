package unical_support.unicalsupport2.service.implementation;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.dto.EmailMessage;
import unical_support.unicalsupport2.service.interfaces.EmailSender;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "gmail", matchIfMissing = true)
public class GmailSenderImpl implements EmailSender {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.getTo().toArray(new String[0]));
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getBody(), false);

            javaMailSender.send(message);
            log.info("Email inviata a {}", emailMessage.getTo());
        } catch (Exception e) {
            log.error("Errore nell'invio email: {}", e.getMessage(), e);
            // TODO fare una custom exception
            throw new RuntimeException("Failed to send email:" + e);
        }
    }
}
