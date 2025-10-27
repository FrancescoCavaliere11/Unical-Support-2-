package unical_support.unicalsupport2.service.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void receiveEmail();
}
