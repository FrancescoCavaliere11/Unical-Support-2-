package unical_support.unicalsupport2.service.interfaces;

import unical_support.unicalsupport2.data.entities.Email;

public interface EmailSender {
    void sendEmail(Email email);
}
