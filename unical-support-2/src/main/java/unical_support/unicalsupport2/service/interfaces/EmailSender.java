package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.dto.EmailMessage;

public interface EmailSender {
    void sendEmail(EmailMessage message);
}
