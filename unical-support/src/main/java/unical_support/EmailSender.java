package unical_support;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import unical_support.config.EmailConfig;
import unical_support.model.EmailMessage;

import java.util.Properties;

public class EmailSender {

    private final EmailConfig config;
    private final Properties props;

    public EmailSender(EmailConfig config) {
        this.config = config;

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
    }

    public void send(EmailMessage email) {
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
            message.setSubject(email.getSubject());
            message.setText(email.getBody());

            Transport.send(message);
        } catch (MessagingException e) {
            System.err.println("Unable to send email: " + e.getMessage());
        }
    }
}
